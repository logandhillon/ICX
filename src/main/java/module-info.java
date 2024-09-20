module net.logandhillon.icx {
    requires javafx.controls;
    requires org.apache.logging.log4j.core;
    requires java.desktop;

    exports net.logandhillon.icx;
    exports net.logandhillon.icx.ui to javafx.graphics;
}