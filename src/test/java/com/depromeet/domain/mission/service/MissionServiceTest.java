package com.depromeet.domain.mission.service;

import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.member.domain.Profile;
import com.depromeet.domain.mission.dao.MissionRepository;
import com.depromeet.domain.mission.domain.Mission;
import com.depromeet.domain.mission.domain.MissionCategory;
import com.depromeet.domain.mission.domain.MissionVisibility;
import com.depromeet.domain.mission.dto.request.CreateMissionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MissionServiceTest {

    @Autowired private MissionService missionService;
    @Autowired private MissionRepository missionRepository;
    @Autowired private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        missionRepository.deleteAll();
    }

    @Test
    void 공부_미션_생성() {
        // given
        Profile profile = new Profile("testNickname", "testProfileImageUrl");
        Member member = Member.createNormalMember(profile);
        Member saveMember = memberRepository.save(member);
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
}
