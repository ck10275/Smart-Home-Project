package com.smarthome.service;

import com.smarthome.domain.device.*;
import com.smarthome.exception.DeviceNotFoundException;
import com.smarthome.exception.SmartHomeException;
import com.smarthome.repository.IDeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer containing all business logic for device management.
 *
 * <p>This layer is independent of HTTP concerns (no request/response objects)
 * and independent of persistence details (depends only on {@link IDeviceRepository}).</p>
 */
@Service
public class DeviceService {

    private final IDeviceRepository repository;
    private final DeviceFactory deviceFactory;

    public DeviceService(IDeviceRepository repository, DeviceFactory deviceFactory) {
        this.repository = repository;
        this.deviceFactory = deviceFactory;
    }

    public List<Device> getAllDevices(String location, DeviceType type, Boolean onlyOn, Boolean onlyOff) {
        List<Device> devices = repository.findAll();

        if (location != null && !location.isBlank()) {
            devices = devices.stream()
                .filter(d -> d.getLocation().equalsIgnoreCase(location))
                .collect(Collectors.toList());
        }
        if (type != null) {
            devices = devices.stream()
                .filter(d -> d.getType() == type)
                .collect(Collectors.toList());
        }
        if (Boolean.TRUE.equals(onlyOn)) {
            devices = devices.stream().filter(Device::isOn).collect(Collectors.toList());
        }
        if (Boolean.TRUE.equals(onlyOff)) {
            devices = devices.stream().filter(d -> !d.isOn()).collect(Collectors.toList());
        }

        return devices;
    }

    public Device getDevice(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new DeviceNotFoundException(id));
    }

    /**
     * Registers a new device. Enforces the one-thermostat-per-location invariant.
     */
    public Device registerDevice(DeviceType type, String name, String location) {
        if (type == DeviceType.THERMOSTAT) {
            boolean hasThermostat = repository.findByLocation(location).stream()
                .anyMatch(d -> d.getType() == DeviceType.THERMOSTAT);
            if (hasThermostat) {
                throw new SmartHomeException(
                    "A thermostat already exists in location '" + location + "'. Only one thermostat is allowed per location."
                );
            }
        }
        Device device = deviceFactory.create(type, name, location);
        repository.save(device);
        return device;
    }

    public void removeDevice(String id) {
        getDevice(id); // throws if not found
        repository.delete(id);
    }

    /**
     * Executes a command on a device and records it in the audit log.
     */
    public Device executeCommand(String id, String command, Map<String, Object> payload) {
        Device device = getDevice(id);
        String description = device.executeCommand(command, payload);
        repository.save(device);

        CommandHistory entry = new CommandHistory(device.getId(), device.getName(), description);
        repository.saveHistory(entry);

        return device;
    }

    public List<CommandHistory> getDeviceHistory(String deviceId) {
        getDevice(deviceId); // validate device exists
        return repository.findHistoryByDeviceId(deviceId);
    }

    public List<CommandHistory> getAllHistory() {
        return repository.findAllHistory();
    }

    public List<String> getLocations() {
        return repository.findAll().stream()
            .map(Device::getLocation)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * Resets all devices to their factory defaults.
     */
    public void resetAllDevices() {
        List<Device> devices = repository.findAll();
        devices.forEach(Device::resetToDefaults);
        repository.saveAll(devices);

        CommandHistory reset = new CommandHistory("system", "System", "All devices reset to factory defaults");
        repository.saveHistory(reset);
    }
}
