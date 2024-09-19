package net.logandhillon.icx.client;

import javafx.css.converter.EffectConverter;
import net.logandhillon.icx.common.ICXPacket;
import net.logandhillon.icx.server.C2SHandler;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ICXClient {
    private static final Logger LOG = LoggerContext.getContext().getLogger(ICXClient.class);
    private static String screenName;
    private static InetSocketAddress serverAddr;
    private static Socket socket;
    private static PrintWriter writer;
    private static BufferedReader reader;

    public static void connect(String _screenName, InetAddress _serverAddr) throws IOException {
        screenName = _screenName;
        serverAddr = new InetSocketAddress(_serverAddr, 195);

        LOG.info("Connecting to {} as {}", serverAddr, screenName);
        socket = new Socket();
        socket.connect(serverAddr, 5000);
        writer = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        S2CHandler handler = new S2CHandler(reader);
        handler.start();

        send(ICXPacket.Command.JOIN, null);
    }

    public static void disconnect() throws IOException {
        writer.close();
        reader.close();
        socket.close();
        LOG.info("Disconnected from server");
    }

    public static void send(ICXPacket.Command command, String content) {
        writer.println(new ICXPacket(command, screenName, content).encode());
    }

    public static String getScreenName() {
        return screenName;
    }

    public static InetSocketAddress getServerAddr() {
        return serverAddr;
    }
}
