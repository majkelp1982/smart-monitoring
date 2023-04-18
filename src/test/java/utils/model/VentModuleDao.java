package utils.model;

import java.util.HashMap;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;
import pl.smarthouse.sharedobjects.dao.ModuleDao;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import utils.model.core.AirExchanger;
import utils.model.core.Fans;
import utils.model.core.ForcedAirSystemExchanger;
import utils.model.core.Throttle;
import utils.model.enums.State;

@Data
@SuperBuilder
public class VentModuleDao extends ModuleDao {
  // TODO temporary transient
  @Transient private final HashMap<ZoneName, ZoneDao> zoneDaoHashMap;
  private final Fans fans;
  private final Throttle intakeThrottle;
  private final AirExchanger airExchanger;
  private final ForcedAirSystemExchanger forcedAirSystemExchanger;
  private State circuitPump;
  private State airCondition;
}
