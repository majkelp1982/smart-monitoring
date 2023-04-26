package pl.smarthouse.smartmonitoring.properties.defaults;

import pl.smarthouse.smartmonitoring.model.BooleanCompareProperties;
import pl.smarthouse.smartmonitoring.model.NumberCompareProperties;
import pl.smarthouse.smartmonitoring.service.CompareProcessor;

public class Ds18b20DefaultProperties {
  public static NumberCompareProperties getTemperatureProperties() {
    return NumberCompareProperties.builder()
        .saveEnabled(true)
        .saveTolerance(0.3)
        .warning(0.5)
        .alarm(1.0)
        .build();
  }

  public static BooleanCompareProperties getErrorProperties() {
    return BooleanCompareProperties.builder().saveEnabled(true).build();
  }

  public static void setDefaultProperties(
      CompareProcessor compareProcessor, String sensorFullName) {
    compareProcessor.addMap(sensorFullName + ".error", getErrorProperties());
    compareProcessor.addMap(sensorFullName + ".temp", getTemperatureProperties());
  }
}
