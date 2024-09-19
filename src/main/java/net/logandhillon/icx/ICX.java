package net.logandhillon.icx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.logandhillon.icx.server.ICXServer;
import net.logandhillon.icx.views.LoginView;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class ICX extends Application {
    private static final Logger LOG = LoggerContext.getContext().getLogger(ICX.class);
    public static Stage stage;

    @Override
    public void start(Stage stage) {
        ICX.stage = stage;
        LOG.info("Initializing UI views");
        Scene scene = new Scene(new LoginView().getView());
        stage.setTitle("ICX");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        boolean isServer = false;
        for (String arg : args)
            switch (arg) {
                case "-h", "--help" -> printHelp();
                case "-s", "--server" -> isServer = true;
            }

        if (isServer) serverMain();
        else clientMain();
        System.exit(0);
    }

    private static void clientMain() {
        LOG.info("Starting ICX client");
        launch();
    }

    private static void serverMain() {
        LOG.info("Starting ICX server");
        ICXServer.start();
    }

    private static void printHelp() {
        System.out.println("""
                Internet Communication Exchange (ICX)
                Usage: icx [options]
                Options:
                    -h, --help          show this help message and exit
                    -s, --server        host an ICX server instead of launching a client""");
    }
}