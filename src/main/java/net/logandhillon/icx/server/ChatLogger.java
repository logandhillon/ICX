package net.logandhillon.icx.server;

import net.logandhillon.icx.ICX;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;

public class ChatLogger {
    private static final Logger LOG = LoggerContext.getContext().getLogger(ChatLogger.class);
    private static final int MAX_LOG_SIZE = ICX.isServer() ? ICXServer.PROPERTIES.chatLoggerSize() : -1;
    private static final Deque<Message> CHAT_LOG = new LinkedList<>();

    public static void log(String sender, String message) {
        Message msg = new Message(sender, message);
        LOG.info(msg);

        synchronized (CHAT_LOG) {
            if (CHAT_LOG.size() >= MAX_LOG_SIZE) CHAT_LOG.removeFirst();
            CHAT_LOG.addLast(msg);
        }
    }

    public static void logAlert(String alert) {
        log(NameRegistry.SERVER.name(), alert);
    }

    public static String encodeLogs() {
        StringBuilder builder = new StringBuilder();
        for (Message msg : CHAT_LOG) builder.append(msg.encode()).append("\036");
        return builder.toString();
    }

    public static Message[] parseLogs(String payload) {
        return Arrays.stream(payload.split("\036"))
                .map(s -> {
                    try {
                        LOG.debug(s);
                        return Message.decode(s);
                    } catch (Exception e) {
                        LOG.warn("Chat-log parsing error: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(Message[]::new);
    }

    public record Message(String sender, String message) {
        @Override
        public String toString() {
            return sender + ": " + message;
        }

        public String encode() {
            return sender + "\037" + message;
        }

        public static Message decode(String s) {
            String[] parts = s.split("\037");
            if (parts.length < 2) return null;
            return new Message(parts[0], parts[1]);
        }
    }
}
