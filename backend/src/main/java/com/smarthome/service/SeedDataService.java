package com.smarthome.service;

import com.smarthome.domain.device.*;
import com.smarthome.repository.IDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds the application with default device data on first run.
 * Only seeds if the device store is empty (idempotent).
 */
@Component
public class SeedDataService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataService.class);

    private final IDeviceRepository repository;

    public SeedDataService(IDeviceRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!repository.findAll().isEmpty()) {
            log.info("Data already seeded, skipping.");
            return;
        }

        log.info("Seeding initial device data...");

        // Living Room
        LightDevice lrOverhead = new LightDevice("Living Room Overhead", "Living Room");
        lrOverhead.setBrightness(80);
        lrOverhead.setColor("#FFF8DC");

        LightDevice lrFloorLamp = new LightDevice("Floor Lamp", "Living Room");
        lrFloorLamp.setBrightness(60);
        lrFloorLamp.setColor("#FF8800");

        FanDevice lrFan = new FanDevice("Ceiling Fan", "Living Room");

        ThermostatDevice lrThermostat = new ThermostatDevice("Main Thermostat", "Living Room");
        lrThermostat.setDesiredTemperature(72.0);
        lrThermostat.setAmbientTemperature(68.0);
        lrThermostat.setMode(ThermostatDevice.ThermostatMode.AUTO);

        DoorLockDevice frontDoor = new DoorLockDevice("Front Door", "Living Room");

        // Kitchen
        LightDevice kitchenLight = new LightDevice("Kitchen Overhead", "Kitchen");
        kitchenLight.setBrightness(100);
        kitchenLight.setColor("#FFFFFF");

        LightDevice kitchenUnder = new LightDevice("Under-Cabinet Lights", "Kitchen");
        kitchenUnder.setBrightness(50);
        kitchenUnder.setColor("#FFD700");

        FanDevice kitchenFan = new FanDevice("Range Hood Fan", "Kitchen");

        DoorLockDevice backDoor = new DoorLockDevice("Back Door", "Kitchen");

        // Master Bedroom
        LightDevice bedroomLight = new LightDevice("Bedroom Overhead", "Master Bedroom");
        bedroomLight.setBrightness(40);
        bedroomLight.setColor("#FF4500");

        FanDevice bedroomFan = new FanDevice("Bedroom Fan", "Master Bedroom");

        ThermostatDevice bedroomThermostat = new ThermostatDevice("Bedroom Thermostat", "Master Bedroom");
        bedroomThermostat.setDesiredTemperature(68.0);
        bedroomThermostat.setAmbientTemperature(75.0);
        bedroomThermostat.setMode(ThermostatDevice.ThermostatMode.COOL);

        DoorLockDevice bedroomDoor = new DoorLockDevice("Bedroom Door Lock", "Master Bedroom");

        // Garage
        LightDevice garageLights = new LightDevice("Garage Lights", "Garage");
        FanDevice garageFan = new FanDevice("Garage Ventilation Fan", "Garage");
        DoorLockDevice garageDoor = new DoorLockDevice("Garage Door Lock", "Garage");

        repository.save(lrOverhead);
        repository.save(lrFloorLamp);
        repository.save(lrFan);
        repository.save(lrThermostat);
        repository.save(frontDoor);
        repository.save(kitchenLight);
        repository.save(kitchenUnder);
        repository.save(kitchenFan);
        repository.save(backDoor);
        repository.save(bedroomLight);
        repository.save(bedroomFan);
        repository.save(bedroomThermostat);
        repository.save(bedroomDoor);
        repository.save(garageLights);
        repository.save(garageFan);
        repository.save(garageDoor);

        log.info("Seeded {} devices across 4 locations.", 16);
    }
}
