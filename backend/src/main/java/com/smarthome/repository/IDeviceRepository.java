package com.smarthome.repository;

import com.smarthome.domain.device.CommandHistory;
import com.smarthome.domain.device.Device;
import com.smarthome.domain.device.DeviceType;

import java.util.List;
import java.util.Optional;

/**
 * Repository abstraction for device persistence.
 *
 * <p>The Repository pattern is applied here to decouple the service layer from the
 * persistence medium. Services depend only on this interface — never on the JSON
 * file implementation directly. Switching to SQLite would require only a new
 * implementation of this interface, with zero changes to the service layer.</p>
 */
public interface IDeviceRepository {

    List<Device> findAll();

    List<Device> findByLocation(String location);

    List<Device> findByType(DeviceType type);

    Optional<Device> findById(String id);

    void save(Device device);

    void delete(String id);

    void saveAll(List<Device> devices);

    List<CommandHistory> findHistoryByDeviceId(String deviceId);

    List<CommandHistory> findAllHistory();

    void saveHistory(CommandHistory entry);

    DataStore.SimulationSettings getSimulationSettings();

    void saveSimulationSettings(DataStore.SimulationSettings settings);
}
