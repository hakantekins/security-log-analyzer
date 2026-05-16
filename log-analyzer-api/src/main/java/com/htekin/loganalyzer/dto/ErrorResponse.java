package com.htekin.loganalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String title;
    private int status;
    private String detail;
    private LocalDateTime timestamp;
}