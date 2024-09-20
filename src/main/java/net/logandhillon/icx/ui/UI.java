package net.logandhillon.icx.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.logandhillon.icx.ui.view.LoginView;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class UI extends Application {
    private static final Logger LOG = LoggerContext.getContext().getLogger(UI.class);
    public static Stage stage;

    @Override
    public void start(Stage stage) {
        UI.stage = stage;
        LOG.info("Initializing UI views");
        Scene scene = new Scene(new LoginView());
        stage.setTitle("ICX");
        stage.setScene(scene);
        stage.show();
    }

    public static void startClient() {
        LOG.info("Starting ICX client");
        launch();
    }

    public static void reloadScene(Scene newScene, Runnable silentAction) {
        stage.close();
        silentAction.run();
        stage.setScene(newScene);
        stage.show();
    }

    public static void reloadScene(Scene newScene) {
        stage.close();
        stage.setScene(newScene);
        stage.show();
    }
}
