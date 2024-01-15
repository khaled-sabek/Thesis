package com.example.application;


import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@Push
@Theme(value = "GCPro")
@PWA(name = "Google Classroom Pro",
        shortName = "GCPRO",
        offlinePath = "META-INF/resources/offline.html",
        offlineResources = { "images/offline.png", "META-INF/resources/offline.html"})
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
