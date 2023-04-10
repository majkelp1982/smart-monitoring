package pl.smarthouse.smartmonitoring.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import pl.smarthouse.smartmonitoring.exception.InvalidValueException;

@Getter
@SuperBuilder
public abstract class CompareProperties {
  private Object alarm;
  @NonNull private boolean saveEnabled;
  private Object warning;
  private Object saveTolerance;
  private Class<?> classType;

  public void setSaveTolerance(final boolean saveTolerance) {
    this.saveTolerance = saveTolerance;
  }

  public abstract Class<?> getClassType();

  public void setWarning(final Object warning) {
    validate(warning);
    this.warning = warning;
  }

  public void setAlarm(final Class<?> classType, final Object alarm) {
    validate(alarm);
    this.alarm = alarm;
  }

  private void validate(final Object object) {
    if (!classType.getTypeName().equals(object.getClass().getTypeName())) {
      throw new InvalidValueException(
          String.format(
              "Object type: %s, is not expected type: %s",
              classType.getTypeName(), object.getClass().getTypeName()));
    }
  }
}
