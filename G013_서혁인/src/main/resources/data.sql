-- =========================================================================
-- 초기 시드 데이터
-- create-drop 이라 서버를 재시작할 때마다 스키마가 새로 만들어지고 이 스크립트가 다시 실행된다.
-- id 컬럼은 명시하지 않고 IDENTITY 자동 증가에 맡기며, 아래 각 섹션에 실제 배정되는 id를 주석으로 남긴다.
-- =========================================================================

-- 회원 10명 (id 1~10). 8, 9번은 자기소개/포트폴리오를 일부러 비워 "이력서 미작성" 예외 시연용으로 둔다.
INSERT INTO members (email, password, nickname, introduction, portfolio_url, created_at) VALUES
('kim.dev@example.com',     'pw1234', '김개발',   '3년차 백엔드 개발자입니다. Spring Boot와 JPA를 주로 사용하며 사이드 프로젝트 리딩 경험이 있습니다.', 'https://github.com/kimdev',     CURRENT_TIMESTAMP),
('lee.front@example.com',   'pw1234', '이프론트', 'React와 TypeScript를 좋아하는 프론트엔드 개발자입니다. UI 디테일에 진심입니다.',                       'https://github.com/leefront',   CURRENT_TIMESTAMP),
('park.back@example.com',   'pw1234', '박백엔드', '대규모 트래픽 처리 경험이 있는 백엔드 개발자입니다. MySQL 튜닝에 관심이 많습니다.',                     'https://github.com/parkback',   CURRENT_TIMESTAMP),
('choi.ai@example.com',     'pw1234', '최에이아이', 'LLM 기반 서비스 개발에 관심 많은 AI 엔지니어입니다. PyTorch, LangChain을 다룹니다.',                     'https://github.com/choiai',     CURRENT_TIMESTAMP),
('jung.infra@example.com',  'pw1234', '정인프라', 'AWS와 Docker/Kubernetes로 인프라를 구축하는 걸 좋아합니다. CI/CD 파이프라인 구축 경험 다수.',           'https://github.com/junginfra',  CURRENT_TIMESTAMP),
('han.design@example.com',  'pw1234', '한디자인', 'Figma로 UI/UX 디자인을 하다가 프론트엔드 개발까지 넘어온 디자이너 출신 개발자입니다.',                   'https://github.com/handesign',  CURRENT_TIMESTAMP),
('song.pm@example.com',     'pw1234', '송피엠',   '여러 해커톤에서 PM을 맡았습니다. 일정 관리와 커뮤니케이션에 자신 있습니다.',                             'https://github.com/songpm',     CURRENT_TIMESTAMP),
('yoon.student@example.com','pw1234', '윤학생',   NULL,                                                                                                  NULL,                             CURRENT_TIMESTAMP),
('jang.newbie@example.com', 'pw1234', '장새내기', '아직 배우는 중입니다',                                                                                NULL,                             CURRENT_TIMESTAMP),
('seo.hyeokin@example.com', 'pw1234', '서혁인',   '백엔드 실습과제를 진행 중인 개발자입니다. Spring Boot, JPA, MyBatis를 학습하고 있습니다.',             'https://github.com/seohyeokin', CURRENT_TIMESTAMP);

