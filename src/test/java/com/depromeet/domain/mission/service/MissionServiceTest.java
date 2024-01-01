package com.depromeet.domain.mission.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.DatabaseCleaner;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.domain.mission.dto.request.MissionUpdateRequest;
import com.depromeet.domain.mission.dto.response.MissionCreateResponse;
import com.depromeet.domain.mission.dto.response.MissionFindResponse;
import com.depromeet.domain.mission.dto.response.MissionUpdateResponse;
import com.depromeet.global.error.exception.CustomException;
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
        MissionCreateRequest missionCreateRequest =
                new MissionCreateRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL);

        // when
        missionService.createMission(missionCreateRequest);

        // expected
        Mission mission = missionRepository.findAll().get(0);
        assertEquals("testMissionName", mission.getName());
        assertEquals("testMissionContent", mission.getContent());
        assertEquals(MissionCategory.STUDY, mission.getCategory());
        assertEquals(MissionVisibility.ALL, mission.getVisibility());
    }

    @Test
    void 미션이름_초과_생성_실패() {
        // given
        MissionCreateRequest missionCreateRequest =
                new MissionCreateRequest(
                        "testMissionNameMoreThan",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL);

        // expected
        assertThatThrownBy(() -> missionService.createMission(missionCreateRequest))
                // instance 검증
                .isInstanceOf(DataIntegrityViolationException.class)
                // 예외 메시지 확인
                .hasMessageContaining("Value too long for column \"NAME CHARACTER VARYING(20)\"");
    }

    @Test
    void 미션_단건_조회_성공() {
        // given
        MissionCreateRequest missionCreateRequest =
                new MissionCreateRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL);
        MissionCreateResponse saveMission = missionService.createMission(missionCreateRequest);

        // when
        MissionFindResponse findMission = missionService.findOneMission(saveMission.missionId());

        // expected
        assertEquals(findMission.name(), "testMissionName");
        assertEquals(findMission.content(), "testMissionContent");
        assertEquals(findMission.category(), MissionCategory.STUDY);
        assertEquals(findMission.visibility(), MissionVisibility.ALL);
    }

    @Test
    void 미션_리스트_조회_성공() {
        // given
        List<MissionCreateRequest> missionCreateRequests =
                IntStream.range(1, 41)
                        .mapToObj(
                                i ->
                                        new MissionCreateRequest(
                                                "testMissionName_" + i,
                                                "testMissionContent_" + i,
                                                MissionCategory.STUDY,
                                                MissionVisibility.ALL))
                        .toList();

        missionCreateRequests.forEach(request -> missionService.createMission(request));
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
        MissionCreateRequest missionCreateRequest =
                new MissionCreateRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL);
        MissionCreateResponse saveMission = missionService.createMission(missionCreateRequest);
        MissionUpdateRequest missionUpdateRequest =
                new MissionUpdateRequest("modifyName", "modifyContent", MissionVisibility.FOLLOWER);

        // when
        MissionUpdateResponse modifyMission =
                missionService.updateMission(missionUpdateRequest, saveMission.missionId());

        // expected
        assertEquals(modifyMission.name(), "modifyName");
        assertEquals(modifyMission.content(), "modifyContent");
        assertEquals(modifyMission.visibility(), MissionVisibility.FOLLOWER);
    }

    @Test
    void 미션_이름_null_수정_실패() {
        // given
        MissionCreateRequest missionCreateRequest =
                new MissionCreateRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL);
        MissionCreateResponse saveMission = missionService.createMission(missionCreateRequest);
        MissionUpdateRequest missionUpdateRequest =
                new MissionUpdateRequest(null, "modifyContent", MissionVisibility.FOLLOWER);

        // when & expected
        assertThatThrownBy(
                        () ->
                                missionService.updateMission(
                                        missionUpdateRequest, saveMission.missionId()))
                // instance 검증
                .isInstanceOf(DataIntegrityViolationException.class)
                // 예외 메시지 확인
                .hasMessageContaining("not-null property references a null or transient value");
    }

    @Test
    void 미션_수정_공개여부_null_예외처리() {
        // given
        MissionCreateRequest missionCreateRequest =
                new MissionCreateRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL);
        MissionCreateResponse saveMission = missionService.createMission(missionCreateRequest);
        MissionUpdateRequest missionUpdateRequest =
                new MissionUpdateRequest("modifyName", "modifyContent", null);

        // when
        CustomException exception =
                assertThrows(
                        CustomException.class,
                        () -> {
                            missionService.updateMission(
                                    missionUpdateRequest, saveMission.missionId());
                        });

        // expected
        assertEquals(exception.getMessage(), "미션 공개 여부가 null입니다.");
    }

    @Test
    void 미션_이름_초과_수정_실패() {
        // given
        MissionCreateRequest missionCreateRequest =
                new MissionCreateRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL);
        MissionCreateResponse saveMission = missionService.createMission(missionCreateRequest);
        MissionUpdateRequest missionUpdateRequest =
                new MissionUpdateRequest(
                        "modifyMissionName_test", "modifyContent", MissionVisibility.FOLLOWER);

        // when & expected
        assertThatThrownBy(
                        () ->
                                missionService.updateMission(
                                        missionUpdateRequest, saveMission.missionId()))
                // instance 검증
                .isInstanceOf(DataIntegrityViolationException.class)
                // 예외 메시지 확인
                .hasMessageContaining("Value too long for column \"NAME CHARACTER VARYING(20)\"");
    }

    @Test
    void 미션_삭제_성공() {
        // given
        MissionCreateRequest missionCreateRequest =
                new MissionCreateRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL);
        MissionCreateResponse saveMission = missionService.createMission(missionCreateRequest);

        // when
        missionService.deleteMission(saveMission.missionId());

        // expected
        assertThat(missionRepository.findAll()).isEmpty();
        assertThat(missionRepository.count()).isEqualTo(0);
    }

    @Test
    void 미션_삭제_실패() {
        // given
        MissionCreateRequest missionCreateRequest =
                new MissionCreateRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL);
        missionService.createMission(missionCreateRequest);

        // when
        missionService.deleteMission(200L);

        // expected
        assertThat(missionRepository.findAll()).isNotEmpty();
    }
}
