package com.depromeet.domain.comment.dao;

import com.depromeet.domain.comment.domain.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByMissionRecordIdInOrderByCreatedAtAsc(List<Long> missionRecordIds);
}
