package com.depromeet.domain.mission.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.OauthInfo;
import com.depromeet.domain.mission.application.MissionService;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionPeriod;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.domain.mission.dto.request.MissionUpdateRequest;
import com.depromeet.domain.mission.dto.response.MissionCreateResponse;
import com.depromeet.domain.mission.dto.response.MissionFindAllResponse;
import com.depromeet.domain.mission.dto.response.MissionFindResponse;
import com.depromeet.domain.mission.dto.response.MissionStatus;
import com.depromeet.domain.mission.dto.response.MissionUpdateResponse;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.global.security.PrincipalDetails;
import com.depromeet.global.util.MemberUtil;
import jakarta.persistence.EntityManagerFactory;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.IntStream;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MissionServiceTest {

    @Autowired private MissionService missionService;
    @Autowired private MissionRepository missionRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private MemberUtil memberUtil;
    @Autowired private MissionRecordRepository missionRecordRepository;
    @Autowired private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void setUp() {
        PrincipalDetails principal = new PrincipalDetails(1L, "USER");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Member member =
                Member.createNormalMember(OauthInfo.createOauthInfo(null, null, null), "nickname");
        memberRepository.save(member);
    }

    @Test
    void 공부미션을_생성한다() {
        // given
        MissionCreateRequest missionCreateRequest =
                new MissionCreateRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL,
                        MissionPeriod.TWO_WEEKS,
                        LocalTime.of(21, 0));

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
                        MissionVisibility.ALL,
                        MissionPeriod.TWO_WEEKS,
                        LocalTime.of(21, 0));
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
                                        MissionVisibility.ALL,
                                        MissionPeriod.TWO_WEEKS,
                                        LocalTime.of(21, 0)))
                .forEach(
                        request ->
                                missionRepository.save(
                                        Mission.createMission(
                                                request.name(),
                                                request.content(),
                                                1,
                                                request.category(),
                                                request.visibility(),
                                                startedAt,
                                                startedAt.plusWeeks(2),
                                                LocalTime.of(21, 0),
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
    void 미션_리스트는_당일_기록만으로_상태를_판단하고_전체_기록을_로드하지_않는다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Mission todayCompleted = saveMission("todayCompleted", now.minusDays(3));
        Mission pastOnly = saveMission("pastOnly", now.minusDays(3));
        MissionRecord todayRecord = saveCompletedRecord(todayCompleted, now);
        saveCompletedRecord(todayCompleted, now.minusDays(1));
        saveCompletedRecord(pastOnly, now.minusDays(1));
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        // when
        List<MissionFindAllResponse> missionList = missionService.findAllMission();

        // then
        assertThat(missionList)
                .extracting("name", "missionStatus", "missionRecordId")
                .containsExactlyInAnyOrder(
                        tuple("todayCompleted", MissionStatus.COMPLETED, todayRecord.getId()),
                        tuple("pastOnly", MissionStatus.NONE, null));
        assertEquals(0L, statistics.getCollectionLoadCount());
        assertEquals(4L, statistics.getEntityLoadCount()); // member 1 + mission 2 + 당일 기록 1
    }

    @Test
    void 인증사진_업로드중인_미션은_마감시각과_함께_인증필요로_조회된다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusMinutes(10).truncatedTo(ChronoUnit.SECONDS);
        Mission mission = saveMission("uploading", now.minusDays(1));
        MissionRecord record =
                MissionRecord.createMissionRecord(
                        Duration.ofMinutes(20), now, now.plusMinutes(20), mission);
        record.updateUploadDeadlineAt(deadline);
        record.updateUploadStatusPending();
        missionRecordRepository.save(record);

        // when
        List<MissionFindAllResponse> missionList = missionService.findAllMission();

        // then
        assertThat(missionList)
                .extracting("missionStatus", "ttlFinishedAt", "missionRecordId")
                .containsExactly(tuple(MissionStatus.REQUIRED, deadline, record.getId()));
    }

    private Mission saveMission(String name, LocalDateTime startedAt) {
        return missionRepository.save(
                Mission.createMission(
                        name,
                        "content",
                        1,
                        MissionCategory.STUDY,
                        MissionVisibility.ALL,
                        startedAt,
                        startedAt.plusWeeks(2),
                        LocalTime.of(21, 0),
                        memberUtil.getCurrentMember()));
    }

    private MissionRecord saveCompletedRecord(Mission mission, LocalDateTime startedAt) {
        MissionRecord record =
                MissionRecord.createMissionRecord(
                        Duration.ofMinutes(20), startedAt, startedAt.plusMinutes(20), mission);
        record.updateUploadStatusPending();
        record.updateUploadStatusComplete("remark", "imageUrl");
        return missionRecordRepository.save(record);
    }

    @Test
    void 미션_단건_수정한다() {
        // given
        MissionCreateRequest missionCreateRequest =
                new MissionCreateRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL,
                        MissionPeriod.TWO_WEEKS,
                        LocalTime.of(21, 0));
        MissionCreateResponse saveMission = missionService.createMission(missionCreateRequest);
        MissionUpdateRequest missionUpdateRequest =
                new MissionUpdateRequest(
                        "modifyName", "modifyContent", MissionVisibility.FOLLOWER, null);

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
                        MissionVisibility.ALL,
                        MissionPeriod.TWO_WEEKS,
                        LocalTime.of(21, 0));
        MissionCreateResponse saveMission = missionService.createMission(missionCreateRequest);
        MissionUpdateRequest missionUpdateRequest =
                new MissionUpdateRequest(null, "modifyContent", MissionVisibility.FOLLOWER, null);

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
                        MissionVisibility.ALL,
                        MissionPeriod.TWO_WEEKS,
                        LocalTime.of(21, 0));
        MissionCreateResponse saveMission = missionService.createMission(missionCreateRequest);
        MissionUpdateRequest missionUpdateRequest =
                new MissionUpdateRequest(
                        "modifyMissionName_test",
                        "modifyContent",
                        MissionVisibility.FOLLOWER,
                        null);

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
                        MissionVisibility.ALL,
                        MissionPeriod.TWO_WEEKS,
                        LocalTime.of(21, 0));
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
                        MissionVisibility.ALL,
                        MissionPeriod.TWO_WEEKS,
                        LocalTime.of(21, 0));
        missionService.createMission(missionCreateRequest);

        // when
        missionService.deleteMission(200L);

        // then
        assertThat(missionRepository.findAll()).isNotEmpty();
    }
}
