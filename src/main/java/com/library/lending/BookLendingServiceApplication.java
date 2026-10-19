package com.library.lending;

import com.library.lending.config.LoanProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(LoanProperties.class)
@SpringBootApplication
public class BookLendingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookLendingServiceApplication.class, args);
    }

}
