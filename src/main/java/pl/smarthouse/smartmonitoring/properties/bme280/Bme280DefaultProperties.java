package pl.smarthouse.smartmonitoring.properties.bme280;

import pl.smarthouse.smartmonitoring.model.BooleanCompareProperties;
import pl.smarthouse.smartmonitoring.model.DoubleCompareProperties;
import pl.smarthouse.smartmonitoring.model.IntCompareProperties;
import pl.smarthouse.smartmonitoring.service.CompareProcessor;

public class Bme280DefaultProperties {
  public static DoubleCompareProperties getTemperatureProperties() {
    return DoubleCompareProperties.builder()
        .saveEnabled(true)
        .saveTolerance(0.3)
        .warning(0.5)
        .alarm(1.0)
        .build();
  }

  public static IntCompareProperties getHumidityProperties() {
    return IntCompareProperties.builder()
        .saveEnabled(true)
        .saveTolerance(5)
        .warning(5)
        .alarm(10)
        .build();
  }

  public static DoubleCompareProperties getPressureProperties() {
    return DoubleCompareProperties.builder().saveEnabled(false).build();
  }

  public static BooleanCompareProperties getErrorProperties() {
    return BooleanCompareProperties.builder().saveEnabled(true).build();
  }

  public static void setDefaultProperties(
      CompareProcessor compareProcessor, String sensorFullName) {
    compareProcessor.addMap(sensorFullName + ".error", getErrorProperties());
    compareProcessor.addMap(sensorFullName + ".humidity", getHumidityProperties());
    compareProcessor.addMap(sensorFullName + ".pressure", getPressureProperties());
    compareProcessor.addMap(sensorFullName + ".temperature", getTemperatureProperties());
  }
}
