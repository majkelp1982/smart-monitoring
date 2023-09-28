package pl.smarthouse.smartmonitoring.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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
  private final HashMap<Integer, ErrorPrediction> errorsPendingAcknowledge;

  public List<ErrorPrediction> getActiveErrorPredictions() {
    return errorPredictions.stream().filter(ErrorPrediction::isActive).collect(Collectors.toList());
  }

  public List<ErrorPrediction> getErrorPredictions() {
    return errorPredictions;
  }

  public HashMap<Integer, ErrorPrediction> getErrorsPendingAcknowledge() {
    return errorsPendingAcknowledge;
  }

  public void add(ErrorPrediction errorPrediction) {
    getErrorPrediction(errorPrediction.getMessage())
        .ifPresent(
            foundError -> {
              throw new IllegalArgumentException(
                  String.format(
                      "Duplicated error message. Error already registered: %s", foundError));
            });

    errorPredictions.add(errorPrediction);
  }

  private Optional<ErrorPrediction> getErrorPrediction(String message) {
    return errorPredictions.stream()
        .filter(errorPrediction -> message.equalsIgnoreCase(errorPrediction.getMessage()))
        .findFirst();
  }

  public void setEnabled(String message, boolean enabled) {
    getErrorPrediction(message)
        .ifPresentOrElse(
            errorPrediction -> errorPrediction.setEnable(enabled),
            () -> new IllegalArgumentException("Given error message not exists"));
  }

  public void process() {
    errorPredictions.forEach(this::handleErrorPrediction);
    monitoringService
        .getModuleDao()
        .setError(errorPredictions.stream().anyMatch(ErrorPrediction::isActive));
    monitoringService
        .getModuleDao()
        .setErrorPendingAcknowledge(!errorsPendingAcknowledge.isEmpty());
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
    ErrorPrediction errorPredictionCloned = (ErrorPrediction) errorPrediction.clone();
    errorsPendingAcknowledge.put(errorPredictionCloned.hashCode(), errorPredictionCloned);
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

  public void acknowledgePendingError(Integer hashcode) {
    errorsPendingAcknowledge.remove(hashcode);
  }

  public void clearPendingError() {
    errorsPendingAcknowledge.clear();
  }

  private final Predicate<ErrorPrediction> isEnabled = ErrorPrediction::isEnable;
  private final Predicate<ErrorPrediction> isActiveButNotEnabled =
      errorPrediction -> errorPrediction.isActive() && !errorPrediction.isEnable();
  private final Predicate<ErrorPrediction> isNotActiveButPendingAcknowledge =
      errorPrediction -> !errorPrediction.isActive() && errorPrediction.isPendingAcknowledge();
}
