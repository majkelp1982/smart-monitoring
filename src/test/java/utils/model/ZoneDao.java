package utils.model;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import pl.smarthouse.sharedobjects.enums.Operation;
import utils.model.core.Throttle;
import utils.model.enums.FunctionType;

@Getter
@Setter
@Builder
public class ZoneDao {
  @NonNull private LocalDateTime lastUpdate;
  @NonNull private FunctionType functionType;
  private Operation operation;
  @NonNull private Throttle throttle;
  private int requiredPower;

  public void setOperation(final Operation operation) {
    this.operation = operation;
    lastUpdate = LocalDateTime.now();
  }
}
