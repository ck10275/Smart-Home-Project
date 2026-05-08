package com.smarthome.domain.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smarthome.domain.statemachine.StateMachine;
import com.smarthome.domain.statemachine.State;
import com.smarthome.domain.statemachine.Transition;
import com.smarthome.exception.InvalidTransitionException;

import java.util.List;
import java.util.Map;

/**
 * Door lock device — a latch device.
 *
 * <p>Latch devices are always energized and have no Off state.
 * They are always considered "on" for UI filtering purposes.
 * The state machine operates entirely at the substate level: Locked / Unlocked.</p>
 *
 * <p>Default state is Locked.</p>
 */
public class DoorLockDevice extends Device {

    public enum LockState implements State {
        LOCKED, UNLOCKED;
    }

    private LockState state = LockState.LOCKED;

    @JsonIgnore
    private StateMachine<LockState> stateMachine;

    public DoorLockDevice() {
        super();
        initStateMachine();
    }

    public DoorLockDevice(String name, String location) {
        super(name, location, DeviceType.DOOR_LOCK);
        initStateMachine();
    }

    private void initStateMachine() {
        List<Transition<LockState>> transitions = List.of(
            new Transition<>(LockState.LOCKED, LockState.UNLOCKED, "unlock"),
            new Transition<>(LockState.UNLOCKED, LockState.LOCKED, "lock")
        );
        stateMachine = new StateMachine<>(state, transitions);
    }

    @Override
    public boolean isOn() {
        // Latch devices are always "on"
        return true;
    }

    @Override
    public String executeCommand(String command, Map<String, Object> payload) {
        return switch (command) {
            case "lock" -> {
                stateMachine.trigger("lock");
                state = LockState.LOCKED;
                yield "Door locked";
            }
            case "unlock" -> {
                stateMachine.trigger("unlock");
                state = LockState.UNLOCKED;
                yield "Door unlocked";
            }
            default -> throw new InvalidTransitionException("Unknown command: " + command);
        };
    }

    @Override
    public void resetToDefaults() {
        state = LockState.UNLOCKED;
        initStateMachine();
    }

    // --- Getters / Setters ---

    public LockState getState() { return state; }
    public void setState(LockState state) {
        this.state = state;
        if (stateMachine == null) initStateMachine();
        stateMachine.rehydrate(state);
    }
}
