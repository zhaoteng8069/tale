package com.tale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Tale启动类
 *
 * @author biezhi
 */
@SpringBootApplication(scanBasePackages = "com.tale")
public class Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.run(args);
        System.out.println("----------TALE START SUCCESS---------------");
    }

}