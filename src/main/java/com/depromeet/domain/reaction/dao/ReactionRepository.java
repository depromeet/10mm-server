package com.depromeet.domain.reaction.dao;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.domain.reaction.domain.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReactionRepository
        extends JpaRepository<Reaction, Long>, ReactionRepositoryCustom {
    boolean existsByMemberAndMissionRecord(Member member, MissionRecord missionRecord);
}
