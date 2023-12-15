package pl.smarthouse.smartmonitoring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@ComponentScan(basePackages = {"pl.smarthouse.smartmonitoring"})
@EnableScheduling
@Service
@RequiredArgsConstructor
public class Scheduler {
  private final MonitoringService monitoringService;
  private final ErrorHandlingService errorHandlingService;

  @Scheduled(initialDelay = 60000, fixedDelay = 10000)
  void monitoringScheduler() {
    monitoringService.process();
  }

  @Scheduled(initialDelay = 60000, fixedDelay = 10000)
  void errorHandlingScheduler() {
    errorHandlingService.process();
  }
}
