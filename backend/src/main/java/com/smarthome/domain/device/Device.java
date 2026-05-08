package com.smarthome.domain.device;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

/**
 * Abstract base class for all smart home devices.
 *
 * <p>Carries universal device metadata (id, name, location, type) and declares
 * the abstract contract that every device subtype must implement:
 * state machine control, command processing, and persistence support.</p>
 *
 * <p>Follows the Open-Closed Principle — new device types extend this class
 * without modifying it or the infrastructure layer.</p>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = LightDevice.class, name = "LIGHT"),
    @JsonSubTypes.Type(value = FanDevice.class, name = "FAN"),
    @JsonSubTypes.Type(value = ThermostatDevice.class, name = "THERMOSTAT"),
    @JsonSubTypes.Type(value = DoorLockDevice.class, name = "DOOR_LOCK")
})
public abstract class Device {

    protected String id;
    protected String name;
    protected String location;
    protected DeviceType type;

    protected Device() {}

    protected Device(String name, String location, DeviceType type) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.location = location;
        this.type = type;
    }

    // --- Getters / Setters for Jackson ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public DeviceType getType() { return type; }
    public void setType(DeviceType type) { this.type = type; }

    /**
     * Returns whether this device is considered "on" for UI filtering.
     * Powered devices: on when state is On (or Heating/Cooling for thermostat).
     * Latch devices: always on.
     */
    public abstract boolean isOn();

    /**
     * Executes a command on this device, returning a description of the operation.
     * Invalid commands throw {@link com.smarthome.exception.InvalidTransitionException}.
     *
     * @param command the command name (e.g., "power_on", "set_brightness")
     * @param payload optional command parameters
     * @return human-readable description of what was done
     */
    public abstract String executeCommand(String command, java.util.Map<String, Object> payload);

    /**
     * Resets this device to its factory default state.
     */
    public abstract void resetToDefaults();
}
