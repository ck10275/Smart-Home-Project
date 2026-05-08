package com.smarthome.controller;

import com.smarthome.dto.AmbientTemperatureRequest;
import com.smarthome.service.DeviceService;
import com.smarthome.service.SimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for simulation settings.
 * Thin controller — all logic delegated to service layer.
 */
@RestController
@RequestMapping("/api/simulation")
@Tag(name = "Simulation", description = "Simulation settings and control")
public class SimulationController {

    private final SimulationService simulationService;
    private final DeviceService deviceService;

    public SimulationController(SimulationService simulationService, DeviceService deviceService) {
        this.simulationService = simulationService;
        this.deviceService = deviceService;
    }

    @GetMapping("/status")
    @Operation(summary = "Get simulation status (speed, clock)")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
            "speedMultiplier", simulationService.getSpeedMultiplier(),
            "simulationTimeMs", simulationService.getSimulationTimeMs()
        ));
    }

    @PutMapping("/speed")
    @Operation(summary = "Set the simulation speed multiplier (1, 2, 5, or 10)")
    public ResponseEntity<Map<String, Object>> setSpeed(@RequestBody Map<String, Integer> body) {
        int multiplier = body.getOrDefault("multiplier", 1);
        simulationService.setSpeedMultiplier(multiplier);
        return ResponseEntity.ok(Map.of("speedMultiplier", multiplier));
    }

    @PutMapping("/locations/{location}/ambient-temperature")
    @Operation(summary = "Set the ambient temperature for a location")
    public ResponseEntity<Void> setAmbientTemperature(
            @PathVariable String location,
            @Valid @RequestBody AmbientTemperatureRequest request) {
        simulationService.setAmbientTemperature(location, request.getTemperature());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset all devices to factory defaults")
    public ResponseEntity<Void> resetAll() {
        deviceService.resetAllDevices();
        return ResponseEntity.ok().build();
    }
}
