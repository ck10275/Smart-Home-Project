package com.smarthome.domain.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smarthome.domain.statemachine.StateMachine;
import com.smarthome.domain.statemachine.State;
import com.smarthome.domain.statemachine.Transition;
import com.smarthome.exception.InvalidTransitionException;

import java.util.List;
import java.util.Map;

/**
 * Smart thermostat device.
 *
 * <p>States: Off, Idle, Heating, Cooling
 * Mode (Heat/Cool/Auto) uses the Strategy pattern to determine transitions.
 * Ambient temperature changes 1°F every 5 seconds (configurable via simulation speed).</p>
 *
 * <p>Invariant: only one thermostat per location. Enforced by the service layer.</p>
 */
public class ThermostatDevice extends Device {

    public enum ThermostatState implements State {
        OFF, IDLE, HEATING, COOLING;
    }

    public enum ThermostatMode { HEAT, COOL, AUTO }

    private ThermostatState state = ThermostatState.OFF;
    private ThermostatMode mode = ThermostatMode.AUTO;
    private double desiredTemperature = 72.0;
    private double ambientTemperature = 70.0;

    @JsonIgnore
    private StateMachine<ThermostatState> stateMachine;
    @JsonIgnore
    private ThermostatModeStrategy modeStrategy;

    public ThermostatDevice() {
        super();
        initStateMachine();
        initStrategy();
    }

    public ThermostatDevice(String name, String location) {
        super(name, location, DeviceType.THERMOSTAT);
        initStateMachine();
        initStrategy();
    }

    private void initStateMachine() {
        List<Transition<ThermostatState>> transitions = List.of(
            new Transition<>(ThermostatState.OFF, ThermostatState.IDLE, "power_on"),
            new Transition<>(ThermostatState.IDLE, ThermostatState.OFF, "power_off"),
            new Transition<>(ThermostatState.HEATING, ThermostatState.OFF, "power_off"),
            new Transition<>(ThermostatState.COOLING, ThermostatState.OFF, "power_off"),
            new Transition<>(ThermostatState.IDLE, ThermostatState.HEATING, "start_heating"),
            new Transition<>(ThermostatState.IDLE, ThermostatState.COOLING, "start_cooling"),
            new Transition<>(ThermostatState.HEATING, ThermostatState.IDLE, "reach_target"),
            new Transition<>(ThermostatState.COOLING, ThermostatState.IDLE, "reach_target"),
            new Transition<>(ThermostatState.HEATING, ThermostatState.COOLING, "switch_to_cooling"),
            new Transition<>(ThermostatState.COOLING, ThermostatState.HEATING, "switch_to_heating")
        );
        stateMachine = new StateMachine<>(state, transitions);
    }

    private void initStrategy() {
        modeStrategy = ThermostatStrategies.forMode(mode);
    }

    @Override
    public boolean isOn() {
        return state == ThermostatState.HEATING || state == ThermostatState.COOLING;
    }

    @Override
    public String executeCommand(String command, Map<String, Object> payload) {
        return switch (command) {
            case "power_on" -> {
                stateMachine.trigger("power_on");
                state = ThermostatState.IDLE;
                evaluateAndTransition();
                yield "Thermostat powered on";
            }
            case "power_off" -> {
                stateMachine.trigger("power_off");
                state = ThermostatState.OFF;
                yield "Thermostat powered off";
            }
            case "set_desired_temperature" -> {
                double temp = ((Number) payload.get("temperature")).doubleValue();
                if (temp < 60 || temp > 80) {
                    throw new IllegalArgumentException("Desired temperature must be between 60 and 80°F");
                }
                desiredTemperature = temp;
                if (state != ThermostatState.OFF) evaluateAndTransition();
                yield "Desired temperature set to " + desiredTemperature + "°F";
            }
            case "set_mode" -> {
                mode = ThermostatMode.valueOf(((String) payload.get("mode")).toUpperCase());
                initStrategy();
                if (state != ThermostatState.OFF) evaluateAndTransition();
                yield "Mode set to " + mode;
            }
            default -> throw new InvalidTransitionException("Unknown thermostat command: " + command);
        };
    }

    /**
     * Called by the simulation tick service every N seconds.
     * Adjusts ambient temperature by 1°F toward desired and re-evaluates state.
     */
    public void simulationTick() {
        if (state == ThermostatState.OFF || state == ThermostatState.IDLE) return;

        if (state == ThermostatState.HEATING) {
            ambientTemperature = Math.min(ambientTemperature + 1, desiredTemperature);
        } else if (state == ThermostatState.COOLING) {
            ambientTemperature = Math.max(ambientTemperature - 1, desiredTemperature);
        }

        evaluateAndTransition();
    }

    /**
     * Re-evaluates whether the thermostat should be heating, cooling, or idle
     * based on the current mode strategy and temperatures.
     */
    public void evaluateAndTransition() {
        if (state == ThermostatState.OFF) return;

        ThermostatModeStrategy.ThermostatAction action =
            modeStrategy.evaluate(ambientTemperature, desiredTemperature);

        switch (action) {
            case HEAT -> {
                if (state == ThermostatState.IDLE) {
                    stateMachine.trigger("start_heating");
                    state = ThermostatState.HEATING;
                } else if (state == ThermostatState.COOLING) {
                    stateMachine.trigger("switch_to_heating");
                    state = ThermostatState.HEATING;
                }
            }
            case COOL -> {
                if (state == ThermostatState.IDLE) {
                    stateMachine.trigger("start_cooling");
                    state = ThermostatState.COOLING;
                } else if (state == ThermostatState.HEATING) {
                    stateMachine.trigger("switch_to_cooling");
                    state = ThermostatState.COOLING;
                }
            }
            case IDLE -> {
                if (state == ThermostatState.HEATING || state == ThermostatState.COOLING) {
                    stateMachine.trigger("reach_target");
                    state = ThermostatState.IDLE;
                }
            }
        }
    }

    @Override
    public void resetToDefaults() {
        state = ThermostatState.OFF;
        mode = ThermostatMode.AUTO;
        desiredTemperature = 72.0;
        ambientTemperature = 70.0;
        initStateMachine();
        initStrategy();
    }

    // --- Getters / Setters ---

    public ThermostatState getState() { return state; }
    public void setState(ThermostatState state) {
        this.state = state;
        if (stateMachine == null) initStateMachine();
        stateMachine.rehydrate(state);
    }

    public ThermostatMode getMode() { return mode; }
    public void setMode(ThermostatMode mode) {
        this.mode = mode;
        initStrategy();
    }

    public double getDesiredTemperature() { return desiredTemperature; }
    public void setDesiredTemperature(double desiredTemperature) { this.desiredTemperature = desiredTemperature; }

    public double getAmbientTemperature() { return ambientTemperature; }
    public void setAmbientTemperature(double ambientTemperature) {
        this.ambientTemperature = ambientTemperature;
        if (state != ThermostatState.OFF && modeStrategy != null) {
            evaluateAndTransition();
        }
    }
}
