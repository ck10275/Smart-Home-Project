package com.smarthome.domain.device;

/**
 * Supported device categories in the Smart Home Simulator.
 * Powered devices have an Off/On power state. Latch devices are always energized.
 */
public enum DeviceType {
    LIGHT,
    FAN,
    THERMOSTAT,
    DOOR_LOCK
}
