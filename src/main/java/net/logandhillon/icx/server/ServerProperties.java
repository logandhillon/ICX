package net.logandhillon.icx.server;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public record ServerProperties(String keystoreFile, String keystorePassword, String roomName, int chatLoggerSize) {
    public static final String FILENAME = "server.properties";
    private static final Logger LOG = LoggerContext.getContext().getLogger(ServerProperties.class);

    public static ServerProperties fromDisk() throws IOException, IllegalArgumentException {
        Properties properties = new Properties();

        try (InputStream input = new FileInputStream(FILENAME)) {
            properties.load(input);
        }

        String keystoreFile = properties.getProperty("keystore.file");
        String keystorePassword = properties.getProperty("keystore.password");
        String roomName = properties.getProperty("room.name", "Unnamed chatroom");
        int chatLoggerSize = 50;
        String _s = properties.getProperty("chat_logger.size");
        try {
            chatLoggerSize = Integer.parseInt(_s);
        } catch (NumberFormatException e) {
            LOG.warn("Invalid chat logger size '{}', defaulting to 50", _s);
        }

        if (keystoreFile == null || keystorePassword == null || keystoreFile.isBlank() || keystorePassword.isBlank())
            throw new IllegalArgumentException("missing or blank property in server.properties");

        if (!new File(keystoreFile).isFile())
            throw new IllegalArgumentException("keystore.file '" + keystoreFile + "' is not a valid file");

        return new ServerProperties(keystoreFile, keystorePassword, roomName, chatLoggerSize);
    }
}
