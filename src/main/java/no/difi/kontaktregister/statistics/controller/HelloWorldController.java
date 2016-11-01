package no.difi.kontaktregister.statistics.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {
    @GetMapping(value = "/hello")
    public String helloWorld() {
        return "Hello world";
    }
}
