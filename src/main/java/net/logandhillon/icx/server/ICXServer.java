package net.logandhillon.icx.server;

import net.logandhillon.icx.ICX;
import net.logandhillon.icx.common.ICXPacket;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.util.ArrayList;

public class ICXServer {
    private static final Logger LOG = LoggerContext.getContext().getLogger(ICXServer.class);
    private static volatile boolean running = true;
    public static final NameRegistry NAME_REGISTRY = new NameRegistry();
    public static final ArrayList<PrintWriter> CLIENT_WRITERS = new ArrayList<>();
    public static final ServerProperties PROPERTIES;

    static {
        ServerProperties properties = null;
        if (ICX.isServer()) try {
            properties = ServerProperties.fromDisk();
        } catch (Exception e) {
            LOG.fatal("ICX server could not be started: {}", e.getMessage());
            LOG.info("Starting server configurator tool");
            ServerConfigurator.launch();
            System.exit(0);
        }
        PROPERTIES = properties;
    }

    public static void broadcast(ICXPacket packet) {
        for (PrintWriter writer : CLIENT_WRITERS)
            writer.println(packet.encode());
    }

    public static void start() {
        NAME_REGISTRY.registerName(NameRegistry.SERVER, InetAddress.getLoopbackAddress());
        int port = 195; // ooh, fun fact port 194 is IRC, so port 195 is a homage to that

        System.setProperty("javax.net.ssl.keyStore", ICXServer.PROPERTIES.keystoreFile());
        System.setProperty("javax.net.ssl.keyStorePassword", ICXServer.PROPERTIES.keystorePassword());

        SSLServerSocketFactory socketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        try (SSLServerSocket server = (SSLServerSocket) socketFactory.createServerSocket(port)) {
            LOG.info("ICX is listening on port {}", port);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOG.info("Shutdown hook triggered. Stopping server...");
                running = false;
                try {
                    server.close();
                } catch (IOException e) {
                    LOG.error("Error closing server socket", e);
                }
            }));

            while (running) {
                try {
                    SSLSocket client = (SSLSocket) server.accept();
                    if (!running) break;

                    C2SHandler handler = new C2SHandler(client);
                    handler.start();
                } catch (IOException e) {
                    if (running) LOG.error("Server exception: ", e);
                }
            }
            LOG.info("Server stopped");
        } catch (BindException e) {
            LOG.fatal("ICX port (195) is already in use or cannot be accessed.");
            System.exit(2);
        } catch (IOException e) {
            LOG.fatal("Unknown server exception: ", e);
        }
    }
}