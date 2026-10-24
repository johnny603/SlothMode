package io.slothmode.ui;

import io.slothmode.config.SlothConfig;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public final class MainWindow {

    private final Stage stage;
    private final SlothConfig config;

    public MainWindow(Stage stage, SlothConfig config) {
        this.stage = stage;
        this.config = config;
    }

    public void show() {
        Label sloth = new Label("\uD83E\uDD9C");
        sloth.setStyle("-fx-font-size: 96px;");

        Label status = new Label("SlothMode is idle");
        status.setStyle("-fx-font-size: 18px;");

        Label detail = new Label(
                "Calendar: " + config.calendarPath() + "\n" +
                "Lockout:  " + config.lockoutMode() + "\n" +
                "Model:    " + config.modelName());
        detail.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        VBox root = new VBox(16, sloth, status, detail);
        root.setStyle("-fx-alignment: center; -fx-padding: 32; -fx-background-color: #f5f1e8;");

        Scene scene = new Scene(root, 520, 360);
        var css = getClass().getResource("/ui/main.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle("SlothMode");
        stage.setScene(scene);
        stage.show();
    }
}
