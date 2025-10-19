package com.gpis.marketplace_link.services.appeal;

import com.gpis.marketplace_link.entities.Appeal;
import com.gpis.marketplace_link.entities.User;
import com.gpis.marketplace_link.enums.AppealStatus;
import com.gpis.marketplace_link.exceptions.business.users.ModeratorNotFoundException;
import com.gpis.marketplace_link.repositories.AppealRepository;
import com.gpis.marketplace_link.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AppealServiceImp implements AppealService {

    private final AppealRepository appealRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public void autoAssignModeratorsToPendingAppeals() {
        // primero buscar todas las pendientes
        List<Appeal> pendingAppeals = appealRepository.findAllPendingWithModerator();

        for (Appeal appeal : pendingAppeals) {
            // para cada apeal pendiente, asignar un nuevo moderador (sin considerar el de la incidencia de ese apppeal)
            Long moderatorIncidence = appeal.getIncidence().getModerator().getId();
            Long newModeratorId = userRepository.findLeastBusyModeratorExcludingId(moderatorIncidence);

            if (newModeratorId != null) {
                User newModUser = userRepository
                        .findById(newModeratorId)
                        .orElseThrow(() -> new ModeratorNotFoundException("Moderator with ID " + newModeratorId + " not found"));

                appeal.setNewModerator(newModUser);
                appeal.setStatus(AppealStatus.ASSIGNED);
                appealRepository.save(appeal);
            }
        }
    }
}
