package pl.smarthouse.smartmonitoring.model;

import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class EnumCompareProperties extends CompareProperties {
  @Override
  public Class<?> getClassType() {
    return Enum.class;
  }
}
