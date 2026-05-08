package com.smarthome.domain.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smarthome.domain.statemachine.StateMachine;
import com.smarthome.domain.statemachine.State;
import com.smarthome.domain.statemachine.Transition;
import com.smarthome.exception.InvalidTransitionException;

import java.util.List;
import java.util.Map;

/**
 * Smart fan device.
 *
 * <p>States: Off, On/Low, On/Medium, On/High
 * Speed is retained when powered off and restored on power-on.
 * Default speed on first power-on is Medium.</p>
 *
 * <p>State machine events: power_on, power_off, set_speed_low, set_speed_medium, set_speed_high</p>
 */
public class FanDevice extends Device {

    public enum FanState implements State {
        OFF, LOW, MEDIUM, HIGH;
    }

    public enum FanSpeed { LOW, MEDIUM, HIGH }

    private FanState state = FanState.OFF;
    private FanSpeed lastSpeed = FanSpeed.MEDIUM;

    @JsonIgnore
    private StateMachine<FanState> stateMachine;

    public FanDevice() {
        super();
        initStateMachine();
    }

    public FanDevice(String name, String location) {
        super(name, location, DeviceType.FAN);
        initStateMachine();
    }

    private void initStateMachine() {
        List<Transition<FanState>> transitions = List.of(
            new Transition<>(FanState.OFF, FanState.MEDIUM, "power_on"),
            new Transition<>(FanState.LOW, FanState.OFF, "power_off"),
            new Transition<>(FanState.MEDIUM, FanState.OFF, "power_off"),
            new Transition<>(FanState.HIGH, FanState.OFF, "power_off"),
            new Transition<>(FanState.LOW, FanState.MEDIUM, "set_speed_medium"),
            new Transition<>(FanState.LOW, FanState.HIGH, "set_speed_high"),
            new Transition<>(FanState.MEDIUM, FanState.LOW, "set_speed_low"),
            new Transition<>(FanState.MEDIUM, FanState.HIGH, "set_speed_high"),
            new Transition<>(FanState.HIGH, FanState.LOW, "set_speed_low"),
            new Transition<>(FanState.HIGH, FanState.MEDIUM, "set_speed_medium")
        );
        stateMachine = new StateMachine<>(state, transitions);
    }

    @Override
    public boolean isOn() {
        return state != FanState.OFF;
    }

    @Override
    public String executeCommand(String command, Map<String, Object> payload) {
        return switch (command) {
            case "power_on" -> {
                // Restore last speed if known
                String speedEvent = switch (lastSpeed) {
                    case LOW -> "set_speed_low";
                    case HIGH -> "set_speed_high";
                    default -> "power_on";
                };
                // For power_on we use the power_on transition then apply speed
                stateMachine.trigger("power_on");
                state = FanState.MEDIUM;
                if (lastSpeed == FanSpeed.LOW) {
                    stateMachine.trigger("set_speed_low");
                    state = FanState.LOW;
                } else if (lastSpeed == FanSpeed.HIGH) {
                    stateMachine.trigger("set_speed_high");
                    state = FanState.HIGH;
                }
                yield "Fan turned on at " + lastSpeed + " speed";
            }
            case "power_off" -> {
                lastSpeed = getCurrentSpeed();
                stateMachine.trigger("power_off");
                state = FanState.OFF;
                yield "Fan turned off";
            }
            case "set_speed_low" -> {
                stateMachine.trigger("set_speed_low");
                state = FanState.LOW;
                lastSpeed = FanSpeed.LOW;
                yield "Fan speed set to Low";
            }
            case "set_speed_medium" -> {
                stateMachine.trigger("set_speed_medium");
                state = FanState.MEDIUM;
                lastSpeed = FanSpeed.MEDIUM;
                yield "Fan speed set to Medium";
            }
            case "set_speed_high" -> {
                stateMachine.trigger("set_speed_high");
                state = FanState.HIGH;
                lastSpeed = FanSpeed.HIGH;
                yield "Fan speed set to High";
            }
            default -> throw new InvalidTransitionException("Unknown command: " + command);
        };
    }

    private FanSpeed getCurrentSpeed() {
        return switch (state) {
            case LOW -> FanSpeed.LOW;
            case HIGH -> FanSpeed.HIGH;
            default -> FanSpeed.MEDIUM;
        };
    }

    @Override
    public void resetToDefaults() {
        state = FanState.OFF;
        lastSpeed = FanSpeed.MEDIUM;
        initStateMachine();
    }

    // --- Getters / Setters ---

    public FanState getState() { return state; }
    public void setState(FanState state) {
        this.state = state;
        if (stateMachine == null) initStateMachine();
        stateMachine.rehydrate(state);
    }

    public FanSpeed getLastSpeed() { return lastSpeed; }
    public void setLastSpeed(FanSpeed lastSpeed) { this.lastSpeed = lastSpeed; }
}
