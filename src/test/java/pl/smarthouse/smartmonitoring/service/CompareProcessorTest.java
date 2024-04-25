package pl.smarthouse.smartmonitoring.service;

import static pl.smarthouse.smartmodule.model.actors.type.pca9685.Pca9685CommandType.WRITE_SERVO0_MICROSECONDS;

import java.util.HashMap;
import java.util.Set;
import org.junit.jupiter.api.Test;
import pl.smarthouse.sharedobjects.dao.ModuleDao;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280Response;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20Result;
import pl.smarthouse.smartmonitoring.model.NumberCompareProperties;
import pl.smarthouse.smartmonitoring.model.PrimitiveField;
import pl.smarthouse.smartmonitoring.utils.PrimitiveFieldFinder;
import utils.model.VentModuleDao;
import utils.model.core.*;

class CompareProcessorTest {

  @Test
  void findingFields() {
    ModuleDao moduleDao = createVentModuleDao();
    HashMap<String, PrimitiveField> primitiveFields =
        PrimitiveFieldFinder.findPrimitiveFields(moduleDao.getClass(), moduleDao);

    primitiveFields.keySet().stream().sorted().forEach(System.out::println);
  }

  @Test
  void checkIfAllPropertiesSet() {
    CompareProcessor compareProcessor = new CompareProcessor();

    compareProcessor.addMap(
        "first.object", NumberCompareProperties.builder().saveEnabled(true).build());
    Set<String> primitiveSet = Set.of("first.object", "second.object", "third.object");

    compareProcessor.checkIfAllPropertiesSet(primitiveSet).subscribe();
  }

  private VentModuleDao createVentModuleDao() {
    return VentModuleDao.builder()
        .type("testModule")
        .zoneDaoHashMap(new HashMap<>())
        .fans(
            Fans.builder()
                .inlet(Fan.builder().currentSpeed(1).goalSpeed(0).build())
                .outlet(Fan.builder().currentSpeed(1).goalSpeed(0).build())
                .build())
        .airExchanger(
            AirExchanger.builder()
                .inlet(new Bme280Response())
                .outlet(new Bme280Response())
                .freshAir(new Bme280Response())
                .userAir(new Bme280Response())
                .build())
        .forcedAirSystemExchanger(
            ForcedAirSystemExchanger.builder()
                .watterIn(new Ds18b20Result())
                .watterOut(new Ds18b20Result())
                .airIn(new Ds18b20Result())
                .airOut(new Ds18b20Result())
                .build())
        .intakeThrottle(
            Throttle.builder()
                .openPosition(0)
                .closePosition(1)
                .goalPosition(1)
                .currentPosition(0)
                .commandType(WRITE_SERVO0_MICROSECONDS)
                .build())
        .build();
  }
}
