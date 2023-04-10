package pl.smarthouse.smartmonitoring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@EnableScheduling
@Service
@RequiredArgsConstructor
public class MonitoringScheduler {
  private final MonitoringService monitoringService;

  @Scheduled(initialDelay = 5000, fixedDelay = 10000)
  void monitoringScheduler() {
    monitoringService.process();
  }
}
