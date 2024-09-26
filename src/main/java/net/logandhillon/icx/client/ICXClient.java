package net.logandhillon.icx.client;

import net.logandhillon.icx.common.ICXMultimediaPayload;
import net.logandhillon.icx.common.ICXPacket;
import net.logandhillon.icx.common.SNVS;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class ICXClient {
    private static final Logger LOG = LoggerContext.getContext().getLogger(ICXClient.class);
    private static SNVS.Token snvs;
    private static InetSocketAddress serverAddr;
    private static SSLSocket socket;
    private static PrintWriter writer;
    private static BufferedReader reader;

    public static String connectedRoomName = null;

    // trust all certificates
    private static final TrustManager[] TRUST_MANAGER = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
    };

    public static void connect(String screenName, InetAddress serverAddr) throws IOException {
        snvs = new SNVS.Token(screenName, SNVS.genToken());
        ICXClient.serverAddr = new InetSocketAddress(serverAddr, 195);

        LOG.info("Connecting to {} as {}", ICXClient.serverAddr, snvs);

        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, TRUST_MANAGER, new SecureRandom());
            socket = (SSLSocket) context.getSocketFactory().createSocket();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        socket.connect(ICXClient.serverAddr, 5000);

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
        LOG.debug("Sending {} packet", command);
        writer.println(new ICXPacket(command, snvs, content).encode());
    }

    public static void uploadFile(File file) {
        LOG.debug("Uploading {}", file.getName());
        try {
            send(ICXPacket.Command.UPLOAD, ICXMultimediaPayload.fromFile(file).encode());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SNVS.Token getSnvs() {
        return snvs;
    }

    public static InetSocketAddress getServerAddr() {
        return serverAddr;
    }

    public static boolean isConnected() {
        return socket.isConnected();
    }
}
