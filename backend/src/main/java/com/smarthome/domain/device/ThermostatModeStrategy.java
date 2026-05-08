package com.smarthome.domain.device;

/**
 * Strategy interface for thermostat mode behavior.
 *
 * <p>The Strategy pattern is applied here to encapsulate the three thermostat modes
 * (Heat, Cool, Auto) as interchangeable behaviors. This avoids conditional branching
 * inside ThermostatDevice and satisfies the Open-Closed Principle — new modes can
 * be added without modifying the thermostat itself.</p>
 */
public interface ThermostatModeStrategy {

    /**
     * Determines what action the thermostat should take given ambient vs desired temp.
     */
    ThermostatAction evaluate(double ambientTemp, double desiredTemp);

    enum ThermostatAction { HEAT, COOL, IDLE }
}
