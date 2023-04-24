package com.leftycode.autoscarper;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@RequiredArgsConstructor
public class AutoscarperApplication implements CommandLineRunner {

	private final ApplicationContext applicationContext;

	public static void main(String[] args) {
		SpringApplication.run(AutoscarperApplication.class, args);
	}

	@Override
	public void run(String... args) {
		if (args.length == 0) {
			return;
		}
		Runnable runnable = (Runnable) applicationContext.getBean(args[0]);
		runnable.run();
		System.exit(0);
	}
}
