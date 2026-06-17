package com.bluepatitas.bluepatitasbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BluePatitasBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BluePatitasBackendApplication.class, args);
    }

}
