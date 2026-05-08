package com.smarthome.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class AmbientTemperatureRequest {
    @Min(value = 0, message = "Temperature must be at least 0°F")
    @Max(value = 120, message = "Temperature must be at most 120°F")
    private double temperature;

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
}
