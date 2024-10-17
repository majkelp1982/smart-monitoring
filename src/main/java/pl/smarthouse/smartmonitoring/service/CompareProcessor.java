package pl.smarthouse.smartmonitoring.service;

import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.enums.Compare;
import pl.smarthouse.smartmonitoring.exception.ComparatorDefinitionException;
import pl.smarthouse.smartmonitoring.exception.CompareProcessorException;
import pl.smarthouse.smartmonitoring.model.CompareProperties;
import pl.smarthouse.smartmonitoring.model.PrimitiveField;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Getter
@RequiredArgsConstructor
@Slf4j
public class CompareProcessor {
  private final HashMap<String, CompareProperties> compareMap = new HashMap<>();

  public void addMap(final String fieldName, final CompareProperties compareProperties) {
    if (Objects.nonNull(compareMap.get(fieldName))) {
      throw new ComparatorDefinitionException(
          String.format("Compare property for: %s, already exist", fieldName));
    }
    compareMap.put(fieldName, compareProperties);
  }

  public Mono<Compare> checkIfSaveRequired(
      final HashMap<String, PrimitiveField> currentPrimitives,
      final HashMap<String, PrimitiveField> referencePrimitives) {
    return Mono.justOrEmpty(currentPrimitives)
        .switchIfEmpty(
            Mono.defer(
                () ->
                    Mono.error(
                        new CompareProcessorException(
                            "Before checking if save required, primitive map need to be set"))))
        .flatMapMany(currentPrimitivesMap -> Flux.fromIterable(currentPrimitivesMap.keySet()))
        .flatMap(
            name -> {
              try {
                return Mono.just(
                    checkIfSaveRequiredByValue(
                        currentPrimitives.get(name).getField(),
                        referencePrimitives.get(name).getField(),
                        name));
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
      final Object currentValue, final Object referenceValue, final @NonNull String name) {
    if (Objects.isNull(currentValue)) {
      log.error("Skipping: {}, current value is null", name);
      return Compare.OK;
    }
    if (Objects.isNull(referenceValue)) {
      log.error("Skipping: {}, reference value is null", name);
      return Compare.OK;
    }
    final CompareProperties compareProperties = compareMap.get(name);

    if (Objects.isNull(compareProperties)) {
      throw new CompareProcessorException(
          String.format(
              "No compare properties set for: %s. Should be marked as transient, if field not needed",
              name));
    }

    String currentValueType = getCurrentValueTypeName(currentValue);

    if (!currentValueType.contains(compareProperties.getClassType().getTypeName().toLowerCase())) {
      throw new CompareProcessorException(
          String.format(
              "Provided compare property for field: %s is wrong. Expected type: %s, but provided is: %s",
              name,
              currentValueType,
              compareProperties.getClassType().getTypeName().toLowerCase()));
    }

    if (currentValueType.contains("number")) {
      return isSaveRequired((Number) currentValue, (Number) referenceValue, compareProperties);
    }

    if (currentValueType.contains("boolean")) {
      return isSaveRequired((boolean) currentValue, (boolean) referenceValue, compareProperties);
    }

    if (currentValueType.contains("enum")) {
      return isSaveRequired((Enum) currentValue, (Enum) referenceValue, compareProperties);
    }

    if (currentValueType.contains("map")) {
      return isSaveRequired(currentValue.hashCode(), referenceValue.hashCode(), compareProperties);
    }

    // If not found, throw an exception
    throw new ComparatorDefinitionException(
        String.format("Comparator is not defined for type: %s", currentValueType));
  }

  private String getCurrentValueTypeName(Object currentValue) {
    String superClassTypeName = currentValue.getClass().getSuperclass().toString().toLowerCase();
    if (superClassTypeName.contains("object")) {
      return currentValue.getClass().toString().toLowerCase();
    } else {
      return superClassTypeName;
    }
  }

  private Compare isSaveRequired(
      final Number currentValue,
      final Number lastValue,
      final CompareProperties compareProperties) {
    if (!compareProperties.isSaveEnabled()) {
      return Compare.OK;
    }
    Number saveTolerance = (Number) compareProperties.getSaveTolerance();
    return (Math.abs(currentValue.doubleValue() - lastValue.doubleValue())
            >= saveTolerance.doubleValue())
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

  private Compare isSaveRequired(
      final Enum currentValue, final Enum lastValue, final CompareProperties compareProperties) {
    if (!compareProperties.isSaveEnabled()) {
      return Compare.OK;
    }
    return (!currentValue.equals(lastValue)) ? Compare.SAVE_REQUIRED : Compare.OK;
  }

  public Mono<Set<String>> checkIfAllPropertiesSet(Set<String> primitiveFieldsName) {
    Set<String> missingCompareProperties = new HashSet<>();
    return Flux.fromStream(primitiveFieldsName.stream())
        .doOnNext(
            name -> {
              if (Objects.isNull(compareMap.get(name))) {
                missingCompareProperties.add(name);
              }
            })
        .collectList()
        .thenReturn(missingCompareProperties)
        .flatMap(
            missingPropertiesSet -> {
              if (!missingPropertiesSet.isEmpty()) {
                return Mono.error(
                    new ComparatorDefinitionException(
                        String.format(
                            "Compare properties missing for following primitive fields: %s. If primitive is not needed, should be marked with @Transient",
                            missingPropertiesSet.stream().sorted().collect(Collectors.toList()))));
              } else {
                return Mono.just(primitiveFieldsName);
              }
            });
  }
}
