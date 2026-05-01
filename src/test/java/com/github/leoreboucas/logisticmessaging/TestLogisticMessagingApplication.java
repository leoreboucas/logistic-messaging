package com.github.leoreboucas.logisticmessaging;

import org.springframework.boot.SpringApplication;

public class TestLogisticMessagingApplication {

    public static void main(String[] args) {
        SpringApplication.from(LogisticMessagingApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
