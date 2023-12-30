package com.depromeet.domain.mission.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.DatabaseCleaner;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.mission.dto.request.CreateMissionRequest;
import com.depromeet.domain.mission.dto.request.ModifyMissionRequest;
import com.depromeet.domain.mission.dto.response.MissionFindResponse;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MissionServiceTest {

    @Autowired private MissionService missionService;
    @Autowired private MissionRepository missionRepository;
    @Autowired private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
        missionRepository.deleteAll();
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
        missionService.craeteMission(createMissionRequest);

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

        // expected
        assertThatThrownBy(() -> missionService.craeteMission(createMissionRequest))
                // instance 검증
                .isInstanceOf(DataIntegrityViolationException.class)
                // 예외 메시지 확인
                .hasMessageContaining("Value too long for column \"NAME CHARACTER VARYING(20)\"");
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
        Mission saveMission = missionService.craeteMission(createMissionRequest);

        // when
        MissionFindResponse findMission = missionService.findOneMission(saveMission.getId());

        // expected
        assertEquals(findMission.name(), "testMissionName");
        assertEquals(findMission.content(), "testMissionContent");
        assertEquals(findMission.category(), "공부");
        assertEquals(findMission.visibility(), "전체 공개");
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

        createMissionRequests.forEach(request -> missionService.craeteMission(request));
        Pageable pageable = PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "id"));
        // when
        Slice<MissionFindResponse> missionList = missionService.findAllMission(pageable, 30L);

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
        Mission saveMission = missionService.craeteMission(createMissionRequest);
        ModifyMissionRequest modifyMissionRequest =
                new ModifyMissionRequest("modifyName", "modifyContent", MissionVisibility.FOLLOWER);

        // when
        Mission modifyMission =
                missionService.updateMission(modifyMissionRequest, saveMission.getId());

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
        Mission saveMission = missionService.craeteMission(createMissionRequest);

        // when
        missionService.deleteMission(saveMission.getId());

        // expected
        assertThat(missionRepository.findAll()).isEmpty();
        assertThat(missionRepository.count()).isEqualTo(0);
    }
}
