package com.retailmanager.rmpaydashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.retailmanager.rmpaydashboard.backgroundRoutines.BackgroundRoutines;

@SpringBootApplication
public class RmpaydashboardApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context=SpringApplication.run(RmpaydashboardApplication.class, args);

		BackgroundRoutines backgroundRoutines = new BackgroundRoutines(context);
		backgroundRoutines.start();

	}

}
