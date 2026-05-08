package com.smarthome.domain.statemachine;

/**
 * Represents a valid transition between two states in the state machine.
 * Transitions define which state changes are allowed — all others are rejected.
 *
 * @param <S> the state enum type
 */
public record Transition<S extends Enum<S> & State>(S from, S to, String event) {
    public boolean matches(S currentState, String triggerEvent) {
        return this.from == currentState && this.event.equals(triggerEvent);
    }
}
