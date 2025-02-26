package com.depromeet.global.util;

import com.depromeet.domain.member.dao.MemberRepository;
import com.depromeet.domain.member.domain.Member;
import com.depromeet.global.error.exception.CustomException;
import com.depromeet.global.error.exception.ErrorCode;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberUtil {

    private final SecurityUtil securityUtil;
    private final MemberRepository memberRepository;
    private static final ExecutorService VIRTUAL_THREAD_EXECUTOR =
            Executors.newVirtualThreadPerTaskExecutor();

    public Member getCurrentMember() {
        return CompletableFuture.supplyAsync(
                        () ->
                                memberRepository
                                        .findById(securityUtil.getCurrentMemberId())
                                        .orElseThrow(
                                                () ->
                                                        new CustomException(
                                                                ErrorCode.MEMBER_NOT_FOUND)))
                .join();
    }

    public Member getMemberByMemberId(Long memberId) {
        return CompletableFuture.supplyAsync(
                        () ->
                                memberRepository
                                        .findById(memberId)
                                        .orElseThrow(
                                                () ->
                                                        new CustomException(
                                                                ErrorCode.MEMBER_NOT_FOUND)))
                .join();
    }
}
