package utils.model.core;

import lombok.Builder;
import lombok.Data;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280Response;

@Data
@Builder
public class AirExchanger {
  private Bme280Response inlet;
  private Bme280Response outlet;
  private Bme280Response freshAir;
  private Bme280Response userAir;
}
