package com.smarthome.domain.device;

import com.smarthome.exception.SmartHomeException;
import org.springframework.stereotype.Component;

/**
 * Factory for creating device instances by type.
 *
 * <p>The Factory pattern centralizes device creation logic. Adding a new device type
 * requires only adding a new case here — no existing code is modified.
 * This satisfies the Open-Closed Principle.</p>
 */
@Component
public class DeviceFactory {

    /**
     * Creates a new device of the given type with default state.
     *
     * @param type     the device type
     * @param name     human-readable device name
     * @param location room or area name
     * @return a new device initialized in its default state
     */
    public Device create(DeviceType type, String name, String location) {
        return switch (type) {
            case LIGHT -> new LightDevice(name, location);
            case FAN -> new FanDevice(name, location);
            case THERMOSTAT -> new ThermostatDevice(name, location);
            case DOOR_LOCK -> new DoorLockDevice(name, location);
            default -> throw new SmartHomeException("Unknown device type: " + type);
        };
    }
}
