package pl.smarthouse.smartmonitoring.properties.defaults;

import pl.smarthouse.smartmonitoring.model.BooleanCompareProperties;
import pl.smarthouse.smartmonitoring.model.NumberCompareProperties;
import pl.smarthouse.smartmonitoring.service.CompareProcessor;

public class Snzb03DefaultProperties {
  private static NumberCompareProperties getNumberCompareProperties(int tolerance) {
    return NumberCompareProperties.builder().saveEnabled(true).saveTolerance(tolerance).build();
  }

  private static BooleanCompareProperties getBooleanProperties() {
    return BooleanCompareProperties.builder().saveEnabled(true).build();
  }

  public static void set(CompareProcessor compareProcessor, String sensorFullName) {
    compareProcessor.addMap(sensorFullName + ".battery", getNumberCompareProperties(1));
    compareProcessor.addMap(sensorFullName + ".batteryLow", getBooleanProperties());
    compareProcessor.addMap(sensorFullName + ".linkQuality", getNumberCompareProperties(3));
    compareProcessor.addMap(sensorFullName + ".occupancy", getBooleanProperties());
    compareProcessor.addMap(sensorFullName + ".tamper", getBooleanProperties());
    compareProcessor.addMap(sensorFullName + ".voltage", getNumberCompareProperties(10));
  }
}
