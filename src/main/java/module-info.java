module net.logandhillon.icx {
    requires javafx.controls;
    requires org.apache.logging.log4j.core;
    requires java.desktop;

    exports net.logandhillon.icx;
    exports net.logandhillon.icx.server;
    exports net.logandhillon.icx.common;
    exports net.logandhillon.icx.ui to javafx.graphics;
}