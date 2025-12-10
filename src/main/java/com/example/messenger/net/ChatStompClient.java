package com.example.messenger.net;

import com.example.messenger.dto.MessageDto;
import com.example.messenger.dto.TaskDto;
import com.example.messenger.store.SessionStore;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.function.Consumer;

public class ChatStompClient {

    private StompSession session;
    private final String wsUrl = "ws://localhost:8080/ws";

    public void connect() {
        if (session != null && session.isConnected()) {
            return;
        }

        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        try {
            session = stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {
                @Override
                public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                    exception.printStackTrace();
                }

                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    exception.printStackTrace();
                }
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscribeToGroupTasks(Long groupId, Consumer<TaskDto> onTaskReceived) {
        if (session == null || !session.isConnected()) {
            connect();
        }

        if (session == null || !session.isConnected()) return;

        String topic = "/topic/groups/" + groupId + "/tasks";

        session.subscribe(topic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return TaskDto.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                if (payload instanceof TaskDto task) {
                    onTaskReceived.accept(task);
                }
            }
        });
    }

    public void subscribeToConversation(Long conversationId, Consumer<MessageDto> onMessageReceived) {
        if (session == null || !session.isConnected()) connect();
        if (session == null) return;

        String topic = "/topic/conversations/" + conversationId;
        session.subscribe(topic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) { return MessageDto.class; }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                if (payload instanceof MessageDto msg) onMessageReceived.accept(msg);
            }
        });
    }

    public void sendMessage(Long conversationId, String content) {
        if (session == null || !session.isConnected()) connect();
        if (session == null) return;
        SendMessageRequest request = new SendMessageRequest(conversationId, SessionStore.getUserId(), content);
        session.send("/app/chat.sendMessage", request);
    }

    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    private static class SendMessageRequest {
        public Long conversationId;
        public Long senderUserId;
        public String content;
        public SendMessageRequest(Long cid, Long uid, String txt) { this.conversationId=cid; this.senderUserId=uid; this.content=txt; }
    }
}