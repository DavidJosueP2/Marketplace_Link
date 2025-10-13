package com.gpis.marketplace_link.services.incidence;

import com.gpis.marketplace_link.dto.incidence.ReportResponse;
import com.gpis.marketplace_link.dto.incidence.RequestReportProduct;

public interface IncidenceService {

    ReportResponse report(RequestReportProduct req);

}
