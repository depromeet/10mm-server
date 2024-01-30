package com.depromeet.global.config.fcm;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.*;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class FcmService {

    /**
     * https://firebase.google.com/support/release-notes/admin/java 위 레퍼런스에 의거하여 sendMulticastAsync
     * 는 Deprecated 되어 sendEachForMulticastAsync
     *
     * @param tokenList: 푸시 토큰 리스트
     * @param title: 알림 제목
     * @param content: 알림 내용
     * @param data: 푸시 데이터
     * @return ApiFuture<BatchResponse>
     */
    public ApiFuture<BatchResponse> sendGroupMessageAsync(
            List<String> tokenList, String title, String content, Map<String, String> data) {
        MulticastMessage multicast =
                MulticastMessage.builder()
                        .addAllTokens(tokenList)
                        .setNotification(
                                Notification.builder().setTitle(title).setBody(content).build())
                        .putAllData(data)
                        .build();
        return FirebaseMessaging.getInstance().sendEachForMulticastAsync(multicast);
    }

    public ApiFuture<String> sendMessageSync(
            String token, String title, String content, Map<String, String> data) {
        Message message =
                Message.builder()
                        .setToken(token)
                        .setNotification(
                                Notification.builder().setTitle(title).setBody(content).build())
                        .putAllData(data)
                        .build();
        return FirebaseMessaging.getInstance().sendAsync(message);
    }
}
