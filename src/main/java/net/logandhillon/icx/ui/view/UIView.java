package net.logandhillon.icx.ui.view;

import javafx.scene.Parent;

public interface UIView<T extends Parent> {
    T getView();
}
