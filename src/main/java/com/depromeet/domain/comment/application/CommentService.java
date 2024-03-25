package com.depromeet.domain.comment.application;

import com.depromeet.domain.comment.dao.CommentRepository;
import com.depromeet.domain.comment.domain.Comment;
import com.depromeet.domain.comment.dto.request.CommentCreateRequest;
import com.depromeet.domain.comment.dto.response.CommentDto;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final MemberUtil memberUtil;
    private final MissionRecordRepository missionRecordRepository;
    private final CommentRepository commentRepository;

    public CommentDto createComment(CommentCreateRequest request) {
        final Member member = memberUtil.getCurrentMember();
        MissionRecord missionRecord =
                missionRecordRepository
                        .findById(request.missionRecordId())
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));

        Comment comment = Comment.createComment(request.content(), member, missionRecord);
        commentRepository.save(comment);

        return CommentDto.from(comment);
    }
}
