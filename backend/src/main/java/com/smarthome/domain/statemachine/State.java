package com.smarthome.domain.statemachine;

/**
 * Represents a state in the state machine.
 * Each state has a name for serialization and display.
 */
public interface State {
    String name();
}
