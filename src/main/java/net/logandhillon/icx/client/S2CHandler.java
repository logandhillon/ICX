package net.logandhillon.icx.client;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import net.logandhillon.icx.ICX;
import net.logandhillon.icx.common.ICXPacket;
import net.logandhillon.icx.ui.view.LoginView;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.BufferedReader;
import java.io.IOException;

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
                    LOG.debug("Incoming: {}", packet);

                    switch (packet.command()) {
                        case SRV_KICK -> Platform.runLater(() -> {
                            ICX.stage.close();

                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Kicked from server");
                            alert.setHeaderText("You have been kicked from " + ICXClient.getServerAddr());
                            alert.setContentText("Reason: " + packet.content());
                            alert.showAndWait();

                            ICX.stage.setScene(new Scene(new LoginView()));
                            ICX.stage.show();
                        });
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
