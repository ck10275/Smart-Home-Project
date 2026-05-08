import { useState, useEffect, useCallback } from 'react';
import { deviceApi } from '../services/api';
import type { Device, DeviceType } from '../models/types';

export function useDevices(filters: { location?: string; type?: DeviceType; on?: boolean; off?: boolean }) {
  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      setLoading(true);
      const data = await deviceApi.getAll(filters);
      setDevices(data);
      setError(null);
    } catch (e: unknown) {
      setError('Failed to load devices');
    } finally {
      setLoading(false);
    }
  }, [filters.location, filters.type, filters.on, filters.off]);

  useEffect(() => {
    load();
    const interval = setInterval(load, 3000); // Poll every 3s for thermostat updates
    return () => clearInterval(interval);
  }, [load]);

  return { devices, loading, error, refresh: load };
}
