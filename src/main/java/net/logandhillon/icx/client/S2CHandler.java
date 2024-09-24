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
                String msg;
                if ((msg = reader.readLine()) != null) {
                    ICXPacket packet = ICXPacket.decode(msg);
                    LOG.debug("Incoming {} packet from {}", packet.command(), packet.sender());

                    switch (packet.command()) {
                        case SRV_KICK -> Platform.runLater(() -> UI.reloadScene(new Scene(new LoginView()), () -> {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Kicked from server");
                            alert.setHeaderText("You have been kicked from " + ICXClient.getServerAddr());
                            alert.setContentText("Reason: " + packet.content());
                            alert.showAndWait();
                        }));
                        case SEND -> Platform.runLater(() -> ChatView.postMessage(packet.sender(), packet.content()));
                        case UPLOAD ->
                                Platform.runLater(() -> ChatView.postMMP(packet.sender(), ICXMultimediaPayload.decode(packet.content())));
                        case JOIN ->
                                Platform.runLater(() -> ChatView.postAlert(String.format("Welcome, %s!", packet.sender())));
                        case EXIT ->
                                Platform.runLater(() -> ChatView.postAlert(String.format("Farewell, %s!", packet.sender())));
                    }
                }
            } catch (SSLException e) {
                LOG.warn("Secure connection failed (likely server fault): {}", e.getMessage());
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING, e.getMessage() + "\nThis is likely the server's fault.");
                    alert.setHeaderText("Secure connection failed");

                    ButtonType ignoreButton = new ButtonType("Ignore");
                    ButtonType exitButton = new ButtonType("Exit");
                    alert.getButtonTypes().setAll(ignoreButton, exitButton);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == exitButton) {
                        UI.reloadScene(new Scene(new LoginView()), ChatView::exitRoom);
                    }
                });
                return;
            } catch (Exception e) {
                LOG.warn("Failed to parse incoming packet: {}", e.getMessage());
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Failed to parse incoming packet: " + e.getMessage());

                    ButtonType ignoreButton = new ButtonType("Ignore");
                    ButtonType exitButton = new ButtonType("Exit");
                    alert.getButtonTypes().setAll(ignoreButton, exitButton);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == exitButton) {
                        UI.reloadScene(new Scene(new LoginView()), ChatView::exitRoom);
                    }
                });
                return;
            }
        }
    }
}
