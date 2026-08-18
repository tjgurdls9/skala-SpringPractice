package com.skala.teammatch.service;

import com.skala.teammatch.dto.ChatRoomDto;
import com.skala.teammatch.dto.MemberSummaryDto;
import com.skala.teammatch.entity.ChatRoom;
import com.skala.teammatch.entity.ChatRoomMember;
import com.skala.teammatch.entity.Member;
import com.skala.teammatch.entity.Project;
import com.skala.teammatch.exception.ResourceNotFoundException;
import com.skala.teammatch.repository.ChatRoomMemberRepository;
import com.skala.teammatch.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    /**
     * 프로젝트 생성 시 호출된다. 리더 혼자만 있는 채팅방을 바로 개설해서, 지원자가 아직 한 명도
     * 수락되지 않은 상태에서도 리더가 "내 프로젝트 채팅방"에 접속할 수 있게 한다.
     */
    @Transactional
    public void createRoomForProject(Project project) {
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.builder().project(project).build());
        addMemberIfAbsent(chatRoom, project.getLeader());
    }

    /**
     * 지원 수락 시 호출된다. (프로젝트 생성 시 이미 채팅방이 만들어져 있지만, 그 이전에 만들어진
     * 데이터를 위해 없으면 새로 개설하는 방어 로직도 남겨둔다.) 새로 합류한 팀원을 참여자로 추가한다.
     */
    @Transactional
    public void ensureRoomAndAddMember(Project project, Member newMember) {
        ChatRoom chatRoom = chatRoomRepository.findByProjectId(project.getId())
                .orElseGet(() -> {
                    ChatRoom created = chatRoomRepository.save(ChatRoom.builder().project(project).build());
                    addMemberIfAbsent(created, project.getLeader());
                    return created;
                });
        addMemberIfAbsent(chatRoom, newMember);
    }

    public ChatRoomDto getRoomByProject(Long projectId) {
        ChatRoom chatRoom = chatRoomRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("아직 개설된 채팅방이 없습니다. 프로젝트 ID: " + projectId));

        List<MemberSummaryDto> members = chatRoomMemberRepository.findByChatRoomId(chatRoom.getId()).stream()
                .map(cm -> MemberSummaryDto.builder()
                        .id(cm.getMember().getId())
                        .nickname(cm.getMember().getNickname())
                        .build())
                .collect(Collectors.toList());

        return ChatRoomDto.builder()
                .id(chatRoom.getId())
                .projectId(projectId)
                .projectTitle(chatRoom.getProject().getTitle())
                .createdAt(chatRoom.getCreatedAt())
                .members(members)
                .build();
    }

    private void addMemberIfAbsent(ChatRoom chatRoom, Member member) {
        if (!chatRoomMemberRepository.existsByChatRoomIdAndMemberId(chatRoom.getId(), member.getId())) {
            chatRoomMemberRepository.save(ChatRoomMember.builder()
                    .chatRoom(chatRoom)
                    .member(member)
                    .build());
        }
    }
}
