package com.smarthome.repository;

import com.smarthome.domain.device.CommandHistory;
import com.smarthome.domain.device.Device;

import java.util.List;

/**
 * The root data model serialized to and deserialized from devices.json.
 * Represents the complete persisted state of the smart home simulator.
 */
public class DataStore {
    private List<Device> devices = new java.util.ArrayList<>();
    private List<CommandHistory> commandHistory = new java.util.ArrayList<>();
    private SimulationSettings simulationSettings = new SimulationSettings();

    public List<Device> getDevices() { return devices; }
    public void setDevices(List<Device> devices) { this.devices = devices; }

    public List<CommandHistory> getCommandHistory() { return commandHistory; }
    public void setCommandHistory(List<CommandHistory> commandHistory) { this.commandHistory = commandHistory; }

    public SimulationSettings getSimulationSettings() { return simulationSettings; }
    public void setSimulationSettings(SimulationSettings simulationSettings) { this.simulationSettings = simulationSettings; }

    public static class SimulationSettings {
        private int speedMultiplier = 1;

        public int getSpeedMultiplier() { return speedMultiplier; }
        public void setSpeedMultiplier(int speedMultiplier) { this.speedMultiplier = speedMultiplier; }
    }
}
