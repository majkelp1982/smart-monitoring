package utils.model;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Fan {
  @Min(value = 0)
  @Max(value = 100)
  private int currentSpeed;

  @Min(value = 0)
  @Max(value = 100)
  private int goalSpeed;

  private int revolution;
}
