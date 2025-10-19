package com.gpis.marketplace_link.services.appeal;

import com.gpis.marketplace_link.entities.Appeal;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AppealService {

    void autoAssignModeratorsToPendingAppeals();

}
