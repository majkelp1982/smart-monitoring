package pl.smarthouse.smartmonitoring.service;

import static pl.smarthouse.smartmonitoring.utils.CloneUtils.cloneObject;

import java.time.LocalDateTime;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.dao.ModuleDao;
import pl.smarthouse.sharedobjects.enums.Compare;
import pl.smarthouse.smartmonitoring.model.PrimitiveField;
import pl.smarthouse.smartmonitoring.utils.PrimitiveFieldFinder;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {
  private final CompareProcessor compareProcessor;
  private final ReactiveMongoTemplate reactiveMongoTemplate;
  private ModuleDao moduleDao;
  private HashMap<String, PrimitiveField> currentPrimitives;
  private HashMap<String, PrimitiveField> referencePrimitives;

  public void process() {
    Mono.just(getPrimitiveFields(moduleDao))
        .flatMap(
            primitiveFieldsMap ->
                compareProcessor.checkIfAllPropertiesSet(primitiveFieldsMap.keySet()))
        .flatMap(
            signal -> compareProcessor.checkIfSaveRequired(currentPrimitives, referencePrimitives))
        .flatMap(
            compare -> {
              if (Compare.SAVE_REQUIRED.equals(compare)) {
                moduleDao.setSaveTimestamp(LocalDateTime.now());
                return reactiveMongoTemplate
                    .save(moduleDao, moduleDao.getModuleName().toLowerCase())
                    .doOnSuccess(signal -> cloneCurrentPrimitiveFieldsToReferenceMap())
                    .doOnSuccess(signal -> log.info("Save dao object successful"));
              } else {
                return Mono.empty();
              }
            })
        .onErrorResume(
            throwable -> {
              log.error(
                  "Error in monitoring service. Message: {},", throwable.getMessage(), throwable);
              return Mono.empty();
            })
        .subscribe();
  }

  public void setModuleDaoObject(final ModuleDao moduleDao) {
    this.moduleDao = moduleDao;
    getPrimitiveFields(moduleDao);
    referencePrimitives = new HashMap<>();
    cloneCurrentPrimitiveFieldsToReferenceMap();
  }

  private HashMap<String, PrimitiveField> getPrimitiveFields(final ModuleDao moduleDao) {
    currentPrimitives = PrimitiveFieldFinder.findPrimitiveFields(moduleDao);
    return currentPrimitives;
  }

  private void cloneCurrentPrimitiveFieldsToReferenceMap() {
    this.referencePrimitives.clear();
    currentPrimitives.forEach(
        (name, primitiveField) -> this.referencePrimitives.put(name, cloneObject(primitiveField)));
  }

  public ModuleDao getModuleDao() {
    return moduleDao;
  }
}
