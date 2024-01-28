package com.depromeet.domain.mission.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.DatabaseCleaner;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.OauthInfo;
import com.depromeet.domain.mission.application.MissionService;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.domain.mission.dto.request.MissionUpdateRequest;
import com.depromeet.domain.mission.dto.response.MissionCreateResponse;
import com.depromeet.domain.mission.dto.response.MissionFindAllResponse;
import com.depromeet.domain.mission.dto.response.MissionFindResponse;
import com.depromeet.domain.mission.dto.response.MissionUpdateResponse;
import com.depromeet.global.security.PrincipalDetails;
import com.depromeet.global.util.MemberUtil;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
class MissionServiceTest {

    @Autowired private MissionService missionService;
    @Autowired private MissionRepository missionRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private DatabaseCleaner databaseCleaner;
    @Autowired private EntityManager entityManager;
    @Autowired private MemberUtil memberUtil;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
        PrincipalDetails principal = new PrincipalDetails(1L, "USER");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Member guestMember =
                Member.createNormalMember(OauthInfo.createOauthInfo(null, null, null), "nickname");
        memberRepository.save(guestMember);
    }

    @Test
    void 공부미션을_생성한다() {
        // given
        MissionCreateRequest missionCreateRequest =
                new MissionCreateRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL);

        // when
        MissionCreateResponse mission = missionService.createMission(missionCreateRequest);

        // then
        assertNotNull(mission);
        assertEquals("testMissionName", mission.name());
        assertEquals("testMissionContent", mission.content());
        assertEquals(MissionCategory.STUDY, mission.category());
        assertEquals(MissionVisibility.ALL, mission.visibility());
    }

    @Test
    void 미션_단건_조회한다() {
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

        // then
        assertEquals(findMission.name(), "testMissionName");
        assertEquals(findMission.content(), "testMissionContent");
        assertEquals(findMission.category(), MissionCategory.STUDY);
        assertEquals(findMission.visibility(), MissionVisibility.ALL);
    }

    @Test
    @Transactional
    void 미션_리스트를_조회한다() {
        // given
        LocalDateTime startedAt = LocalDateTime.now();

        IntStream.range(1, 5)
                .mapToObj(
                        i ->
                                new MissionCreateRequest(
                                        "testMissionName_" + i,
                                        "testMissionContent_" + i,
                                        MissionCategory.STUDY,
                                        MissionVisibility.ALL))
                .forEach(
                        request ->
                                entityManager.persist(
                                        Mission.createMission(
                                                request.name(),
                                                request.content(),
                                                1,
                                                request.category(),
                                                request.visibility(),
                                                startedAt,
                                                startedAt.plusWeeks(2),
                                                memberUtil.getCurrentMember())));

        // when
        List<MissionFindAllResponse> missionList = missionService.findAllMission();

        // then
        assertThat(missionList.size()).isEqualTo(4);
        assertThat(missionList)
                .extracting("missionId", "name", "content")
                .containsExactlyInAnyOrder(
                        tuple(1L, "testMissionName_1", "testMissionContent_1"),
                        tuple(2L, "testMissionName_2", "testMissionContent_2"),
                        tuple(3L, "testMissionName_3", "testMissionContent_3"),
                        tuple(4L, "testMissionName_4", "testMissionContent_4"));
    }

    @Test
    void 미션_단건_수정한다() {
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
        assertEquals(modifyMission.missionId(), 1L);
    }

    @Test
    void 미션이름에_null값은_미션수정_실패한다() {
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

        // when, then
        assertThatThrownBy(
                        () ->
                                missionService.updateMission(
                                        missionUpdateRequest, saveMission.missionId()))
                // instance 검증
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 미션이름_20자_초과하면_미션수정_실패한다() {
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

        // when, then
        assertThatThrownBy(
                        () ->
                                missionService.updateMission(
                                        missionUpdateRequest, saveMission.missionId()))
                // instance 검증
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 미션_단건_삭제한다() {
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

        // then
        assertThat(missionRepository.findAll()).isEmpty();
        assertThat(missionRepository.count()).isEqualTo(0);
    }

    @Test
    void 미션_삭제_시_존재하지_않는_아이디라면_삭제되지_않는다() {
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

        // then
        assertThat(missionRepository.findAll()).isNotEmpty();
    }
}
