package com.depromeet.domain.relation.dao;

import com.depromeet.domain.relation.domain.MemberRelation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRelationRepository extends JpaRepository<MemberRelation, Long> {}
