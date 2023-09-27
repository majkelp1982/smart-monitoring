package pl.smarthouse.smartmonitoring.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.smartmonitoring.model.ErrorPrediction;

@Service
@RequiredArgsConstructor
@Slf4j
public class ErrorHandlingService {
  private final MonitoringService monitoringService;
  private final List<ErrorPrediction> errorPredictions;
  private final List<ErrorPrediction> errorsPendingAcknowledge;

  public void process() {
    errorPredictions.forEach(this::handleErrorPrediction);
  }

  private void handleErrorPrediction(ErrorPrediction errorPrediction) {
    if (isActiveButNotEnabled.or(isNotActiveButPendingAcknowledge).test(errorPrediction)) {
      try {
        log.info(
            "Error: {} is not more active. Moving to pending acknowledge",
            errorPrediction.getMessage());
        moveToErrorsPendingAcknowledgeAndResetState(errorPrediction);
      } catch (CloneNotSupportedException e) {
        log.error(
            "Error: {} should be moved to pending acknowledge but error occurred. Error: {}",
            errorPrediction.getMessage(),
            e);
        throw new RuntimeException(e);
      }
      return;
    }
    if (!isEnabled.test(errorPrediction)
        || !errorPrediction.getPredicate().test(monitoringService.getModuleDao())) {
      return;
    }

    setErrorPredictionActive(errorPrediction);
    log.error("Module error discovered. Message: {}", errorPrediction.getMessage());
  }

  private void moveToErrorsPendingAcknowledgeAndResetState(ErrorPrediction errorPrediction)
      throws CloneNotSupportedException {
    errorPrediction.setEndTimestamp(LocalDateTime.now());
    errorsPendingAcknowledge.add((ErrorPrediction) errorPrediction.clone());
    resetErrorPrediction(errorPrediction);
  }

  private void setErrorPredictionActive(ErrorPrediction errorPrediction) {
    errorPrediction.setActive(true);
    errorPrediction.setPendingAcknowledge(true);
    errorPrediction.setBeginTimestamp(LocalDateTime.now());
    errorPrediction.getStateChangedListener().accept(true);
  }

  private void resetErrorPrediction(ErrorPrediction errorPrediction) {
    errorPrediction.setActive(false);
    errorPrediction.setPendingAcknowledge(false);
    errorPrediction.setBeginTimestamp(null);
    errorPrediction.setEndTimestamp(null);
    errorPrediction.getStateChangedListener().accept(false);
  }

  private final Predicate<ErrorPrediction> isEnabled = ErrorPrediction::isEnable;
  private final Predicate<ErrorPrediction> isActiveButNotEnabled =
      errorPrediction -> errorPrediction.isActive() && !errorPrediction.isEnable();
  private final Predicate<ErrorPrediction> isNotActiveButPendingAcknowledge =
      errorPrediction -> !errorPrediction.isActive() && errorPrediction.isPendingAcknowledge();
}