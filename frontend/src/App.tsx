import React, { useState, useEffect, useCallback } from 'react';
import { deviceApi, simulationApi } from './services/api';
import type {
  Device, LightDevice, FanDevice, ThermostatDevice, DoorLockDevice,
  DeviceType, CommandHistory, SimulationStatus, RegisterDeviceRequest
} from './models/types';
import './App.css';

// ─── Icons ────────────────────────────────────────────────────────────────────

const IconLight = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <path d="M9 21h6M12 3a6 6 0 0 1 4.243 10.243A4 4 0 0 0 14 17H10a4 4 0 0 0-2.243-3.757A6 6 0 0 1 12 3z"/>
  </svg>
);

const IconFan = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <path d="M12 12m-1 0a1 1 0 1 0 2 0a1 1 0 1 0 -2 0"/>
    <path d="M12 3c-1.333 1.333-2 2.667-2 4s.667 2 2 2c1.333 0 2-.667 2-2s-.667-2.667-2-4z"/>
    <path d="M3 12c1.333 1.333 2.667 2 4 2s2-.667 2-2-.667-2-2-2-2.667.667-4 2z"/>
    <path d="M12 21c1.333-1.333 2-2.667 2-4s-.667-2-2-2c-1.333 0-2 .667-2 2s.667 2.667 2 4z"/>
    <path d="M21 12c-1.333-1.333-2.667-2-4-2s-2 .667-2 2 .667 2 2 2 2.667-.667 4-2z"/>
  </svg>
);

const IconThermo = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <path d="M14 14.76V3.5a2.5 2.5 0 0 0-5 0v11.26a4.5 4.5 0 1 0 5 0z"/>
  </svg>
);

const IconLock = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
    <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
  </svg>
);

const IconUnlock = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
    <path d="M7 11V7a5 5 0 0 1 9.9-1"/>
  </svg>
);

const IconSettings = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="3"/>
    <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/>
  </svg>
);

const IconPlus = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
    <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
  </svg>
);

const IconX = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
    <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
  </svg>
);

const IconHistory = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="12 8 12 12 14 14"/>
    <path d="M3.05 11a9 9 0 1 0 .5-4.5"/>
    <polyline points="3 3 3 11 11 11"/>
  </svg>
);

// ─── Device Icon by type ──────────────────────────────────────────────────────

function DeviceIcon({ type }: { type: DeviceType }) {
  switch (type) {
    case 'LIGHT': return <IconLight />;
    case 'FAN': return <IconFan />;
    case 'THERMOSTAT': return <IconThermo />;
    case 'DOOR_LOCK': return <IconLock />;
  }
}

// ─── Device color theme ───────────────────────────────────────────────────────

function deviceTheme(device: Device) {
  if (!isOn(device)) return { color: 'var(--off)', glow: 'transparent', label: 'Off' };
  switch (device.type) {
    case 'LIGHT': return { color: 'var(--light-on)', glow: 'var(--light-glow)', label: 'On' };
    case 'FAN': return { color: 'var(--fan-on)', glow: 'var(--fan-glow)', label: (device as FanDevice).state };
    case 'THERMOSTAT': {
      const t = device as ThermostatDevice;
      if (t.state === 'HEATING') return { color: 'var(--heat)', glow: 'var(--heat-glow)', label: 'Heating' };
      if (t.state === 'COOLING') return { color: 'var(--cool)', glow: 'var(--cool-glow)', label: 'Cooling' };
      return { color: 'var(--text-muted)', glow: 'transparent', label: t.state };
    }
    case 'DOOR_LOCK': {
      const l = device as DoorLockDevice;
      return { color: 'var(--lock)', glow: 'var(--lock-glow)', label: l.state === 'LOCKED' ? 'Locked' : 'Unlocked' };
    }
  }
}

function isOn(device: Device): boolean {
  switch (device.type) {
    case 'LIGHT': return (device as LightDevice).state === 'ON';
    case 'FAN': return (device as FanDevice).state !== 'OFF';
    case 'THERMOSTAT': {
      const s = (device as ThermostatDevice).state;
      return s === 'HEATING' || s === 'COOLING';
    }
    case 'DOOR_LOCK': return true;
  }
}

// ─── Light Controls ────────────────────────────────────────────────────────────

