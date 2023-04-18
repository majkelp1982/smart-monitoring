package utils.model.core;

import lombok.*;
import pl.smarthouse.smartmodule.model.actors.type.pca9685.Pca9685CommandType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Throttle {
  private int openPosition;
  private int closePosition;
  private int currentPosition;
  private int goalPosition;

  private Pca9685CommandType commandType;
}
