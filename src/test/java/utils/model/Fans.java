package utils.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Fans {
  private final Fan inlet = new Fan();
  private final Fan outlet = new Fan();
}
