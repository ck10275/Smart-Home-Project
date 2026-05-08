package com.smarthome.dto;

import com.smarthome.domain.device.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RegisterDeviceRequest {
    @NotNull(message = "Device type is required")
    private DeviceType type;

    @NotBlank(message = "Device name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    public DeviceType getType() { return type; }
    public void setType(DeviceType type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
