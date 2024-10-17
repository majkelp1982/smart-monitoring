package pl.smarthouse.smartmonitoring.model;

import java.util.AbstractMap;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AbstractMapCompareProperties extends CompareProperties {

  @Override
  public Class<?> getClassType() {
    return AbstractMap.class;
  }
}
