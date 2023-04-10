package pl.smarthouse.smartmonitoring.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280Response;

@SuperBuilder
@Getter
public class Bme280ResponseCompareProperties extends CompareProperties {
  @NonNull DoubleCompareProperties temperature;
  @NonNull IntCompareProperties humidity;
  @NonNull DoubleCompareProperties pressure;

  @Override
  public Class<?> getClassType() {
    return Bme280Response.class;
  }
}
