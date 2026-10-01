package br.gov.ma.emap.auditoria.config;

import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

//@Component
public class Ambiente {

    private final Environment environment;

    public Ambiente(Environment environment) {
        this.environment = environment;
    }

    //@PostConstruct
    public void inicializar() {
        /*System.out.println(environment.matchesProfiles("dev", "test"));
        System.out.println(environment.matchesProfiles("!prod"));
        System.out.println(environment.matchesProfiles("dev & local"));*/
        String[] profiles = environment.getActiveProfiles();
        System.out.println("Profiles ativos: " + String.join(", ", profiles));
    }
}