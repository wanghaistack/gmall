package com.atguigu.gmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.atguigu.gmall")
public class GmallIndentWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallIndentWebApplication.class, args);
	}
}
