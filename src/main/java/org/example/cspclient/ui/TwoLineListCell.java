package org.example.cspclient.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Function;

/**
 * Generic two-line ListCell: bold title + muted subtitle (e.g., last-message preview).
 * Usage example:
 *   conversations.getStyleClass().add("conversation-list");
 *   conversations.setCellFactory(lv -> new TwoLineListCell<>(
 *       item -> item.getDisplayName(),
 *       item -> lastMessageService.previewFor(item)
 *   ));
 */
public class TwoLineListCell<T> extends ListCell<T> {

    private final Function<T, String> titleFn;
    private final Function<T, String> subtitleFn;
    private final Label title = new Label();
    private final Label subtitle = new Label();
    private final VBox textBox = new VBox(2, title, subtitle);
    private final HBox root = new HBox(10, textBox);

    public TwoLineListCell(Function<T, String> titleFn, Function<T, String> subtitleFn) {
        this.titleFn = titleFn;
        this.subtitleFn = subtitleFn;
        title.getStyleClass().add("cell-title");
        subtitle.getStyleClass().add("cell-subtitle");
        root.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textBox, Priority.ALWAYS);
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            title.setText(safe(titleFn.apply(item)));
            subtitle.setText(safe(subtitleFn.apply(item)));
            setText(null);
            setGraphic(root);
        }
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }
}