function LightControls({ device, onCommand }: { device: LightDevice; onCommand: (cmd: string, payload?: Record<string, unknown>) => void }) {
  const on = device.state === 'ON';
  return (
    <div className="controls">
      <button className={`toggle-btn ${on ? 'active' : ''}`} onClick={() => onCommand(on ? 'power_off' : 'power_on')}>
        {on ? 'Turn Off' : 'Turn On'}
      </button>
      {on && (
        <>
          <div className="control-row">
            <span className="control-label">Brightness</span>
            <span className="control-value">{device.brightness}%</span>
          </div>
          <input type="range" min={10} max={100} value={device.brightness}
            onChange={e => onCommand('set_brightness', { brightness: +e.target.value })}
            className="slider" style={{ accentColor: device.color }} />
          <div className="control-row">
            <span className="control-label">Color</span>
            <input type="color" value={device.color}
              onChange={e => onCommand('set_color', { color: e.target.value })}
              className="color-picker" />
          </div>
        </>
      )}
    </div>
  );
}

// ─── Fan Controls ──────────────────────────────────────────────────────────────

function FanControls({ device, onCommand }: { device: FanDevice; onCommand: (cmd: string) => void }) {
  const on = device.state !== 'OFF';
  const speeds = ['LOW', 'MEDIUM', 'HIGH'] as const;
  return (
    <div className="controls">
      <button className={`toggle-btn ${on ? 'active' : ''}`} onClick={() => onCommand(on ? 'power_off' : 'power_on')}>
        {on ? 'Turn Off' : 'Turn On'}
      </button>
      {on && (
        <div className="speed-btns">
          {speeds.map(s => (
            <button key={s} className={`speed-btn ${device.state === s ? 'active' : ''}`}
              onClick={() => onCommand(`set_speed_${s.toLowerCase()}`)}>
              {s}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

// ─── Thermostat Controls ───────────────────────────────────────────────────────

function ThermostatControls({ device, onCommand }: { device: ThermostatDevice; onCommand: (cmd: string, payload?: Record<string, unknown>) => void }) {
  const on = device.state !== 'OFF';
  const modes = ['HEAT', 'COOL', 'AUTO'] as const;
  return (
    <div className="controls">
      <button className={`toggle-btn ${on ? 'active' : ''}`} onClick={() => onCommand(on ? 'power_off' : 'power_on')}>
        {on ? 'Turn Off' : 'Turn On'}
      </button>
      {on && (
        <>
          <div className="temp-display">
            <div className="temp-row">
              <span className="temp-label">Ambient</span>
              <span className="temp-value ambient">{device.ambientTemperature.toFixed(1)}°F</span>
            </div>
            <div className="temp-row">
              <span className="temp-label">Target</span>
              <span className="temp-value target">{device.desiredTemperature}°F</span>
            </div>
          </div>
          <div className="control-row">
            <button className="temp-adj" onClick={() => onCommand('set_desired_temperature', { temperature: Math.max(60, device.desiredTemperature - 1) })}>−</button>
            <input type="range" min={60} max={80} value={device.desiredTemperature}
              onChange={e => onCommand('set_desired_temperature', { temperature: +e.target.value })}
              className="slider" style={{ accentColor: device.state === 'HEATING' ? 'var(--heat)' : 'var(--cool)' }} />
            <button className="temp-adj" onClick={() => onCommand('set_desired_temperature', { temperature: Math.min(80, device.desiredTemperature + 1) })}>+</button>
          </div>
          <div className="mode-btns">
            {modes.map(m => (
              <button key={m} className={`mode-btn ${device.mode === m ? 'active' : ''}`}
                onClick={() => onCommand('set_mode', { mode: m })}>
                {m}
              </button>
            ))}
          </div>
        </>
      )}
    </div>
  );
}

// ─── Door Lock Controls ────────────────────────────────────────────────────────

function DoorLockControls({ device, onCommand }: { device: DoorLockDevice; onCommand: (cmd: string) => void }) {
  const locked = device.state === 'LOCKED';
  return (
    <div className="controls">
      <button className={`lock-btn ${locked ? 'locked' : 'unlocked'}`}
        onClick={() => onCommand(locked ? 'unlock' : 'lock')}>
        <span className="lock-icon">{locked ? <IconLock /> : <IconUnlock />}</span>
        {locked ? 'Unlock' : 'Lock'}
      </button>
    </div>
  );
}

// ─── Device Card ───────────────────────────────────────────────────────────────

function DeviceCard({ device, onRefresh, onDelete }: { device: Device; onRefresh: () => void; onDelete: (id: string) => void }) {
  const theme = deviceTheme(device);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [showHistory, setShowHistory] = useState(false);
  const [history, setHistory] = useState<CommandHistory[]>([]);

  const handleCommand = useCallback(async (cmd: string, payload?: Record<string, unknown>) => {
    setBusy(true);
    setError('');
    try {
      await deviceApi.sendCommand(device.id, { command: cmd, payload });
      onRefresh();
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { detail?: string } } })?.response?.data?.detail || 'Command failed';
      setError(msg);
    } finally {
      setBusy(false);
    }
  }, [device.id, onRefresh]);

  const loadHistory = useCallback(async () => {
    const data = await deviceApi.getHistory(device.id);
    setHistory(data.slice(-10).reverse());
    setShowHistory(true);
  }, [device.id]);

  const handleDelete = useCallback(async () => {
    if (!confirm(`Remove "${device.name}"?`)) return;
    await deviceApi.remove(device.id);
    onRefresh();
    onDelete(device.id);
  }, [device.id, device.name, onRefresh, onDelete]);

  return (
    <div className={`device-card ${isOn(device) ? 'on' : ''}`} style={{ '--glow': theme.glow } as React.CSSProperties}>
      <div className="card-header">
        <div className="card-icon" style={{ color: theme.color }}>
          <DeviceIcon type={device.type} />
        </div>
        <div className="card-meta">
          <span className="device-name">{device.name}</span>
          <span className="device-status" style={{ color: theme.color }}>{theme.label}</span>
        </div>
        <div className="card-actions">
          <button className="icon-btn" onClick={loadHistory} title="History"><IconHistory /></button>
          <button className="icon-btn danger" onClick={handleDelete} title="Remove"><IconX /></button>
        </div>
      </div>

      {error && <div className="error-msg">{error}</div>}

      <div className={busy ? 'controls-wrap busy' : 'controls-wrap'}>
        {device.type === 'LIGHT' && <LightControls device={device as LightDevice} onCommand={handleCommand} />}
        {device.type === 'FAN' && <FanControls device={device as FanDevice} onCommand={handleCommand} />}
        {device.type === 'THERMOSTAT' && <ThermostatControls device={device as ThermostatDevice} onCommand={handleCommand} />}
        {device.type === 'DOOR_LOCK' && <DoorLockControls device={device as DoorLockDevice} onCommand={handleCommand} />}
      </div>

      {showHistory && (
        <div className="history-panel">
          <div className="history-header">
            <span>Recent Activity</span>
            <button className="icon-btn" onClick={() => setShowHistory(false)}><IconX /></button>
          </div>
          {history.length === 0 ? <p className="no-history">No history yet</p> : (
            <ul className="history-list">
              {history.map(h => (
                <li key={h.id}>
                  <span className="history-op">{h.operation}</span>
                  <span className="history-ts">{new Date(h.timestamp).toLocaleTimeString()}</span>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}

// ─── Register Modal ────────────────────────────────────────────────────────────

function RegisterModal({ onClose, onRegistered, locations }: {
  onClose: () => void;
  onRegistered: () => void;
  locations: string[];
}) {
  const [form, setForm] = useState<RegisterDeviceRequest>({ type: 'LIGHT', name: '', location: '' });
  const [newLoc, setNewLoc] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const location = newLoc || form.location;

  const handleSubmit = async () => {
    if (!form.name.trim() || !location.trim()) {
      setError('Name and location are required');
      return;
    }
    setBusy(true);
    try {
      await deviceApi.register({ ...form, location });
      onRegistered();
      onClose();
    } catch (e: unknown) {
      setError((e as { response?: { data?: { detail?: string } } })?.response?.data?.detail || 'Registration failed');
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <span>Add Device</span>
          <button className="icon-btn" onClick={onClose}><IconX /></button>
        </div>
        <div className="modal-body">
          <label>Type
            <select value={form.type} onChange={e => setForm(f => ({ ...f, type: e.target.value as DeviceType }))}>
              <option value="LIGHT">💡 Light</option>
              <option value="FAN">🌀 Fan</option>
              <option value="THERMOSTAT">🌡️ Thermostat</option>
              <option value="DOOR_LOCK">🔒 Door Lock</option>
            </select>
          </label>
          <label>Name
            <input value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} placeholder="e.g. Bedside Lamp" />
          </label>
          <label>Location
            <select value={newLoc ? '__new__' : form.location} onChange={e => {
              if (e.target.value === '__new__') setNewLoc(' ');
              else { setForm(f => ({ ...f, location: e.target.value })); setNewLoc(''); }
            }}>
              <option value="">Select location…</option>
              {locations.map(l => <option key={l} value={l}>{l}</option>)}
              <option value="__new__">+ New location…</option>
            </select>
          </label>
          {newLoc !== '' && (
            <label>New Location Name
              <input value={newLoc.trim()} onChange={e => setNewLoc(e.target.value)} placeholder="e.g. Office" autoFocus />
            </label>
          )}
          {error && <div className="error-msg">{error}</div>}
        </div>
        <div className="modal-footer">
          <button className="btn-secondary" onClick={onClose}>Cancel</button>
          <button className="btn-primary" onClick={handleSubmit} disabled={busy}>
            {busy ? 'Adding…' : 'Add Device'}
          </button>
        </div>
      </div>
    </div>
  );
}

// ─── Simulation Settings Panel ─────────────────────────────────────────────────

function SimulationPanel({ locations, onClose, onReset }: {
  locations: string[];
  onClose: () => void;
  onReset: () => void;
}) {
  const [status, setStatus] = useState<SimulationStatus>({ speedMultiplier: 1, simulationTimeMs: 0 });
  const [temps, setTemps] = useState<Record<string, number>>({});
  const [msg, setMsg] = useState('');

  useEffect(() => {
    simulationApi.getStatus().then(setStatus);
  }, []);

  const handleSpeed = async (m: number) => {
    await simulationApi.setSpeed(m);
    setStatus(s => ({ ...s, speedMultiplier: m }));
  };

  const handleTemp = async (loc: string) => {
    const t = temps[loc];
    if (t === undefined) return;
    await simulationApi.setAmbientTemperature(loc, t);
    setMsg(`Ambient temperature in ${loc} set to ${t}°F`);
    setTimeout(() => setMsg(''), 2000);
  };

  const handleReset = async () => {
    if (!confirm('Reset all devices to factory defaults?')) return;
    await simulationApi.reset();
    onReset();
    onClose();
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal sim-modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <span>Simulation Settings</span>
          <button className="icon-btn" onClick={onClose}><IconX /></button>
        </div>
        <div className="modal-body">
          <div className="sim-section">
            <h3>Speed Multiplier</h3>
            <div className="speed-btns">
              {[1, 2, 5, 10].map(m => (
                <button key={m} className={`speed-btn ${status.speedMultiplier === m ? 'active' : ''}`}
                  onClick={() => handleSpeed(m)}>{m}x</button>
              ))}
            </div>
            <p className="hint">At 1x: thermostats change 1°F every 5 seconds.</p>
          </div>

          <div className="sim-section">
            <h3>Ambient Temperature by Location</h3>
            {locations.map(loc => (
              <div key={loc} className="ambient-row">
                <span className="ambient-loc">{loc}</span>
                <input type="number" min={0} max={120}
                  value={temps[loc] ?? ''}
                  placeholder="°F"
                  onChange={e => setTemps(t => ({ ...t, [loc]: +e.target.value }))}
                  className="ambient-input" />
                <button className="btn-primary small" onClick={() => handleTemp(loc)}>Set</button>
              </div>
            ))}
            {msg && <div className="success-msg">{msg}</div>}
          </div>

          <div className="sim-section danger-zone">
            <h3>Danger Zone</h3>
            <button className="btn-danger" onClick={handleReset}>Reset All Devices</button>
          </div>
        </div>
      </div>
    </div>
  );
}

// ─── Main App ─────────────────────────────────────────────────────────────────

type FilterState = { location: string; type: DeviceType | ''; onlyOn: boolean; onlyOff: boolean };

export default function App() {
  const [devices, setDevices] = useState<Device[]>([]);
  const [locations, setLocations] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState<FilterState>({ location: '', type: '', onlyOn: false, onlyOff: false });
  const [showRegister, setShowRegister] = useState(false);
  const [showSim, setShowSim] = useState(false);
  const [simStatus, setSimStatus] = useState<SimulationStatus>({ speedMultiplier: 1, simulationTimeMs: 0 });
  const [deletedIds, setDeletedIds] = useState<Set<string>>(new Set());

  const load = useCallback(async () => {
    try {
      const params: Record<string, unknown> = {};
      if (filters.location) params.location = filters.location;
      if (filters.type) params.type = filters.type;
      if (filters.onlyOn) params.on = true;
      if (filters.onlyOff) params.off = true;
      const [devs, locs, sim] = await Promise.all([
        deviceApi.getAll(params as { location?: string; type?: DeviceType; on?: boolean; off?: boolean }),
        deviceApi.getLocations(),
        simulationApi.getStatus(),
      ]);
      setDevices(devs);
      setLocations(locs);
      setSimStatus(sim);
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    load();
    const id = setInterval(load, 3000);
    return () => clearInterval(id);
  }, [load]);

  // Group devices by location
  const grouped = devices.reduce((acc, d) => {
    if (!acc[d.location]) acc[d.location] = [];
    acc[d.location].push(d);
    return acc;
  }, {} as Record<string, Device[]>);

  const formatClock = (ms: number) => {
    const s = Math.floor(ms / 1000) % 60;
    const m = Math.floor(ms / 60000) % 60;
    const h = Math.floor(ms / 3600000);
    return `${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`;
  };

  return (
    <div className="app">
      {/* Header */}
      <header className="header">
        <div className="header-left">
          <h1 className="app-title">Smart<em>Home</em></h1>
          <span className="subtitle">Simulator</span>
        </div>
        <div className="header-center">
          <div className="sim-clock">
            <span className="clock-label">SIM</span>
            <span className="clock-value">{formatClock(simStatus.simulationTimeMs)}</span>
            <span className="clock-speed">{simStatus.speedMultiplier}×</span>
          </div>
        </div>
        <div className="header-right">
          <button className="btn-icon" onClick={() => setShowRegister(true)} title="Add device">
            <IconPlus />
          </button>
          <button className="btn-icon" onClick={() => setShowSim(true)} title="Simulation settings">
            <IconSettings />
          </button>
        </div>
      </header>

      {/* Filter Bar */}
      <div className="filter-bar">
        <div className="filter-group">
          <button className={`filter-chip ${filters.onlyOn ? 'active' : ''}`}
            onClick={() => setFilters(f => ({ ...f, onlyOn: !f.onlyOn, onlyOff: false }))}>
            🟢 On
          </button>
          <button className={`filter-chip ${filters.onlyOff ? 'active' : ''}`}
            onClick={() => setFilters(f => ({ ...f, onlyOff: !f.onlyOff, onlyOn: false }))}>
            ⚫ Off
          </button>
        </div>
        <select className="filter-select" value={filters.location}
          onChange={e => setFilters(f => ({ ...f, location: e.target.value }))}>
          <option value="">All Locations</option>
          {locations.map(l => <option key={l} value={l}>{l}</option>)}
        </select>
        <select className="filter-select" value={filters.type}
          onChange={e => setFilters(f => ({ ...f, type: e.target.value as DeviceType | '' }))}>
          <option value="">All Types</option>
          <option value="LIGHT">💡 Lights</option>
          <option value="FAN">🌀 Fans</option>
          <option value="THERMOSTAT">🌡️ Thermostats</option>
          <option value="DOOR_LOCK">🔒 Locks</option>
        </select>
        {(filters.location || filters.type || filters.onlyOn || filters.onlyOff) && (
          <button className="clear-filters" onClick={() => setFilters({ location: '', type: '', onlyOn: false, onlyOff: false })}>
            Clear filters
          </button>
        )}
      </div>

      {/* Main Content */}
      <main className="main">
        {loading ? (
          <div className="loading">Loading devices…</div>
        ) : devices.length === 0 ? (
          <div className="empty">
            <p>No devices found.</p>
            <button className="btn-primary" onClick={() => setShowRegister(true)}>Add your first device</button>
          </div>
        ) : (
          Object.entries(grouped).sort(([a], [b]) => a.localeCompare(b)).map(([loc, devs]) => (
            <section key={loc} className="location-section">
              <h2 className="location-title">{loc}</h2>
              <div className="device-grid">
                {devs.sort((a, b) => a.name.localeCompare(b.name)).map(d => (
                  <DeviceCard key={d.id} device={d}
                    onRefresh={load}
                    onDelete={id => setDeletedIds(s => new Set([...s, id]))} />
                ))}
              </div>
            </section>
          ))
        )}
      </main>

      {showRegister && (
        <RegisterModal onClose={() => setShowRegister(false)} onRegistered={load} locations={locations} />
      )}
      {showSim && (
        <SimulationPanel locations={locations} onClose={() => setShowSim(false)} onReset={load} />
      )}
    </div>
  );
}
