package com.depromeet.domain.ranking.application;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import com.depromeet.DatabaseCleaner;
import com.depromeet.domain.comment.domain.Comment;
import com.depromeet.domain.follow.domain.MemberRelation;
import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.OauthInfo;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.domain.ranking.dao.RankingRepository;
import com.depromeet.domain.ranking.dto.response.RankingResponse;
import com.depromeet.domain.reaction.domain.EmojiType;
import com.depromeet.domain.reaction.domain.Reaction;
import com.depromeet.global.security.PrincipalDetails;

@SpringBootTest
@ActiveProfiles("test")
class RankingServiceTest {

	@Autowired private DatabaseCleaner databaseCleaner;
	@Autowired private RankingService rankingService;
	@Autowired private MemberRepository memberRepository;
	@Autowired private MissionRepository missionRepository;
	@Autowired private RankingRepository rankingRepository;
	@Autowired private MissionRecordRepository missionRecordRepository;

	@BeforeEach
	void setUp() {
		databaseCleaner.execute();
	}

	private void logoutAndReloginAs(Long memberId) {
		SecurityContextHolder.clearContext(); // 현재 회원 로그아웃
		PrincipalDetails principalDetails = new PrincipalDetails(memberId, "USER");
		Authentication authentication =
			new UsernamePasswordAuthenticationToken(
				principalDetails, null, principalDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private void setFixture() {
		Member member1 =
			Member.createNormalMember(
				OauthInfo.createOauthInfo("test1", "test1", "test1"), "test1");
		Member member2 =
			Member.createNormalMember(
				OauthInfo.createOauthInfo("test2", "test2", "test2"), "test2");
		Member member3 =
			Member.createNormalMember(
				OauthInfo.createOauthInfo("test3", "test3", "test3"), "test3");

		memberRepository.saveAll(List.of(member1, member2, member3));

		Mission mission1 =
			Mission.createMission(
				"mission1",
				"content1",
				1,
				MissionCategory.PROJECT,
				MissionVisibility.ALL,
				LocalDateTime.of(2024, 3, 1, 0, 0),
				LocalDateTime.of(2024, 3, 14, 0, 0),
				LocalTime.of(12, 0, 0),
				member1);

		Mission mission2 =
			Mission.createMission(
				"mission2",
				"content2",
				2,
				MissionCategory.EXERCISE,
				MissionVisibility.ALL,
				LocalDateTime.of(2024, 3, 2, 0, 0),
				LocalDateTime.of(2024, 3, 15, 0, 0),
				LocalTime.of(12, 0, 0),
				member2);

		Mission mission3 =
			Mission.createMission(
				"mission3",
				"content3",
				3,
				MissionCategory.STUDY,
				MissionVisibility.FOLLOWER,
				LocalDateTime.of(2024, 3, 3, 0, 0),
				LocalDateTime.of(2024, 3, 16, 0, 0),
				LocalTime.of(12, 0, 0),
				member3);

		Mission mission4 =
			Mission.createMission(
				"mission4",
				"content4",
				4,
				MissionCategory.ETC,
				MissionVisibility.NONE,
				LocalDateTime.of(2024, 3, 4, 0, 0),
				LocalDateTime.of(2024, 3, 17, 0, 0),
				LocalTime.of(12, 0, 0),
				member3);

		missionRepository.saveAll(List.of(mission1, mission2, mission3, mission4));

		MissionRecord missionRecord1 =
			MissionRecord.createMissionRecord(
				Duration.ofMinutes(15),
				LocalDateTime.of(2024, 3, 1, 12, 0),
				LocalDateTime.of(2024, 3, 1, 12, 15),
				mission1);

		MissionRecord missionRecord2 =
			MissionRecord.createMissionRecord(
				Duration.ofMinutes(25),
				LocalDateTime.of(2024, 3, 2, 12, 0),
				LocalDateTime.of(2024, 3, 2, 12, 25),
				mission2);

		MissionRecord missionRecord3 =
			MissionRecord.createMissionRecord(
				Duration.ofMinutes(35),
				LocalDateTime.of(2024, 3, 3, 12, 0),
				LocalDateTime.of(2024, 3, 3, 12, 35),
				mission3);

		MissionRecord missionRecord4 =
			MissionRecord.createMissionRecord(
				Duration.ofMinutes(45),
				LocalDateTime.of(2024, 3, 4, 12, 0),
				LocalDateTime.of(2024, 3, 4, 12, 45),
				mission4);

		missionRecordRepository.saveAll(
			List.of(missionRecord1, missionRecord2, missionRecord3, missionRecord4));
	}

	@Test
	void 랭킹_전체_조회한다() {
		// given
		setFixture();

		// when
		List<RankingResponse> rankings = rankingService.findAllRanking();

		// then
		assertEquals(4, rankings.size());
		assertEquals(1, rankings.get(0).rank());
		assertEquals(2, rankings.get(1).rank());
		assertEquals(3, rankings.get(2).rank());
		assertEquals(4, rankings.get(3).rank());
	}

}