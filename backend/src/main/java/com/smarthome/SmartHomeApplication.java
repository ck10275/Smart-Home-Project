package com.smarthome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Smart Home Simulator — Spring Boot Application Entry Point.
 *
 * <p>Architecture:
 * Controller → Service → Repository (interface) → JsonDeviceRepository (JSON file)
 *
 * Design Patterns applied:
 * - State: Each device type has a formal StateMachine with defined transitions
 * - Factory: DeviceFactory creates devices by type (OCP compliant)
 * - Strategy: ThermostatModeStrategy for Heat/Cool/Auto mode behavior
 * - Repository: IDeviceRepository decouples service from persistence
 * </p>
 */
@SpringBootApplication
@EnableScheduling
public class SmartHomeApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartHomeApplication.class, args);
    }
}
