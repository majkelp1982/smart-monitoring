package utils.model.core;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Fan {
  @Min(value = 0)
  @Max(value = 100)
  private int currentSpeed;

  @Min(value = 0)
  @Max(value = 100)
  private int goalSpeed;

  private int revolution;
}
