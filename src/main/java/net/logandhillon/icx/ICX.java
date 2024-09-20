package net.logandhillon.icx;

import net.logandhillon.icx.server.ICXServer;
import net.logandhillon.icx.ui.UI;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class ICX {
    private static final Logger LOG = LoggerContext.getContext().getLogger(ICX.class);

    public static void main(String[] args) {
        boolean isServer = false;
        for (String arg : args)
            switch (arg) {
                case "-h", "--help" -> printHelp();
                case "-s", "--server" -> isServer = true;
            }

        if (isServer) serverMain();
        else UI.startClient();
        System.exit(0);
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