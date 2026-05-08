package com.smarthome.domain;

import com.smarthome.domain.device.*;
import com.smarthome.exception.InvalidTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("State Machine Tests")
class StateMachineTest {

    // --- Light ---

    @Test
    @DisplayName("Light: Off → On on power_on")
    void lightPowerOn() {
        LightDevice light = new LightDevice("Test Light", "Room");
        light.executeCommand("power_on", Map.of());
        assertThat(light.getState()).isEqualTo(LightDevice.LightState.ON);
        assertThat(light.isOn()).isTrue();
    }

    @Test
    @DisplayName("Light: On → Off on power_off")
    void lightPowerOff() {
        LightDevice light = new LightDevice("Test Light", "Room");
        light.executeCommand("power_on", Map.of());
        light.executeCommand("power_off", Map.of());
        assertThat(light.getState()).isEqualTo(LightDevice.LightState.OFF);
        assertThat(light.isOn()).isFalse();
    }

    @Test
    @DisplayName("Light: Cannot set brightness when Off")
    void lightCannotSetBrightnessWhenOff() {
        LightDevice light = new LightDevice("Test Light", "Room");
        assertThatThrownBy(() -> light.executeCommand("set_brightness", Map.of("brightness", 50)))
            .isInstanceOf(InvalidTransitionException.class);
    }

    @Test
    @DisplayName("Light: Brightness boundary — 10 and 100 are valid")
    void lightBrightnessValidRange() {
        LightDevice light = new LightDevice("Test Light", "Room");
        light.executeCommand("power_on", Map.of());
        light.executeCommand("set_brightness", Map.of("brightness", 10));
        assertThat(light.getBrightness()).isEqualTo(10);
        light.executeCommand("set_brightness", Map.of("brightness", 100));
        assertThat(light.getBrightness()).isEqualTo(100);
    }

