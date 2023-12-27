package com.depromeet.domain.mission.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.mission.dto.request.CreateMissionRequest;
import com.depromeet.domain.mission.dto.request.ModifyMissionRequest;
import com.depromeet.domain.mission.dto.response.MissionResponse;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Slice;

@SpringBootTest
class MissionServiceTest {

    @Autowired private MissionService missionService;
    @Autowired private MissionRepository missionRepository;
    @Autowired private MemberRepository memberRepository;

    Member member;
    Member saveMember;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        missionRepository.deleteAll();
        member = Member.createNormalMember(new Profile("testNickname", "testProfileImageUrl"));
        saveMember = memberRepository.save(member);
    }

    @AfterEach
    void tearDown() {
        missionRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void 공부_미션_생성_성공() {
        // given
        CreateMissionRequest createMissionRequest =
                new CreateMissionRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL);

        // when
        missionService.addMission(createMissionRequest, saveMember.getId());

        // expected
        Mission mission = missionRepository.findAll().get(0);
        assertEquals("testMissionName", mission.getName());
        assertEquals("testMissionContent", mission.getContent());
    }

    @Test
    void 미션이름_초과_생성_실패() {
        // given
        CreateMissionRequest createMissionRequest =
                new CreateMissionRequest(
                        "testMissionNameMoreThan",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL);

        // when
        DataIntegrityViolationException e =
                assertThrows(
                        DataIntegrityViolationException.class,
                        () -> {
                            missionService.addMission(createMissionRequest, saveMember.getId());
                        });

        // expected
        // 예외 메시지 확인
        assertTrue(
                e.getMessage()
                        .contains("Value too long for column \"NAME CHARACTER VARYING(20)\""));
    }

    @Test
    void 미션_단건_조회_성공() {
        // given
        CreateMissionRequest createMissionRequest =
                new CreateMissionRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL);
        Mission saveMission = missionService.addMission(createMissionRequest, saveMember.getId());

        // when
        Mission findMission = missionService.findMission(saveMission.getId());

        // expected
        assertEquals(findMission.getName(), "testMissionName");
        assertEquals(findMission.getContent(), "testMissionContent");
        assertEquals(findMission.getCategory().getValue(), "공부");
        assertEquals(findMission.getVisibility().getValue(), "전체 공개");
    }

    @Test
    void 미션_리스트_조회_성공() {
        // given
        List<CreateMissionRequest> createMissionRequests =
                IntStream.range(1, 41)
                        .mapToObj(
                                i ->
                                        new CreateMissionRequest(
                                                "testMissionName_" + i,
                                                "testMissionContent_" + i,
                                                MissionCategory.STUDY,
                                                MissionVisibility.ALL))
                        .toList();

        createMissionRequests.forEach(
                request -> missionService.addMission(request, saveMember.getId()));
        // when
        Slice<MissionResponse> missionList = missionService.listMission(saveMember.getId(), 4, 30L);

        // expected
        assertThat(missionList.getContent().size()).isEqualTo(4);
        assertThat(missionList.getContent())
                .hasSize(4)
                .extracting("missionId", "name", "content")
                .containsExactlyInAnyOrder(
                        tuple(29L, "testMissionName_29", "testMissionContent_29"),
                        tuple(28L, "testMissionName_28", "testMissionContent_28"),
                        tuple(27L, "testMissionName_27", "testMissionContent_27"),
                        tuple(26L, "testMissionName_26", "testMissionContent_26"));
        assertFalse(missionList.isLast());
    }

    @Test
    void 미션_수정_성공() {
        // given
        CreateMissionRequest createMissionRequest =
                new CreateMissionRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL);
        Mission saveMission = missionService.addMission(createMissionRequest, saveMember.getId());
        ModifyMissionRequest modifyMissionRequest =
                new ModifyMissionRequest("modifyName", "modifyContent", MissionVisibility.FOLLOWER);

        // when
        Mission modifyMission =
                missionService.modifyMission(modifyMissionRequest, saveMission.getId());

        // expected
        assertEquals(modifyMission.getName(), "modifyName");
        assertEquals(modifyMission.getContent(), "modifyContent");
        assertEquals(modifyMission.getVisibility().getValue(), "팔로워에게 공개");
    }

    @Test
    void 미션_삭제_성공() {
        // given
        CreateMissionRequest createMissionRequest =
                new CreateMissionRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL);
        Mission saveMission = missionService.addMission(createMissionRequest, saveMember.getId());

        // when
        missionService.removeMission(saveMission.getId());

        // expected
        assertThat(missionRepository.findAll()).isEmpty();
        assertThat(missionRepository.count()).isEqualTo(0);
    }
}
