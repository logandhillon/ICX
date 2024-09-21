package net.logandhillon.icx.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerProperties {
    public final String keystoreFile;
    public final String keystorePassword;

    public ServerProperties() throws IOException, IllegalArgumentException {
        Properties properties = new Properties();

        try (InputStream input = new FileInputStream("server.properties")) {
            properties.load(input);
        }

        keystoreFile = properties.getProperty("keystore.file");
        keystorePassword = properties.getProperty("keystore.password");

        if (keystoreFile == null || keystorePassword == null || keystoreFile.isBlank() || keystorePassword.isBlank())
            throw new IllegalArgumentException("missing or blank property in server.properties");

        if (!new File(keystoreFile).isFile())
            throw new IllegalArgumentException("keystore.file '" + keystoreFile + "' is not a valid file");
    }
}
