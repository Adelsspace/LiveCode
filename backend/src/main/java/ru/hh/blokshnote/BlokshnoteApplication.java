package ru.hh.blokshnote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BlokshnoteApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlokshnoteApplication.class, args);
	}

}
