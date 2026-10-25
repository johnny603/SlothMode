package io.slothmode.ui;

import io.slothmode.calendar.BreakRepository;
import io.slothmode.calendar.BreakSourceFactory;
import io.slothmode.config.SlothConfig;
import io.slothmode.lockout.FullscreenLockout;
import io.slothmode.lockout.GentleNudgeLockout;
import io.slothmode.lockout.LockoutAbstraction;
import io.slothmode.session.SessionConfig;
import io.slothmode.session.SessionContext;
import io.slothmode.session.SessionHub;
import io.slothmode.session.SessionSubject;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public final class MainWindow {

    private final Stage stage;
    private final SlothConfig config;
    private final SessionHub hub;
    private final SessionContext sessionContext;
    private final BreakRepository breakRepository;

    public MainWindow(Stage stage, SlothConfig config) {
        this.stage = stage;
        this.config = config;
        this.breakRepository = new BreakRepository(BreakSourceFactory.fromConfig(config));
        this.sessionContext = buildSessionContext();
        this.hub = new SessionHub(sessionContext);
    }

    private SessionContext buildSessionContext() {
        LockoutAbstraction lockout = new LockoutAbstraction(
                "gentle".equalsIgnoreCase(config.lockoutMode())
                        ? new GentleNudgeLockout()
                        : new FullscreenLockout()
        );
        SessionConfig sessionConfig = SessionConfig.defaults();
        SessionSubject subject = new SessionSubject();
        return new SessionContext(sessionConfig, lockout, subject, breakRepository);
    }

    public void show() {
        Label sloth = new Label("🦥");
        sloth.setStyle("-fx-font-size: 96px;");

        Label status = new Label("SlothMode is idle");
        status.setStyle("-fx-font-size: 18px;");

        Label detail = new Label(
                "Calendar: " + config.calendarPath() + "\n" +
                "Lockout:  " + config.lockoutMode() + "\n" +
                "Model:    " + config.modelName() + "\n" +
                "Breaks:   " + breakRepository.breaks().size() + " from " + breakRepository.source().name());
        detail.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        Button start = new Button("Start focus session");
        Button cancel = new Button("Cancel");
        cancel.setDisable(true);

        start.setOnAction(e -> {
            hub.start();
            start.setDisable(true);
            cancel.setDisable(false);
        });
        cancel.setOnAction(e -> {
            hub.cancel();
            start.setDisable(true);
            cancel.setDisable(true);
        });

        HBox buttons = new HBox(12, start, cancel);
        buttons.setStyle("-fx-alignment: center;");

        VBox root = new VBox(16, sloth, status, detail, buttons);
        root.setStyle("-fx-alignment: center; -fx-padding: 32; -fx-background-color: #f5f1e8;");

        Scene scene = new Scene(root, 520, 420);
        var css = getClass().getResource("/ui/main.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle("SlothMode");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> hub.close());
        stage.show();
    }
}
