package com.smarthome.domain.statemachine;

import com.smarthome.exception.InvalidTransitionException;

import java.util.List;
import java.util.Optional;

/**
 * Generic, reusable state machine engine.
 *
 * <p>This engine is intentionally device-agnostic. Adding a new device type requires
 * only defining its states and transitions — the engine itself is never modified.
 * This satisfies the Open-Closed Principle: the engine is open for extension
 * (new device types) but closed for modification.</p>
 *
 * <p>Invalid transitions are rejected with {@link InvalidTransitionException}.
 * Transitions are never silently ignored.</p>
 *
 * @param <S> the state enum type implementing {@link State}
 */
public class StateMachine<S extends Enum<S> & State> {

    private S currentState;
    private final List<Transition<S>> transitions;

    public StateMachine(S initialState, List<Transition<S>> transitions) {
        this.currentState = initialState;
        this.transitions = List.copyOf(transitions);
    }

    /**
     * Attempts to trigger an event and transition to the target state.
     * Throws {@link InvalidTransitionException} if the event is not valid from the current state.
     *
     * @param event the event identifier to trigger
     * @return the new state after the transition
     */
    public S trigger(String event) {
        Optional<Transition<S>> match = transitions.stream()
                .filter(t -> t.matches(currentState, event))
                .findFirst();

        if (match.isEmpty()) {
            throw new InvalidTransitionException(
                "Invalid transition: event '" + event + "' is not allowed from state '" + currentState.name() + "'"
            );
        }

        currentState = match.get().to();
        return currentState;
    }

    /**
     * Returns whether the given event is valid from the current state.
     * Used by the UI and validation layer to disable invalid controls.
     */
    public boolean canTrigger(String event) {
        return transitions.stream().anyMatch(t -> t.matches(currentState, event));
    }

    /**
     * Returns the current state. Used for dehydration (serialization to persistence).
     */
    public S getCurrentState() {
        return currentState;
    }

    /**
     * Rehydrates (restores) the state machine to a previously persisted state.
     * Called on application startup to restore device state from the JSON store.
     */
    public void rehydrate(S state) {
        this.currentState = state;
    }
}
