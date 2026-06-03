package com.shopdog;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class ShopDogApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopDogApplication.class, args);
        log.info(
                "------------------------------------------------------- shopDog start up success ! "
                        + "-------------------------------------------------------");
    }
}