    @Test
    @DisplayName("Light: Brightness 9 is rejected")
    void lightBrightnessOutOfRangeLow() {
        LightDevice light = new LightDevice("Test Light", "Room");
        light.executeCommand("power_on", Map.of());
        assertThatThrownBy(() -> light.executeCommand("set_brightness", Map.of("brightness", 9)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Light: Brightness 101 is rejected")
    void lightBrightnessOutOfRangeHigh() {
        LightDevice light = new LightDevice("Test Light", "Room");
        light.executeCommand("power_on", Map.of());
        assertThatThrownBy(() -> light.executeCommand("set_brightness", Map.of("brightness", 101)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Light: Settings retained after power cycle")
    void lightSettingsRetained() {
        LightDevice light = new LightDevice("Test Light", "Room");
        light.executeCommand("power_on", Map.of());
        light.executeCommand("set_brightness", Map.of("brightness", 75));
        light.executeCommand("set_color", Map.of("color", "#FF8800"));
        light.executeCommand("power_off", Map.of());
        assertThat(light.getBrightness()).isEqualTo(75);
        assertThat(light.getColor()).isEqualTo("#FF8800");
    }

    // --- Fan ---

    @Test
    @DisplayName("Fan: Powers on at Medium by default")
    void fanPowerOnDefault() {
        FanDevice fan = new FanDevice("Test Fan", "Room");
        fan.executeCommand("power_on", Map.of());
        assertThat(fan.getState()).isEqualTo(FanDevice.FanState.MEDIUM);
    }

    @Test
    @DisplayName("Fan: Cannot set speed when off")
    void fanCannotSetSpeedWhenOff() {
        FanDevice fan = new FanDevice("Test Fan", "Room");
        assertThatThrownBy(() -> fan.executeCommand("set_speed_high", Map.of()))
            .isInstanceOf(InvalidTransitionException.class);
    }

    @Test
    @DisplayName("Fan: All speed transitions work when on")
    void fanSpeedTransitions() {
        FanDevice fan = new FanDevice("Test Fan", "Room");
        fan.executeCommand("power_on", Map.of());
        fan.executeCommand("set_speed_high", Map.of());
        assertThat(fan.getState()).isEqualTo(FanDevice.FanState.HIGH);
        fan.executeCommand("set_speed_low", Map.of());
        assertThat(fan.getState()).isEqualTo(FanDevice.FanState.LOW);
        fan.executeCommand("set_speed_medium", Map.of());
        assertThat(fan.getState()).isEqualTo(FanDevice.FanState.MEDIUM);
    }

    // --- Thermostat ---

    @Test
    @DisplayName("Thermostat: Off → Idle on power_on")
    void thermostatPowerOn() {
        ThermostatDevice t = new ThermostatDevice("Thermostat", "Room");
        t.executeCommand("power_on", Map.of());
        assertThat(t.getState()).isIn(
            ThermostatDevice.ThermostatState.IDLE,
            ThermostatDevice.ThermostatState.HEATING,
            ThermostatDevice.ThermostatState.COOLING
        );
    }

    @Test
    @DisplayName("Thermostat: Transitions to Heating when ambient < desired (Heat mode)")
    void thermostatHeatingTransition() {
        ThermostatDevice t = new ThermostatDevice("Thermostat", "Room");
        t.setMode(ThermostatDevice.ThermostatMode.HEAT);
        t.setDesiredTemperature(75.0);
        t.setAmbientTemperature(65.0);
        t.executeCommand("power_on", Map.of());
        assertThat(t.getState()).isEqualTo(ThermostatDevice.ThermostatState.HEATING);
    }

    @Test
    @DisplayName("Thermostat: Desired temp 60 and 80 are valid")
    void thermostatTempBoundary() {
        ThermostatDevice t = new ThermostatDevice("Thermostat", "Room");
        t.executeCommand("power_on", Map.of());
        t.executeCommand("set_desired_temperature", Map.of("temperature", 60.0));
        assertThat(t.getDesiredTemperature()).isEqualTo(60.0);
        t.executeCommand("set_desired_temperature", Map.of("temperature", 80.0));
        assertThat(t.getDesiredTemperature()).isEqualTo(80.0);
    }

    @Test
    @DisplayName("Thermostat: Desired temp 59 is rejected")
    void thermostatTempTooLow() {
        ThermostatDevice t = new ThermostatDevice("Thermostat", "Room");
        t.executeCommand("power_on", Map.of());
        assertThatThrownBy(() -> t.executeCommand("set_desired_temperature", Map.of("temperature", 59.0)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Thermostat: Desired temp 81 is rejected")
    void thermostatTempTooHigh() {
        ThermostatDevice t = new ThermostatDevice("Thermostat", "Room");
        t.executeCommand("power_on", Map.of());
        assertThatThrownBy(() -> t.executeCommand("set_desired_temperature", Map.of("temperature", 81.0)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Thermostat: Transitions to Idle when ambient reaches desired")
    void thermostatReachesIdle() {
        ThermostatDevice t = new ThermostatDevice("Thermostat", "Room");
        t.setMode(ThermostatDevice.ThermostatMode.HEAT);
        t.setDesiredTemperature(72.0);
        t.setAmbientTemperature(71.0);
        t.executeCommand("power_on", Map.of());
        assertThat(t.getState()).isEqualTo(ThermostatDevice.ThermostatState.HEATING);
        t.simulationTick(); // ambient goes to 72
        assertThat(t.getState()).isEqualTo(ThermostatDevice.ThermostatState.IDLE);
    }

    // --- Door Lock ---

    @Test
    @DisplayName("DoorLock: Defaults to Locked")
    void doorLockDefaultLocked() {
        DoorLockDevice lock = new DoorLockDevice("Front Door", "Hallway");
        assertThat(lock.getState()).isEqualTo(DoorLockDevice.LockState.LOCKED);
    }

    @Test
    @DisplayName("DoorLock: Is always on")
    void doorLockAlwaysOn() {
        DoorLockDevice lock = new DoorLockDevice("Front Door", "Hallway");
        assertThat(lock.isOn()).isTrue();
        lock.executeCommand("unlock", Map.of());
        assertThat(lock.isOn()).isTrue();
    }

    @Test
    @DisplayName("DoorLock: Locked → Unlocked → Locked")
    void doorLockToggle() {
        DoorLockDevice lock = new DoorLockDevice("Front Door", "Hallway");
        lock.executeCommand("unlock", Map.of());
        assertThat(lock.getState()).isEqualTo(DoorLockDevice.LockState.UNLOCKED);
        lock.executeCommand("lock", Map.of());
        assertThat(lock.getState()).isEqualTo(DoorLockDevice.LockState.LOCKED);
    }

    @Test
    @DisplayName("DoorLock: Cannot lock when already locked")
    void doorLockCannotLockWhenLocked() {
        DoorLockDevice lock = new DoorLockDevice("Front Door", "Hallway");
        assertThatThrownBy(() -> lock.executeCommand("lock", Map.of()))
            .isInstanceOf(InvalidTransitionException.class);
    }

    // --- Factory ---

    @Test
    @DisplayName("Factory: Creates all device types")
    void factoryCreatesAllTypes() {
        DeviceFactory factory = new DeviceFactory();
        assertThat(factory.create(DeviceType.LIGHT, "Light", "Room")).isInstanceOf(LightDevice.class);
        assertThat(factory.create(DeviceType.FAN, "Fan", "Room")).isInstanceOf(FanDevice.class);
        assertThat(factory.create(DeviceType.THERMOSTAT, "Thermostat", "Room")).isInstanceOf(ThermostatDevice.class);
        assertThat(factory.create(DeviceType.DOOR_LOCK, "Lock", "Room")).isInstanceOf(DoorLockDevice.class);
    }
}
