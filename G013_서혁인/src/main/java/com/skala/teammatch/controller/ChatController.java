package com.skala.teammatch.controller;

import com.skala.teammatch.dto.ChatRoomDto;
import com.skala.teammatch.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "채팅방 관리", description = "프로젝트 생성 시 자동 개설되는 프로젝트 팀 채팅방 조회 API")
public class ChatController {

    private final ChatRoomService chatRoomService;

    @GetMapping("/api/projects/{projectId}/chatroom")
    @Operation(summary = "프로젝트 채팅방 조회",
            description = "프로젝트 생성과 동시에 리더만 있는 채팅방으로 개설되며, 지원이 수락될 때마다 팀원이 추가된다")
    public ResponseEntity<ChatRoomDto> getRoomByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(chatRoomService.getRoomByProject(projectId));
    }
}
