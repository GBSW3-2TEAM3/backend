package com.example.walkinggo.service;

import com.example.walkinggo.dto.WalkLogResponse;
import com.example.walkinggo.entity.SharedRoute;
import com.example.walkinggo.entity.WalkLog;
import com.example.walkinggo.repository.SharedRouteRepository;
import com.example.walkinggo.repository.WalkLogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final SharedRouteRepository sharedRouteRepository;
    private final WalkLogRepository walkLogRepository;

    @Transactional
    public UUID createShareLink(Long walkLogId, String username) {
        WalkLog walkLog = walkLogRepository.findById(walkLogId)
                .orElseThrow(() -> new EntityNotFoundException("해당 산책 기록을 찾을 수 없습니다."));

        if (!walkLog.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("자신의 산책 기록만 공유할 수 있습니다.");
        }

        return sharedRouteRepository.findByWalkLogId(walkLogId)
                .map(SharedRoute::getShareId)
                .orElseGet(() -> {
                    SharedRoute newSharedRoute = SharedRoute.builder()
                            .shareId(UUID.randomUUID())
                            .walkLog(walkLog)
                            .build();
                    return sharedRouteRepository.save(newSharedRoute).getShareId();
                });
    }

    @Transactional(readOnly = true)
    public WalkLogResponse getSharedRoute(UUID shareId) {
        SharedRoute sharedRoute = sharedRouteRepository.findByShareId(shareId)
                .orElseThrow(() -> new EntityNotFoundException("유효하지 않은 공유 링크입니다."));

        return WalkLogResponse.fromEntity(sharedRoute.getWalkLog());
    }
}