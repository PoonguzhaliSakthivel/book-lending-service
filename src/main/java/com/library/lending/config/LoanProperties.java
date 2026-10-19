package com.library.lending.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "library.loan")
public class LoanProperties {
    @Min(1)
    private int maxActiveLoansPerMember = 3;

    @Min(1)
    private int loanDurationDays = 14;
}
