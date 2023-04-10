package pl.smarthouse.smartmonitoring.service;

import static pl.smarthouse.smartmonitoring.utils.CloneUtils.cloneObject;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.dao.ModuleDao;
import pl.smarthouse.sharedobjects.enums.Compare;
import reactor.core.publisher.Mono;

@Service
@Setter
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {
  private final CompareProcessor compareProcessor;
  private final ReactiveMongoTemplate reactiveMongoTemplate;
  private ModuleDao currentModuleDao;
  private ModuleDao referenceModuleDao;

  public void process() {
    compareProcessor
        .checkIfSaveRequired(currentModuleDao, referenceModuleDao)
        .flatMap(
            compare -> {
              if (Compare.SAVE_REQUIRED.equals(compare)) {
                currentModuleDao.setSaveTimestamp(LocalDateTime.now());
                return reactiveMongoTemplate
                    .save(currentModuleDao, currentModuleDao.getModuleName().toLowerCase())
                    .doOnSuccess(signal -> setReferenceObjectToCurrent())
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
    this.currentModuleDao = moduleDao;
    referenceModuleDao = cloneObject(moduleDao);
  }

  private void setReferenceObjectToCurrent() {
    referenceModuleDao = cloneObject(currentModuleDao);
  }
}
