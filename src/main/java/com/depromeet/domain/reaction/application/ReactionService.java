package com.depromeet.domain.reaction.application;

import static java.util.Comparator.*;

import com.depromeet.domain.member.domain.Member;
import com.depromeet.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.domain.missionRecord.domain.MissionRecord;
import com.depromeet.domain.reaction.dao.ReactionRepository;
import com.depromeet.domain.reaction.domain.EmojiType;
import com.depromeet.domain.reaction.domain.Reaction;
import com.depromeet.domain.reaction.dto.request.ReactionCreateRequest;
import com.depromeet.domain.reaction.dto.request.ReactionUpdateRequest;
import com.depromeet.domain.reaction.dto.response.ReactionCreateResponse;
import com.depromeet.domain.reaction.dto.response.ReactionGroupByEmojiResponse;
import com.depromeet.domain.reaction.dto.response.ReactionUpdateResponse;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import com.depromeet.global.util.MemberUtil;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReactionService {

    private final MemberUtil memberUtil;
    private final MissionRecordRepository missionRecordRepository;
    private final ReactionRepository reactionRepository;

    public List<ReactionGroupByEmojiResponse> findAllReaction(Long missionRecordId) {
        Map<EmojiType, List<Reaction>> reactionMap =
                reactionRepository.findAllGroupByEmoji(missionRecordId);
        return reactionMap.entrySet().stream()
                .map(ReactionGroupByEmojiResponse::of)
                .sorted(comparing(ReactionGroupByEmojiResponse::count).reversed())
                .toList();
    }

    public ReactionCreateResponse createReaction(ReactionCreateRequest request) {
        final Member member = memberUtil.getCurrentMember();
        MissionRecord missionRecord =
                missionRecordRepository
                        .findById(request.missionRecordId())
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));

        validateNotMyMissionRecord(member, missionRecord);
        validateMyReactionAlreadyExists(member, missionRecord);

        Reaction reaction = Reaction.createReaction(request.emojiType(), member, missionRecord);
        return ReactionCreateResponse.from(reactionRepository.save(reaction));
    }

    private void validateNotMyMissionRecord(Member member, MissionRecord missionRecord) {
        if (missionRecord.getMission().getMember().equals(member)) {
            throw new CustomException(ErrorCode.REACTION_SELF_NOT_ALLOWED);
        }
    }

    private void validateMyReactionAlreadyExists(Member member, MissionRecord missionRecord) {
        missionRecord.getReactions().stream()
                .filter(reaction -> reaction.getMember().equals(member))
                .findAny()
                .ifPresent(
                        reaction -> {
                            throw new CustomException(ErrorCode.REACTION_ALREADY_EXISTS);
                        });
    }

    public ReactionUpdateResponse updateReaction(Long reactionId, ReactionUpdateRequest request) {
        final Member member = memberUtil.getCurrentMember();
        Reaction reaction =
                reactionRepository
                        .findById(reactionId)
                        .orElseThrow(() -> new CustomException(ErrorCode.REACTION_NOT_FOUND));

        validateReactionMemberMismatch(member, reaction);

        reaction.updateEmojiType(request.emojiType());
        return ReactionUpdateResponse.from(reaction);
    }

    public void deleteReaction(Long reactionId) {
        final Member member = memberUtil.getCurrentMember();
        Reaction reaction =
                reactionRepository
                        .findById(reactionId)
                        .orElseThrow(() -> new CustomException(ErrorCode.REACTION_NOT_FOUND));

        validateReactionMemberMismatch(member, reaction);

        reactionRepository.delete(reaction);
    }

    private void validateReactionMemberMismatch(Member member, Reaction reaction) {
        if (!reaction.getMember().equals(member)) {
            throw new CustomException(ErrorCode.REACTION_MEMBER_MISMATCH);
        }
    }
}
