package utils.model.core;

import lombok.Data;
import pl.smarthouse.sharedobjects.enums.Operation;

@Data
public class TempComfortZone {
  boolean enabled;
  private double temperature;
  private int humidity;
  private double requiredTemperature;

  private boolean forcedAirSystemEnabled;

  private Operation currentOperation;

  public TempComfortZone(final boolean enabled) {
    this.enabled = enabled;
  }
}
