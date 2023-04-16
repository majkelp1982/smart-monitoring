package utils;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;
import pl.smarthouse.sharedobjects.dao.ModuleDao;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280Response;
import utils.model.Fans;

@Data
@SuperBuilder
public class TestModuleDao extends ModuleDao {
  private final Bme280Response sensorBme280 = new Bme280Response();
  private final Fans fans = new Fans();

  @Transient private boolean requiredAir;
  private boolean requiredWater;
}
