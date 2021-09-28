package com.justafewmistakes.nim.route;

import com.justafewmistakes.nim.route.kit.NacosGatewayListener;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class RouteApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(RouteApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        Thread thread = new Thread(new NacosGatewayListener());
        thread.setName("nacos-listener");
        thread.start();
    }
}
