package com.finm.notification.controller;

import com.finm.notification.dto.NotificationResponseDto;
import com.finm.notification.service.HistoryService;
import com.finm.notification.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;
    private final SseEmitterService sseEmitterService;

    // SSE 실시간 알림 구독
    @GetMapping(value = "/subscribe/{accountNumber}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long accountNumber) {
        return sseEmitterService.subscribe(accountNumber);
    }

    // 💡 1. 사용자 기준 알림 목록 조회 (GET /api/notifications)
    // 기존 /history/{accountNumber} 경로 삭제 및 명세서(사용자 ID 기반) 규격에 맞춤
    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getNotifications(@RequestParam Long userId) {
        List<NotificationResponseDto> response = historyService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    // 💡 2. 알림 읽음 처리 API (PATCH /api/notifications/{notificationId}/read)
    // 기존 /history/{historyId}/read 에서 /history 경로 제거
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        historyService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }
}