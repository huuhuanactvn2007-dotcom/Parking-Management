package com.hotrodoan;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ParkingLotApplication {
	public static void main(String[] args) {
		SpringApplication.run(ParkingLotApplication.class, args);
	}

}
