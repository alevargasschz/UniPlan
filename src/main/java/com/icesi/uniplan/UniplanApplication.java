package com.icesi.uniplan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.icesi.uniplan.repository.postgres")
@EnableMongoRepositories(basePackages = "com.icesi.uniplan.repository.mongo")
public class UniplanApplication {

	public static void main(String[] args) {
		SpringApplication.run(UniplanApplication.class, args);
	}

}
