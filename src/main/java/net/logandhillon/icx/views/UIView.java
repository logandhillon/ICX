package net.logandhillon.icx.views;

import javafx.scene.Parent;

public interface UIView<T extends Parent> {
    T getView();
}
