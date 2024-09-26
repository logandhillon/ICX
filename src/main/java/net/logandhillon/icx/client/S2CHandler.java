package net.logandhillon.icx.client;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import net.logandhillon.icx.common.ICXMultimediaPayload;
import net.logandhillon.icx.common.ICXPacket;
import net.logandhillon.icx.ui.UI;
import net.logandhillon.icx.ui.view.ChatView;
import net.logandhillon.icx.ui.view.LoginView;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import javax.net.ssl.SSLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.Optional;

public class S2CHandler extends Thread {
    private static final Logger LOG = LoggerContext.getContext().getLogger(S2CHandler.class);
    private final BufferedReader reader;

    public S2CHandler(BufferedReader reader) {
        this.reader = reader;
        this.setName("S2C-Handler");
    }

    @Override
    public void run() {
        LOG.info("Started");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutdown hook triggered. Stopping S2C(IN) handler...");
            try {
                ICXClient.disconnect();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));

        while (ICXClient.isConnected()) {
            if (!ICXClient.isConnected()) break;
            try {
                ChatView.updateRoomName();

                String msg;
                if ((msg = reader.readLine()) != null) {
                    ICXPacket packet = ICXPacket.decode(msg);
                    LOG.debug("Incoming {} packet from {}", packet.command(), packet.snvs().name());

                    switch (packet.command()) {
                        case SRV_HELLO -> {
                            ICXClient.connectedRoomName = packet.content();
                            ChatView.updateRoomName();
                            LOG.info("Server room name is {}", packet.content());
                        }
                        case SRV_KICK -> {
                            Platform.runLater(() -> UI.reloadScene(new Scene(new LoginView()), () -> {
                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                alert.setTitle("Kicked from server");
                                alert.setHeaderText("You have been kicked from " + ICXClient.getServerAddr());
                                alert.setContentText("Reason: " + packet.content());
                                alert.showAndWait();
                            }));
                            ICXClient.disconnect();
                            throw new SocketException("Socket closed");
                        }
                        case SEND ->
                                Platform.runLater(() -> ChatView.postMessage(packet.snvs().name(), packet.content()));
                        case FILE_INF ->
                                Platform.runLater(() -> ChatView.postMMP(packet.snvs().name(), ICXMultimediaPayload.decode(packet.content())));
                        case JOIN ->
                                Platform.runLater(() -> ChatView.postAlert(String.format("Welcome, %s!", packet.snvs().name())));
                        case EXIT ->
                                Platform.runLater(() -> ChatView.postAlert(String.format("Farewell, %s!", packet.snvs().name())));
                    }
                }
            } catch (SSLException e) {
                showError("An error disrupted your connection: " + e.getMessage(), "Server SSL Error");
                return;
            } catch (SocketException e) {
                if (e.getMessage().equals("Socket closed")) return;
                showError("An error disrupted your connection: " + e.getMessage(), "Connection Error");
                return;
            } catch (Exception e) {
                showError("Failed to parse incoming packet: " + e.getMessage(), e.getClass().getName());
                return;
            }
        }
    }

    private static void showError(String error, String cause) {
        LOG.warn(error);
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING, error);
            alert.setHeaderText(cause);

            ButtonType ignoreButton = new ButtonType("Ignore");
            ButtonType exitButton = new ButtonType("Exit");
            alert.getButtonTypes().setAll(ignoreButton, exitButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == exitButton) {
                UI.reloadScene(new Scene(new LoginView()), ChatView::exitRoom);
            }
        });
    }
}
