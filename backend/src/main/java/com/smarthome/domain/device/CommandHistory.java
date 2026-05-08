package com.smarthome.domain.device;

import java.time.Instant;
import java.util.UUID;

/**
 * Audit log entry recording an operation performed on a device.
 * The command history is persisted alongside device state.
 */
public class CommandHistory {

    private String id;
    private String deviceId;
    private String deviceName;
    private String operation;
    private Instant timestamp;

    public CommandHistory() {}

    public CommandHistory(String deviceId, String deviceName, String operation) {
        this.id = UUID.randomUUID().toString();
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.operation = operation;
        this.timestamp = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
