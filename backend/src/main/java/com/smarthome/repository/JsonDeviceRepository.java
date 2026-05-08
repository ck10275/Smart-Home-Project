package com.smarthome.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smarthome.domain.device.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * JSON file-based implementation of {@link IDeviceRepository}.
 *
 * <p>All device state and command history is stored in a single JSON file.
 * State machines are rehydrated on load by calling {@code setState()} on each
 * device, which restores the state machine to the last known state.</p>
 *
 * <p>Thread-safety is provided via a {@link ReadWriteLock} since the thermostat
 * simulation timer runs on a separate thread.</p>
 */
@Repository
public class JsonDeviceRepository implements IDeviceRepository {

    private static final Logger log = LoggerFactory.getLogger(JsonDeviceRepository.class);

    private final String dataFilePath;
    private final ObjectMapper mapper;
    private DataStore store;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public JsonDeviceRepository(@Value("${smarthome.data-file}") String dataFilePath) {
        this.dataFilePath = dataFilePath;
        this.mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        load();
    }

    private void load() {
        File file = new File(dataFilePath);
        if (file.exists()) {
            try {
                store = mapper.readValue(file, DataStore.class);
                // Rehydrate all state machines after deserialization
                store.getDevices().forEach(this::rehydrateDevice);
                log.info("Loaded {} devices from {}", store.getDevices().size(), dataFilePath);
            } catch (IOException e) {
                log.error("Failed to load data file, starting fresh: {}", e.getMessage());
                store = new DataStore();
            }
        } else {
            store = new DataStore();
            log.info("No data file found, starting with empty store");
        }
    }

    /**
     * Rehydrates a device's state machine after JSON deserialization.
     * Each device subtype's setter is responsible for calling stateMachine.rehydrate().
     */
    private void rehydrateDevice(Device device) {
        // State machine rehydration happens automatically in each device's setState() setter,
        // which Jackson calls during deserialization. This is a no-op but kept for clarity.
        log.debug("Rehydrated device: {} [{}]", device.getName(), device.getType());
    }

    private void persist() {
        try {
            File file = new File(dataFilePath);
            file.getParentFile().mkdirs();
            mapper.writeValue(file, store);
        } catch (IOException e) {
            log.error("Failed to persist data: {}", e.getMessage());
        }
    }

    @Override
    public List<Device> findAll() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableList(store.getDevices());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Device> findByLocation(String location) {
        lock.readLock().lock();
        try {
            return store.getDevices().stream()
                .filter(d -> d.getLocation().equalsIgnoreCase(location))
                .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Device> findByType(DeviceType type) {
        lock.readLock().lock();
        try {
            return store.getDevices().stream()
                .filter(d -> d.getType() == type)
                .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Device> findById(String id) {
        lock.readLock().lock();
        try {
            return store.getDevices().stream()
                .filter(d -> d.getId().equals(id))
                .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void save(Device device) {
        lock.writeLock().lock();
        try {
            store.getDevices().removeIf(d -> d.getId().equals(device.getId()));
            store.getDevices().add(device);
            persist();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(String id) {
        lock.writeLock().lock();
        try {
            store.getDevices().removeIf(d -> d.getId().equals(id));
            persist();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void saveAll(List<Device> devices) {
        lock.writeLock().lock();
        try {
            store.setDevices(new ArrayList<>(devices));
            persist();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<CommandHistory> findHistoryByDeviceId(String deviceId) {
        lock.readLock().lock();
        try {
            return store.getCommandHistory().stream()
                .filter(h -> h.getDeviceId().equals(deviceId))
                .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<CommandHistory> findAllHistory() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableList(store.getCommandHistory());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void saveHistory(CommandHistory entry) {
        lock.writeLock().lock();
        try {
            store.getCommandHistory().add(entry);
            // Keep last 500 entries
            if (store.getCommandHistory().size() > 500) {
                store.getCommandHistory().remove(0);
            }
            persist();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public DataStore.SimulationSettings getSimulationSettings() {
        lock.readLock().lock();
        try {
            return store.getSimulationSettings();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void saveSimulationSettings(DataStore.SimulationSettings settings) {
        lock.writeLock().lock();
        try {
            store.setSimulationSettings(settings);
            persist();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
