package com.smarthome.controller;

import com.smarthome.domain.device.CommandHistory;
import com.smarthome.domain.device.Device;
import com.smarthome.domain.device.DeviceType;
import com.smarthome.dto.CommandRequest;
import com.smarthome.dto.RegisterDeviceRequest;
import com.smarthome.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for device management.
 * Controllers are thin — all business logic is delegated to {@link DeviceService}.
 */
@RestController
@RequestMapping("/api/devices")
@Tag(name = "Devices", description = "Device management and control endpoints")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    @Operation(summary = "List all devices with optional filtering")
    public ResponseEntity<List<Device>> listDevices(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) DeviceType type,
            @RequestParam(required = false) Boolean on,
            @RequestParam(required = false) Boolean off) {
        return ResponseEntity.ok(deviceService.getAllDevices(location, type, on, off));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific device by ID")
    public ResponseEntity<Device> getDevice(@PathVariable String id) {
        return ResponseEntity.ok(deviceService.getDevice(id));
    }

    @PostMapping
    @Operation(summary = "Register a new device")
    public ResponseEntity<Device> registerDevice(@Valid @RequestBody RegisterDeviceRequest request) {
        Device device = deviceService.registerDevice(
            request.getType(), request.getName(), request.getLocation()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(device);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a device")
    public ResponseEntity<Void> removeDevice(@PathVariable String id) {
        deviceService.removeDevice(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/commands")
    @Operation(summary = "Send a command to a device")
    public ResponseEntity<Device> executeCommand(
            @PathVariable String id,
            @Valid @RequestBody CommandRequest request) {
        Device updated = deviceService.executeCommand(id, request.getCommand(), request.getPayload());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get command history for a device")
    public ResponseEntity<List<CommandHistory>> getHistory(@PathVariable String id) {
        return ResponseEntity.ok(deviceService.getDeviceHistory(id));
    }

    @GetMapping("/history")
    @Operation(summary = "Get all command history")
    public ResponseEntity<List<CommandHistory>> getAllHistory() {
        return ResponseEntity.ok(deviceService.getAllHistory());
    }

    @GetMapping("/locations")
    @Operation(summary = "Get all unique locations")
    public ResponseEntity<List<String>> getLocations() {
        return ResponseEntity.ok(deviceService.getLocations());
    }
}
