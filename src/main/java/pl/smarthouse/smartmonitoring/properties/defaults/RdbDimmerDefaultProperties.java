package pl.smarthouse.smartmonitoring.properties.defaults;

import pl.smarthouse.smartmonitoring.model.BooleanCompareProperties;
import pl.smarthouse.smartmonitoring.model.NumberCompareProperties;
import pl.smarthouse.smartmonitoring.service.CompareProcessor;

public class RdbDimmerDefaultProperties {
  private static NumberCompareProperties getNumberCompareProperties() {
    return NumberCompareProperties.builder().saveEnabled(true).saveTolerance(1).build();
  }

  private static BooleanCompareProperties getBooleanProperties() {
    return BooleanCompareProperties.builder().saveEnabled(false).build();
  }

  public static void setDefaultProperties(
      CompareProcessor compareProcessor, String sensorFullName) {
    compareProcessor.addMap(sensorFullName + ".goalPower", getNumberCompareProperties());
    compareProcessor.addMap(sensorFullName + ".incremental", getBooleanProperties());
    compareProcessor.addMap(sensorFullName + ".msDelay", getNumberCompareProperties());
    compareProcessor.addMap(sensorFullName + ".power", getNumberCompareProperties());
    compareProcessor.addMap(sensorFullName + ".state", getBooleanProperties());
  }
}
