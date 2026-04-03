package de.mediguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MediGuardApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediGuardApplication.class, args);
        System.out.println("""
                ╔══════════════════════════════════════════════════╗
                ║         MediGuard Deutschland  v1.0.0           ║
                ║  DSGVO-compliant Healthcare & Security Platform  ║
                ║  Swagger UI → http://localhost:8080/swagger-ui.html ║
                ║  H2 Console → http://localhost:8080/h2-console  ║
                ║  Demo:  admin / MediGuard2024!                  ║
                ╚══════════════════════════════════════════════════╝
                """);
    }
}
