package com.smarthome.service;

import com.smarthome.domain.device.Device;
import com.smarthome.domain.device.ThermostatDevice;
import com.smarthome.repository.DataStore;
import com.smarthome.repository.IDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages the thermostat simulation — ticking ambient temperature toward desired.
 *
 * <p>At 1x speed: 1°F change every 5 seconds.
 * At Nx speed: 1°F change every (5000 / N) milliseconds.</p>
 */
@Service
public class SimulationService {

    private static final Logger log = LoggerFactory.getLogger(SimulationService.class);

    private final IDeviceRepository repository;
    private final AtomicInteger tickCounter = new AtomicInteger(0);
    private volatile int speedMultiplier = 1;
    private long simulationTimeMs = 0;

    public SimulationService(IDeviceRepository repository) {
        this.repository = repository;
        this.speedMultiplier = repository.getSimulationSettings().getSpeedMultiplier();
    }

    /**
     * Fires every 500ms. At 1x speed, thermostats update every 10 ticks (5 seconds).
     * At 10x speed, thermostats update every 1 tick (0.5 seconds).
     */
    @Scheduled(fixedDelay = 500)
    public void tick() {
        simulationTimeMs += (500L * speedMultiplier);

        int ticksNeeded = Math.max(1, 10 / speedMultiplier);
        int count = tickCounter.incrementAndGet();

        if (count >= ticksNeeded) {
            tickCounter.set(0);
            tickThermostats();
        }
    }

    private void tickThermostats() {
        List<Device> devices = List.copyOf(repository.findAll());
        devices.stream()
            .filter(d -> d instanceof ThermostatDevice)
            .map(d -> (ThermostatDevice) d)
            .forEach(t -> {
                t.simulationTick();
                repository.save(t);
            });
    }

    public void setSpeedMultiplier(int multiplier) {
        if (multiplier != 1 && multiplier != 2 && multiplier != 5 && multiplier != 10) {
            throw new IllegalArgumentException("Speed multiplier must be 1, 2, 5, or 10");
        }
        this.speedMultiplier = multiplier;
        DataStore.SimulationSettings settings = repository.getSimulationSettings();
        settings.setSpeedMultiplier(multiplier);
        repository.saveSimulationSettings(settings);
    }

    public int getSpeedMultiplier() {
        return speedMultiplier;
    }

    public long getSimulationTimeMs() {
        return simulationTimeMs;
    }

    public void setAmbientTemperature(String location, double temperature) {
        List<Device> devices = repository.findAll();
        devices.stream()
            .filter(d -> d instanceof ThermostatDevice)
            .filter(d -> d.getLocation().equalsIgnoreCase(location))
            .map(d -> (ThermostatDevice) d)
            .forEach(t -> {
                t.setAmbientTemperature(temperature);
                repository.save(t);
                log.info("Set ambient temperature of {} to {}°F", location, temperature);
            });
    }
}
