package ru.hh.blokshnote.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ExecutorConfig {


  @Bean(name = "sharedScheduledExecutorService", destroyMethod = "shutdown")
  public ScheduledExecutorService sharedScheduledExecutorService() {
    int numOfCores = Runtime.getRuntime().availableProcessors();
    return Executors.newScheduledThreadPool(numOfCores);
  }
}
