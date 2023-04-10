package pl.smarthouse.smartmonitoring.service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.dao.ModuleDao;
import pl.smarthouse.sharedobjects.enums.Compare;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280Response;
import pl.smarthouse.smartmonitoring.exception.ComparatorDefinitionException;
import pl.smarthouse.smartmonitoring.exception.CompareProcessorException;
import pl.smarthouse.smartmonitoring.model.Bme280ResponseCompareProperties;
import pl.smarthouse.smartmonitoring.model.CompareProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Getter
@RequiredArgsConstructor
@Slf4j
public class CompareProcessor {
  private final HashMap<String, CompareProperties> compareMap = new HashMap<>();

  public void addMap(final String fieldName, final CompareProperties compareProperties) {
    compareMap.put(fieldName, compareProperties);
  }

  public Mono<Compare> checkIfSaveRequired(
      final ModuleDao currentModuleDao, final ModuleDao referenceModuleDao) {
    return Mono.justOrEmpty(currentModuleDao)
        .switchIfEmpty(
            Mono.defer(
                () ->
                    Mono.error(
                        new CompareProcessorException(
                            "Before checking if save required, module configuration DAO object need to be set"))))
        .flatMapMany(moduleDao -> Flux.fromArray(moduleDao.getClass().getDeclaredFields()))
        .flatMap(
            field -> {
              try {
                return Mono.just(
                    checkIfSaveRequiredByValue(currentModuleDao, referenceModuleDao, field));
              } catch (final Exception exception) {
                return Mono.error(exception);
              }
            })
        .collectList()
        .flatMap(
            compares ->
                Mono.just(
                    compares.contains(Compare.SAVE_REQUIRED) ? Compare.SAVE_REQUIRED : Compare.OK))
        .doOnError(
            throwable ->
                log.error("Error in compare processor. Message: {}", throwable.getMessage()));
  }

  public Compare checkIfSaveRequiredByValue(
      final @NonNull Object currentValue,
      @NonNull final Object referenceValue,
      @NonNull final Field field)
      throws IllegalAccessException, NoSuchFieldException {
    if (field.isAnnotationPresent(org.springframework.data.annotation.Transient.class)) {
      return Compare.OK;
    }
    final Field currentField = currentValue.getClass().getDeclaredField(field.getName());
    final Field referenceField = referenceValue.getClass().getDeclaredField(field.getName());
    final CompareProperties compareProperties = compareMap.get(field.getName());
    if (Objects.isNull(compareProperties)) {
      throw new CompareProcessorException(
          String.format(
              "No compare properties set for: %s, if not needed field should be marked as transient",
              field.getName()));
    }
    if (!compareProperties.getClassType().equals(field.getGenericType())) {
      throw new CompareProcessorException(
          String.format(
              "Provided compare property for field: %s is wrong. Expected type: %s, but is %s",
              field.getName(), field.getGenericType(), compareProperties.getClassType()));
    }

    final String classFullName = (field.getType()).toString();
    final String className =
        classFullName.substring(classFullName.lastIndexOf(".") + 1).toLowerCase();
    currentField.setAccessible(true);
    referenceField.setAccessible(true);
    switch (className) {
      case "int":
        return isSaveRequired(
            (int) currentField.get(currentValue),
            (int) referenceField.get(referenceValue),
            compareProperties);
      case "double":
      case "float":
        return isSaveRequired(
            (double) currentField.get(currentValue),
            (double) referenceField.get(referenceValue),
            compareProperties);
      case "boolean":
        return isSaveRequired(
            (boolean) currentField.get(currentValue),
            (boolean) referenceField.get(referenceValue),
            compareProperties);
      case "bme280response":
        {
          final Bme280Response bme280ResponseCurrent =
              (Bme280Response) currentField.get(currentValue);
          final Bme280Response bme280ResponseReference =
              (Bme280Response) referenceField.get(referenceValue);
          final Bme280ResponseCompareProperties bme280ResponseCompareProperties =
              (Bme280ResponseCompareProperties) compareProperties;
          final Compare compareTemperature =
              isSaveRequired(
                  bme280ResponseCurrent.getTemperature(),
                  bme280ResponseReference.getTemperature(),
                  bme280ResponseCompareProperties.getTemperature());
          final Compare compareHumidity =
              isSaveRequired(
                  bme280ResponseCurrent.getHumidity(),
                  bme280ResponseReference.getHumidity(),
                  bme280ResponseCompareProperties.getHumidity());
          final Compare comparePressure =
              isSaveRequired(
                  bme280ResponseCurrent.getPressure(),
                  bme280ResponseReference.getPressure(),
                  bme280ResponseCompareProperties.getPressure());

          return List.of(compareTemperature, compareHumidity, comparePressure)
                  .contains(Compare.SAVE_REQUIRED)
              ? Compare.SAVE_REQUIRED
              : Compare.OK;
        }
      default:
        throw new ComparatorDefinitionException(
            String.format(
                "Comparator is not defined for type: %s", currentValue.getClass().getTypeName()));
    }
  }

  private Compare isSaveRequired(
      final int currentValue, final int lastValue, final CompareProperties compareProperties) {
    if (!compareProperties.isSaveEnabled()) {
      return Compare.OK;
    }
    return (Math.abs(currentValue - lastValue) > (int) compareProperties.getSaveTolerance())
        ? Compare.SAVE_REQUIRED
        : Compare.OK;
  }

  private Compare isSaveRequired(
      final double currentValue,
      final double lastValue,
      final CompareProperties compareProperties) {
    if (!compareProperties.isSaveEnabled()) {
      return Compare.OK;
    }
    return (Math.abs(currentValue - lastValue) > (double) compareProperties.getSaveTolerance())
        ? Compare.SAVE_REQUIRED
        : Compare.OK;
  }

  private Compare isSaveRequired(
      final boolean currentValue,
      final boolean lastValue,
      final CompareProperties compareProperties) {
    if (!compareProperties.isSaveEnabled()) {
      return Compare.OK;
    }
    return (currentValue != lastValue) ? Compare.SAVE_REQUIRED : Compare.OK;
  }
}
