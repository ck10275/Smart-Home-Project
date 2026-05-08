import axios from 'axios';
import type { Device, DeviceType, CommandHistory, SimulationStatus, RegisterDeviceRequest, CommandRequest } from '../models/types';

const BASE = '/api';
const api = axios.create({ baseURL: BASE });

export const deviceApi = {
  getAll: (params?: { location?: string; type?: DeviceType; on?: boolean; off?: boolean }) =>
    api.get<Device[]>('/devices', { params }).then(r => r.data),

  getById: (id: string) =>
    api.get<Device>(`/devices/${id}`).then(r => r.data),

  register: (req: RegisterDeviceRequest) =>
    api.post<Device>('/devices', req).then(r => r.data),

  remove: (id: string) =>
    api.delete(`/devices/${id}`),

  sendCommand: (id: string, req: CommandRequest) =>
    api.post<Device>(`/devices/${id}/commands`, req).then(r => r.data),

  getHistory: (id: string) =>
    api.get<CommandHistory[]>(`/devices/${id}/history`).then(r => r.data),

  getAllHistory: () =>
    api.get<CommandHistory[]>('/devices/history').then(r => r.data),

  getLocations: () =>
    api.get<string[]>('/devices/locations').then(r => r.data),
};

export const simulationApi = {
  getStatus: () =>
    api.get<SimulationStatus>('/simulation/status').then(r => r.data),

  setSpeed: (multiplier: number) =>
    api.put('/simulation/speed', { multiplier }),

  setAmbientTemperature: (location: string, temperature: number) =>
    api.put(`/simulation/locations/${encodeURIComponent(location)}/ambient-temperature`, { temperature }),

  reset: () =>
    api.post('/simulation/reset'),
};
