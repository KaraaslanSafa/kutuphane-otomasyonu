package com.kutuphane.otomasyon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KutuphaneOtomasyonApplication {

	public static void main(String[] args) {
		SpringApplication.run(KutuphaneOtomasyonApplication.class, args);
	}

}
