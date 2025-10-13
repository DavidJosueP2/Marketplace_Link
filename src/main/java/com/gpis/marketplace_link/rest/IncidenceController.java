package com.gpis.marketplace_link.rest;

import com.gpis.marketplace_link.dto.incidence.ReportResponse;
import com.gpis.marketplace_link.dto.incidence.RequestReportProduct;
import com.gpis.marketplace_link.services.incidence.IncidenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/incidences")
public class IncidenceController {

    private final IncidenceService incidenceService;

    @PostMapping("/report")
    public ReportResponse report(@Valid @RequestBody RequestReportProduct req) {
        return incidenceService.report(req);
    }

}
