export type DeviceType = 'LIGHT' | 'FAN' | 'THERMOSTAT' | 'DOOR_LOCK';

export type LightState = 'OFF' | 'ON';
export type FanState = 'OFF' | 'LOW' | 'MEDIUM' | 'HIGH';
export type ThermostatState = 'OFF' | 'IDLE' | 'HEATING' | 'COOLING';
export type LockState = 'LOCKED' | 'UNLOCKED';
export type ThermostatMode = 'HEAT' | 'COOL' | 'AUTO';
export type FanSpeed = 'LOW' | 'MEDIUM' | 'HIGH';

export interface BaseDevice {
  id: string;
  name: string;
  location: string;
  type: DeviceType;
}

export interface LightDevice extends BaseDevice {
  type: 'LIGHT';
  state: LightState;
  brightness: number;
  color: string;
}

export interface FanDevice extends BaseDevice {
  type: 'FAN';
  state: FanState;
  lastSpeed: FanSpeed;
}

export interface ThermostatDevice extends BaseDevice {
  type: 'THERMOSTAT';
  state: ThermostatState;
  mode: ThermostatMode;
  desiredTemperature: number;
  ambientTemperature: number;
}

export interface DoorLockDevice extends BaseDevice {
  type: 'DOOR_LOCK';
  state: LockState;
}

export type Device = LightDevice | FanDevice | ThermostatDevice | DoorLockDevice;

export interface CommandHistory {
  id: string;
  deviceId: string;
  deviceName: string;
  operation: string;
  timestamp: string;
}

export interface SimulationStatus {
  speedMultiplier: number;
  simulationTimeMs: number;
}

export interface RegisterDeviceRequest {
  type: DeviceType;
  name: string;
  location: string;
}

export interface CommandRequest {
  command: string;
  payload?: Record<string, unknown>;
}
