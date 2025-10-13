package com.gpis.marketplace_link.dto.incidence;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReportResponse {

    private Long incidenceId;
    private Long productId;
    private Long reportId;
    private String message;
    private LocalDateTime createdAt;

}
