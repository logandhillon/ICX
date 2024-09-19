package net.logandhillon.icx.views;

import javafx.scene.layout.AnchorPane;

public class MainView implements UIView<AnchorPane> {
    private final AnchorPane parent;

    public MainView() {
        parent = new AnchorPane();
    }

    @Override
    public AnchorPane getView() {
        return parent;
    }
}