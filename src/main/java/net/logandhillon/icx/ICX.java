package net.logandhillon.icx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.logandhillon.icx.server.ICXServer;
import net.logandhillon.icx.views.MainView;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class ICX extends Application {
    private static final Logger LOG = LoggerContext.getContext().getLogger(ICX.class);

    @Override
    public void start(Stage stage) {
        LOG.info("Initializing UI views");
        Scene scene = new Scene(new MainView().getView(), 320, 240);
        stage.setTitle("ICX");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h", "--help" -> printHelp();
                case "-s", "--server" -> serverMain();
                default -> clientMain();
            }
        }
        System.exit(0);
    }

    private static void clientMain() {
        launch();
    }

    private static void serverMain() {
        new ICXServer().start();
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