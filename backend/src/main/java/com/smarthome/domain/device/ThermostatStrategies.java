package com.smarthome.domain.device;

/**
 * Concrete Strategy implementations for thermostat heating/cooling modes.
 */
public class ThermostatStrategies {

    /** Heat-only mode: heats when ambient is below desired, never cools. */
    public static class HeatStrategy implements ThermostatModeStrategy {
        @Override
        public ThermostatAction evaluate(double ambientTemp, double desiredTemp) {
            if (ambientTemp < desiredTemp) return ThermostatAction.HEAT;
            return ThermostatAction.IDLE;
        }
    }

    /** Cool-only mode: cools when ambient is above desired, never heats. */
    public static class CoolStrategy implements ThermostatModeStrategy {
        @Override
        public ThermostatAction evaluate(double ambientTemp, double desiredTemp) {
            if (ambientTemp > desiredTemp) return ThermostatAction.COOL;
            return ThermostatAction.IDLE;
        }
    }

    /** Auto mode: heats or cools automatically based on ambient vs. desired. */
    public static class AutoStrategy implements ThermostatModeStrategy {
        @Override
        public ThermostatAction evaluate(double ambientTemp, double desiredTemp) {
            if (ambientTemp < desiredTemp) return ThermostatAction.HEAT;
            if (ambientTemp > desiredTemp) return ThermostatAction.COOL;
            return ThermostatAction.IDLE;
        }
    }

    public static ThermostatModeStrategy forMode(ThermostatDevice.ThermostatMode mode) {
        return switch (mode) {
            case HEAT -> new HeatStrategy();
            case COOL -> new CoolStrategy();
            case AUTO -> new AutoStrategy();
        };
    }
}
