package net.logandhillon.icx.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public record ServerProperties(String keystoreFile, String keystorePassword) {
    public static final String FILENAME = "server.properties";

    public static ServerProperties fromDisk() throws IOException, IllegalArgumentException {
        Properties properties = new Properties();

        try (InputStream input = new FileInputStream(FILENAME)) {
            properties.load(input);
        }

        String keystoreFile = properties.getProperty("keystore.file");
        String keystorePassword = properties.getProperty("keystore.password");

        if (keystoreFile == null || keystorePassword == null || keystoreFile.isBlank() || keystorePassword.isBlank())
            throw new IllegalArgumentException("missing or blank property in server.properties");

        if (!new File(keystoreFile).isFile())
            throw new IllegalArgumentException("keystore.file '" + keystoreFile + "' is not a valid file");

        return new ServerProperties(keystoreFile, keystorePassword);
    }
}
