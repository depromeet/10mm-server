package com.depromeet.domain.mission.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.TestQuerydslConfig;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.OauthInfo;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionPeriod;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.global.security.PrincipalDetails;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(TestQuerydslConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles(value = "test")
class MissionRepositoryTest {

    @Autowired private MissionRepository missionRepository;
    @Autowired private MemberRepository memberRepository;

    private Member saveAndRegisterMember() {
        SecurityContextHolder.clearContext();
        OauthInfo oauthInfo =
                OauthInfo.createOauthInfo("testOauthId", "testOauthProvider", "testOauthEmail");
        Member member = Member.createNormalMember(oauthInfo, "testNickname");
        memberRepository.save(member);
        PrincipalDetails principalDetails = new PrincipalDetails(member.getId(), "USER");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principalDetails, null, principalDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return member;
    }

    @Test
    void 미션을_생성한다() {
        // given
        LocalDateTime startedAt = LocalDateTime.now();
        Member member = saveAndRegisterMember();

        MissionCreateRequest missionCreateRequest =
                new MissionCreateRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL,
                        MissionPeriod.TWO_WEEKS,
                        LocalTime.of(21, 0));

        // when
        Mission saveMission =
                missionRepository.save(
                        Mission.createMission(
                                missionCreateRequest.name(),
                                missionCreateRequest.content(),
                                1,
                                missionCreateRequest.category(),
                                missionCreateRequest.visibility(),
                                startedAt,
                                startedAt.plusWeeks(2),
                                LocalTime.of(21, 0),
                                member));

        // then
        assertThat(saveMission.getId()).isNotNull();
        assertThat(saveMission.getName()).isEqualTo(missionCreateRequest.name());
        assertThat(saveMission.getContent()).isEqualTo(missionCreateRequest.content());
        assertThat(saveMission.getCategory()).isEqualTo(missionCreateRequest.category());
        assertThat(saveMission.getVisibility()).isEqualTo(missionCreateRequest.visibility());
        assertThat(saveMission.getStartedAt()).isEqualTo(startedAt);
    }

    @Test
    void 미션이름_20자_초과하면_미션생셩_실패한다() {
        // given
        LocalDateTime startedAt = LocalDateTime.now();
        Member member = saveAndRegisterMember();

        MissionCreateRequest missionCreateRequest =
                new MissionCreateRequest(
                        "testMissionNameMoreThan",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL,
                        MissionPeriod.TWO_WEEKS,
                        LocalTime.of(21, 0));

        // when
        Mission mission =
                Mission.createMission(
                        missionCreateRequest.name(),
                        missionCreateRequest.content(),
                        1,
                        missionCreateRequest.category(),
                        missionCreateRequest.visibility(),
                        startedAt,
                        startedAt.plusWeeks(2),
                        LocalTime.of(21, 0),
                        member);

        // then
        assertThatThrownBy(() -> missionRepository.save(mission))
                // instance 검증
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 단건_미션을_조회한다() {
        // given
        MissionCreateRequest missionCreateRequest =
                new MissionCreateRequest(
                        "testMissionName",
                        "testMissionContent",
                        MissionCategory.STUDY,
                        MissionVisibility.ALL,
                        MissionPeriod.TWO_WEEKS,
                        LocalTime.of(21, 0));
        LocalDateTime startedAt = LocalDateTime.now();
        Member member = saveAndRegisterMember();

        Mission saveMission =
                missionRepository.save(
                        Mission.createMission(
                                missionCreateRequest.name(),
                                missionCreateRequest.content(),
                                1,
                                missionCreateRequest.category(),
                                missionCreateRequest.visibility(),
                                startedAt,
                                startedAt.plusWeeks(2),
                                LocalTime.of(21, 0),
                                member));
        // when
        Optional<Mission> findMission = missionRepository.findById(saveMission.getId());

        // then
        assertThat(findMission).isPresent();
        assertThat(findMission.get().getId()).isEqualTo(saveMission.getId());
        assertThat(findMission.get().getName()).isEqualTo(saveMission.getName());
        assertThat(findMission.get().getContent()).isEqualTo(saveMission.getContent());
        assertThat(findMission.get().getCategory()).isEqualTo(saveMission.getCategory());
        assertThat(findMission.get().getVisibility()).isEqualTo(saveMission.getVisibility());
        assertThat(findMission.get().getStartedAt()).isEqualTo(saveMission.getStartedAt());
        assertThat(findMission.get().getFinishedAt()).isEqualTo(saveMission.getFinishedAt());
    }

    @Test
    void 미션_리스트_조회한다() {
        // given
        LocalDateTime startedAt = LocalDateTime.now();
        Member member = saveAndRegisterMember();

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
                                                member)));

        // when
        List<Mission> missionList =
                missionRepository.findInProgressMissionsWithRecords(member.getId());

        // then
        assertThat(missionList.size()).isEqualTo(4);
        assertThat(missionList)
                .hasSize(4)
                .extracting("id", "name", "content")
                .containsExactlyInAnyOrder(
                        tuple(4L, "testMissionName_4", "testMissionContent_4"),
                        tuple(3L, "testMissionName_3", "testMissionContent_3"),
                        tuple(2L, "testMissionName_2", "testMissionContent_2"),
                        tuple(1L, "testMissionName_1", "testMissionContent_1"));
    }
}
