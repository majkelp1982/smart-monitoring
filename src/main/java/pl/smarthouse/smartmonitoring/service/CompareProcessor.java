package pl.smarthouse.smartmonitoring.service;

import java.util.HashMap;
import java.util.Objects;
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
      final @NonNull Object currentValue,
      @NonNull final Object referenceValue,
      @NonNull final String name) {
    final CompareProperties compareProperties = compareMap.get(name);

    if (Objects.isNull(compareProperties)) {
      throw new CompareProcessorException(
          String.format(
              "No compare properties set for: %s. Should be marked as transient, if field not needed",
              name));
    }
    if (!currentValue
        .getClass()
        .getTypeName()
        .toLowerCase()
        .contains(compareProperties.getClassType().toString().toLowerCase())) {
      throw new CompareProcessorException(
          String.format(
              "Provided compare property for field: %s is wrong. Expected type: %s, but is %s",
              name, currentValue.getClass().getTypeName(), compareProperties.getClassType()));
    }

    switch (compareProperties.getClassType().toString()) {
      case "int":
        return isSaveRequired((int) currentValue, (int) referenceValue, compareProperties);
      case "double":
      case "float":
        return isSaveRequired((double) currentValue, (double) referenceValue, compareProperties);
      case "boolean":
        return isSaveRequired((boolean) currentValue, (boolean) referenceValue, compareProperties);
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
