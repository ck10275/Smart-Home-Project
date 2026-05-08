package com.smarthome.domain.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smarthome.domain.statemachine.StateMachine;
import com.smarthome.domain.statemachine.State;
import com.smarthome.domain.statemachine.Transition;
import com.smarthome.exception.InvalidTransitionException;

import java.util.List;
import java.util.Map;

/**
 * Smart light device.
 *
 * <p>States: Off, On (with sub-state Illuminating)
 * Attributes: brightness [10–100], color (hex RGB)
 * Settings are retained when powered off and restored on power-on.</p>
 *
 * <p>State machine events: power_on, power_off, set_brightness, set_color</p>
 */
public class LightDevice extends Device {

    public enum LightState implements State {
        OFF, ON;
    }

    // Persisted fields
    private LightState state = LightState.OFF;
    private int brightness = 100;
    private String color = "#FFFFFF";

    @JsonIgnore
    private StateMachine<LightState> stateMachine;

    public LightDevice() {
        super();
        initStateMachine();
    }

    public LightDevice(String name, String location) {
        super(name, location, DeviceType.LIGHT);
        initStateMachine();
    }

    private void initStateMachine() {
        List<Transition<LightState>> transitions = List.of(
            new Transition<>(LightState.OFF, LightState.ON, "power_on"),
            new Transition<>(LightState.ON, LightState.OFF, "power_off"),
            new Transition<>(LightState.ON, LightState.ON, "set_brightness"),
            new Transition<>(LightState.ON, LightState.ON, "set_color")
        );
        stateMachine = new StateMachine<>(state, transitions);
    }

    @Override
    public boolean isOn() {
        return state == LightState.ON;
    }

    @Override
    public String executeCommand(String command, Map<String, Object> payload) {
        return switch (command) {
            case "power_on" -> {
                stateMachine.trigger("power_on");
                state = LightState.ON;
                yield "Light turned on";
            }
            case "power_off" -> {
                stateMachine.trigger("power_off");
                state = LightState.OFF;
                yield "Light turned off";
            }
            case "set_brightness" -> {
                stateMachine.trigger("set_brightness");
                int value = ((Number) payload.get("brightness")).intValue();
                if (value < 10 || value > 100) {
                    throw new IllegalArgumentException("Brightness must be between 10 and 100");
                }
                brightness = value;
                yield "Brightness set to " + brightness + "%";
            }
            case "set_color" -> {
                stateMachine.trigger("set_color");
                color = (String) payload.get("color");
                yield "Color set to " + color;
            }
            default -> throw new InvalidTransitionException("Unknown command: " + command);
        };
    }

    @Override
    public void resetToDefaults() {
        state = LightState.OFF;
        brightness = 100;
        color = "#FFFFFF";
        initStateMachine();
    }

    // --- Getters / Setters (for JSON dehydration/rehydration) ---

    public LightState getState() { return state; }
    public void setState(LightState state) {
        this.state = state;
        if (stateMachine == null) initStateMachine();
        stateMachine.rehydrate(state);
    }

    public int getBrightness() { return brightness; }
    public void setBrightness(int brightness) { this.brightness = brightness; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
