package com.grongo.cloud_storage_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CloudStorageAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudStorageAppApplication.class, args);
	}

}
