package com.smarthome.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.HashMap;
import java.util.Map;

public class CommandRequest {
    @NotBlank(message = "Command is required")
    private String command;
    private Map<String, Object> payload = new HashMap<>();

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
}