-- 프로젝트 6개 (id 1~6): RECRUITING 3개(1,2,6), CLOSED 1개(3, 정원 마감), COMPLETED 2개(4,5, 랭킹 통계용)
INSERT INTO projects (leader_id, title, description, status, deadline, created_at) VALUES
(1, 'AI 이력서 첨삭 서비스',       'AI로 이력서/자기소개서를 첨삭해주는 서비스를 만듭니다. LLM 프롬프트 엔지니어링과 프론트엔드 UI에 자신 있는 분을 찾습니다.', 'RECRUITING', DATEADD('DAY', 14, CURRENT_DATE),  DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(2, '실시간 협업 화이트보드',     '여러 명이 동시에 그릴 수 있는 실시간 협업 화이트보드 웹앱입니다. WebSocket 기반 실시간 동기화가 핵심입니다.',                'RECRUITING', DATEADD('DAY', 21, CURRENT_DATE),  DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(3, '중고거래 안전결제 플랫폼',   '에스크로 방식의 안전결제를 지원하는 중고거래 플랫폼입니다.',                                                                'CLOSED',     DATEADD('DAY', -3, CURRENT_DATE),  DATEADD('DAY', -20, CURRENT_TIMESTAMP)),
(7, '포트폴리오 챗봇 빌더',       '대화형으로 질문에 답하면 포트폴리오 페이지를 자동 생성해주는 챗봇 빌더입니다.',                                            'COMPLETED',  DATEADD('DAY', -30, CURRENT_DATE), DATEADD('DAY', -60, CURRENT_TIMESTAMP)),
(10,'취업 스터디 매칭 앱',        '관심 직무/지역이 비슷한 사람끼리 스터디 그룹을 매칭해주는 앱입니다.',                                                      'COMPLETED',  DATEADD('DAY', -10, CURRENT_DATE), DATEADD('DAY', -40, CURRENT_TIMESTAMP)),
(4, '그린 리워드 챌린지 앱',       '친환경 습관을 인증하면 포인트를 적립해주는 리워드 앱입니다.',                                                              'RECRUITING', DATEADD('DAY', 30, CURRENT_DATE),  CURRENT_TIMESTAMP);

-- 역할별 모집 정원 (id 1~18)
INSERT INTO recruit_slots (project_id, role, capacity, filled_count) VALUES
(1, 'PM', 1, 0), (1, 'FRONTEND', 2, 0), (1, 'AI', 1, 0), (1, 'INFRA', 1, 0),                 -- 1~4
(2, 'BACKEND', 2, 0), (2, 'PM', 1, 0), (2, 'INFRA', 1, 0),                                    -- 5~7
(3, 'FRONTEND', 1, 1), (3, 'PM', 1, 1), (3, 'INFRA', 1, 1),                                   -- 8~10 (정원 마감)
(4, 'BACKEND', 1, 1), (4, 'AI', 1, 1), (4, 'FRONTEND', 1, 1),                                 -- 11~13
(5, 'FRONTEND', 1, 1), (5, 'BACKEND', 1, 1),                                                  -- 14~15
(6, 'FRONTEND', 1, 0), (6, 'BACKEND', 1, 0), (6, 'PM', 1, 0);                                 -- 16~18

-- 프로젝트별 기술 스택 태그 (TOP10 수요 통계 시드 - Spring Boot/React/Figma 가 4건으로 가장 많다)
INSERT INTO project_tech_stacks (project_id, tech_name) VALUES
(1, 'Spring Boot'), (1, 'React'), (1, 'PyTorch'), (1, 'AWS'), (1, 'Figma'),
(2, 'React'), (2, 'WebSocket'), (2, 'Figma'), (2, 'Node.js'),
(3, 'Spring Boot'), (3, 'MySQL'), (3, 'Docker'), (3, 'Kotlin'),
(4, 'Spring Boot'), (4, 'LangChain'), (4, 'React'),
(5, 'React'), (5, 'Spring Boot'), (5, 'Figma'),
(6, 'Flutter'), (6, 'Firebase'), (6, 'Python'), (6, 'Figma');

-- 팀장은 프로젝트 생성과 동시에 팀원으로 자동 합류한다
INSERT INTO project_members (project_id, member_id, role, joined_at) VALUES
(1, 1, 'BACKEND', DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(2, 2, 'FRONTEND', DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(3, 3, 'BACKEND', DATEADD('DAY', -20, CURRENT_TIMESTAMP)),
(4, 7, 'PM', DATEADD('DAY', -60, CURRENT_TIMESTAMP)),
(5, 10, 'PM', DATEADD('DAY', -40, CURRENT_TIMESTAMP)),
(6, 4, 'AI', CURRENT_TIMESTAMP),
-- 지원 수락으로 합류가 확정된 팀원들 (아래 team_applications 의 ACCEPTED 건과 1:1 대응)
(3, 6, 'FRONTEND', DATEADD('DAY', -18, CURRENT_TIMESTAMP)),
(3, 7, 'PM',       DATEADD('DAY', -17, CURRENT_TIMESTAMP)),
(3, 5, 'INFRA',    DATEADD('DAY', -16, CURRENT_TIMESTAMP)),
(4, 1, 'BACKEND',  DATEADD('DAY', -58, CURRENT_TIMESTAMP)),
(4, 4, 'AI',       DATEADD('DAY', -57, CURRENT_TIMESTAMP)),
(4, 2, 'FRONTEND', DATEADD('DAY', -56, CURRENT_TIMESTAMP)),
(5, 2, 'FRONTEND', DATEADD('DAY', -38, CURRENT_TIMESTAMP)),
(5, 1, 'BACKEND',  DATEADD('DAY', -37, CURRENT_TIMESTAMP));

-- 지원 내역 (id 1~13): 1~8은 이미 수락되어 위 project_members 와 짝을 이루고, 9~12는 PENDING, 13은 REJECTED
INSERT INTO team_applications (project_id, slot_id, applicant_id, status, message, applied_at, decided_at) VALUES
(3, 8,  6, 'ACCEPTED', '프론트엔드로 참여하고 싶습니다', DATEADD('DAY', -19, CURRENT_TIMESTAMP), DATEADD('DAY', -18, CURRENT_TIMESTAMP)),
(3, 9,  7, 'ACCEPTED', 'PM 경험을 살리고 싶습니다',       DATEADD('DAY', -19, CURRENT_TIMESTAMP), DATEADD('DAY', -17, CURRENT_TIMESTAMP)),
(3, 10, 5, 'ACCEPTED', '인프라 구축 경험이 있습니다',     DATEADD('DAY', -19, CURRENT_TIMESTAMP), DATEADD('DAY', -16, CURRENT_TIMESTAMP)),
(4, 11, 1, 'ACCEPTED', '백엔드로 지원합니다',             DATEADD('DAY', -59, CURRENT_TIMESTAMP), DATEADD('DAY', -58, CURRENT_TIMESTAMP)),
(4, 12, 4, 'ACCEPTED', 'AI 파트를 맡고 싶습니다',         DATEADD('DAY', -59, CURRENT_TIMESTAMP), DATEADD('DAY', -57, CURRENT_TIMESTAMP)),
(4, 13, 2, 'ACCEPTED', '프론트엔드로 지원합니다',         DATEADD('DAY', -59, CURRENT_TIMESTAMP), DATEADD('DAY', -56, CURRENT_TIMESTAMP)),
(5, 14, 2, 'ACCEPTED', '스터디 매칭 UI를 만들고 싶습니다', DATEADD('DAY', -39, CURRENT_TIMESTAMP), DATEADD('DAY', -38, CURRENT_TIMESTAMP)),
(5, 15, 1, 'ACCEPTED', '백엔드 API를 맡고 싶습니다',       DATEADD('DAY', -39, CURRENT_TIMESTAMP), DATEADD('DAY', -37, CURRENT_TIMESTAMP)),
(1, 2,  6, 'PENDING',  '프론트엔드로 참여하고 싶습니다',   DATEADD('DAY', -1, CURRENT_TIMESTAMP), NULL),
(1, 3,  4, 'PENDING',  'AI 모델링을 맡고 싶습니다',        CURRENT_TIMESTAMP, NULL),
(2, 5,  3, 'PENDING',  '백엔드 API 설계 경험이 있습니다',  DATEADD('DAY', -1, CURRENT_TIMESTAMP), NULL),
(6, 16, 6, 'PENDING',  '프론트엔드로 참여하고 싶습니다',   CURRENT_TIMESTAMP, NULL),
(2, 6,  5, 'REJECTED', 'PM 역할에 지원합니다',             DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP));

-- 합격/불합격 처리 감사 로그: 위 team_applications 의 처리 결과와 대응 (실제로는 DecisionAuditAspect 가 자동으로 쌓는다)
INSERT INTO application_decision_logs (application_id, decision, success, message, created_at) VALUES
(1, 'ACCEPT', TRUE, '[성공] ACCEPT 처리 - 지원자: 한디자인, 프로젝트: 중고거래 안전결제 플랫폼, 역할: FRONTEND', DATEADD('DAY', -18, CURRENT_TIMESTAMP)),
(4, 'ACCEPT', TRUE, '[성공] ACCEPT 처리 - 지원자: 김개발, 프로젝트: 포트폴리오 챗봇 빌더, 역할: BACKEND',       DATEADD('DAY', -58, CURRENT_TIMESTAMP)),
(13,'REJECT', TRUE, '[성공] REJECT 처리 - 지원자: 정인프라, 프로젝트: 실시간 협업 화이트보드, 역할: PM',         DATEADD('DAY', -1, CURRENT_TIMESTAMP));

-- 팀 채팅방: 지원이 1건 이상 수락된 프로젝트(3, 4, 5)는 팀원이 여럿인 채팅방(id 1~3),
-- 아직 지원 수락이 없는 RECRUITING 프로젝트(1, 2, 6)는 리더 혼자만 있는 채팅방(id 4~6)이다.
-- (프로젝트 생성 시 리더 전용 채팅방이 즉시 개설되도록 ProjectService/ChatRoomService 가 보장한다)
INSERT INTO chat_rooms (project_id, created_at) VALUES
(3, DATEADD('DAY', -18, CURRENT_TIMESTAMP)),
(4, DATEADD('DAY', -58, CURRENT_TIMESTAMP)),
(5, DATEADD('DAY', -38, CURRENT_TIMESTAMP)),
(1, DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(2, DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(6, CURRENT_TIMESTAMP);

INSERT INTO chat_room_members (chat_room_id, member_id, joined_at) VALUES
(1, 3, DATEADD('DAY', -18, CURRENT_TIMESTAMP)), (1, 6, DATEADD('DAY', -18, CURRENT_TIMESTAMP)),
(1, 7, DATEADD('DAY', -17, CURRENT_TIMESTAMP)), (1, 5, DATEADD('DAY', -16, CURRENT_TIMESTAMP)),
(2, 7, DATEADD('DAY', -58, CURRENT_TIMESTAMP)), (2, 1, DATEADD('DAY', -58, CURRENT_TIMESTAMP)),
(2, 4, DATEADD('DAY', -57, CURRENT_TIMESTAMP)), (2, 2, DATEADD('DAY', -56, CURRENT_TIMESTAMP)),
(3, 10, DATEADD('DAY', -38, CURRENT_TIMESTAMP)), (3, 2, DATEADD('DAY', -38, CURRENT_TIMESTAMP)),
(3, 1, DATEADD('DAY', -37, CURRENT_TIMESTAMP)),
(4, 1, DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(5, 2, DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
(6, 4, CURRENT_TIMESTAMP);
