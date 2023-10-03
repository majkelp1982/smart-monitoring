package pl.smarthouse.smartmonitoring.controller;

import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.smarthouse.sharedobjects.dto.error.ErrorPrediction;
import pl.smarthouse.smartmonitoring.service.ErrorHandlingService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/errorhandler")
@RequiredArgsConstructor
public class ErrorHandlingController {
  private final ErrorHandlingService errorHandlingService;

  @GetMapping()
  public Mono<List<ErrorPrediction>> getAllErrorPredictions() {
    return Mono.just(errorHandlingService.getErrorPredictions());
  }

  @GetMapping(value = "/active")
  public Mono<List<ErrorPrediction>> getActiveErrors() {
    return Mono.just(errorHandlingService.getActiveErrorPredictions());
  }

  @GetMapping(value = "/pending")
  public Mono<HashMap<Integer, ErrorPrediction>> getPendingAcknowledgeErrorPredictions() {
    return Mono.just(errorHandlingService.getErrorsPendingAcknowledge());
  }

  @PatchMapping(value = "/pending/acknowledge")
  public void acknowledgePendingError(@PathVariable int hashCode) {
    errorHandlingService.acknowledgePendingError(hashCode);
  }

  @PatchMapping(value = "/pending/clear")
  public void clearPendingAcknowledgeErrors() {
    errorHandlingService.clearPendingError();
  }
}
