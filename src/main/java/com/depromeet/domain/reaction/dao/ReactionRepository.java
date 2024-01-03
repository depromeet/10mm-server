package com.depromeet.domain.reaction.dao;

import com.depromeet.domain.reaction.domain.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {}
