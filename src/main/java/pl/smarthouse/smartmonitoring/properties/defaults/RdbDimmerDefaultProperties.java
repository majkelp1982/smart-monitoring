package pl.smarthouse.smartmonitoring.properties.defaults;

import pl.smarthouse.smartmonitoring.model.BooleanCompareProperties;
import pl.smarthouse.smartmonitoring.model.NumberCompareProperties;
import pl.smarthouse.smartmonitoring.service.CompareProcessor;

public class RdbDimmerDefaultProperties {
  private static NumberCompareProperties getNumberCompareProperties() {
    return NumberCompareProperties.builder().saveEnabled(true).saveTolerance(1).build();
  }

  private static BooleanCompareProperties getBooleanProperties() {
    return BooleanCompareProperties.builder().saveEnabled(true).build();
  }

  public static void setDefaultProperties(
      CompareProcessor compareProcessor, String sensorFullName) {
    compareProcessor.addMap(sensorFullName + ".forceMax", getBooleanProperties());
    compareProcessor.addMap(sensorFullName + ".forceMin", getBooleanProperties());
    compareProcessor.addMap(
        sensorFullName + ".rdbDimmerResponse.goalPower", getNumberCompareProperties());
    compareProcessor.addMap(
        sensorFullName + ".rdbDimmerResponse.incremental", getBooleanProperties());
    compareProcessor.addMap(
        sensorFullName + ".rdbDimmerResponse.msDelay", getNumberCompareProperties());
    compareProcessor.addMap(
        sensorFullName + ".rdbDimmerResponse.power", getNumberCompareProperties());
    compareProcessor.addMap(sensorFullName + ".rdbDimmerResponse.state", getBooleanProperties());
  }
}
