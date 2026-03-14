package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemErrorLogResponse {

    private List<ErrorLog> logs;
    private long total;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorLog {
        private String id;
        private String level; // ERROR, WARN, INFO
        private String message;
        private String exception;
        private String stackTrace;
        private String source;
        private LocalDateTime timestamp;
    }
}
