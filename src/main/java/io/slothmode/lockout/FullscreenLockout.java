package io.slothmode.lockout;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Fullscreen, undismissable, calm overlay.
 *
 * - No window decorations, no close button.
 * - Full-screen exit key combination set to NO_MATCH, hint blank.
 * - Always on top.
 * - Escape hatch: hold the ESCAPE key for 5 seconds to dismiss and log a
 *   skipped break. This is deliberate: forcing a break must not trap a
 *   user in an emergency.
 */
public final class FullscreenLockout implements LockoutImplementor {

    private static final Logger log = LoggerFactory.getLogger(FullscreenLockout.class);
    private static final int ESCAPE_HATCH_SECONDS = 5;

    private volatile Stage stage;
    private volatile CountdownRing ring;
    private volatile CountDownLatch completed;

    @Override
    public void present(int durationSeconds) {
        CountDownLatch latch = new CountDownLatch(1);
        this.completed = latch;

        Platform.runLater(() -> {
            Stage s = new Stage(StageStyle.UNDECORATED);
            s.setTitle("SlothMode — break");

            Label sloth = new Label("🦥");
            sloth.setStyle("-fx-font-size: 120px;");

            Label message = new Label("You're on a break.");
            message.setStyle("-fx-font-size: 24px; -fx-text-fill: #333;");

            Label hint = new Label("Hold ESC for 5 seconds if this is an emergency.");
            hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

            CountdownRing countdown = new CountdownRing(durationSeconds);
            this.ring = countdown;

            VBox root = new VBox(24, sloth, message, countdown, hint);
            root.setAlignment(Pos.CENTER);
            root.setStyle("-fx-background-color: #f5f1e8;");

            Scene scene = new Scene(root, 800, 600, Color.web("#f5f1e8"));
            var css = getClass().getResource("/ui/main.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            // Undismissable: no full-screen exit, no decorations, no close.
            s.initStyle(StageStyle.UNDECORATED);
            s.setFullScreenExitHint("");
            s.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
            s.setAlwaysOnTop(true);
            s.setResizable(false);
            s.setFullScreen(true);
            s.setOnCloseRequest(WindowEvent::consume);

            // Escape hatch: hold ESC for 5 seconds → dismiss as "skipped".
            installEscapeHatch(scene, s);

            s.setScene(scene);
            s.show();
            this.stage = s;

            countdown.start();
            countdown.sceneProperty().addListener((obs, o, n) -> {
                if (n == null) { /* ignore */ }
            });

            // Dismiss when the countdown finishes.
            Timeline watcher = new Timeline(new KeyFrame(Duration.millis(250), e -> {
                if (countdown.isFinished()) {
                    log.info("Break completed after {}s", durationSeconds);
                    dismiss();
                }
            }));
            watcher.setCycleCount(Timeline.INDEFINITE);
            watcher.play();
            s.addEventHandler(WindowEvent.WINDOW_HIDDEN, e -> watcher.stop());
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void installEscapeHatch(Scene scene, Stage s) {
        // We use a JavaFX Timeline to measure a 5-second hold.
        final Timeline[] holdTimer = new Timeline[1];
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (holdTimer[0] != null && holdTimer[0].getStatus() == Animation.Status.RUNNING) {
                    return;
                }
                holdTimer[0] = new Timeline(new KeyFrame(
                        Duration.seconds(ESCAPE_HATCH_SECONDS),
                        e -> {
                            log.warn("Escape hatch fired — break skipped after {}s hold", ESCAPE_HATCH_SECONDS);
                            dismiss();
                        }
                ));
                holdTimer[0].play();
            }
        });
        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ESCAPE && holdTimer[0] != null) {
                if (holdTimer[0].getStatus() == Animation.Status.RUNNING) {
                    holdTimer[0].stop();
                }
            }
        });
    }

    @Override
    public void dismiss() {
        CountDownLatch latch = this.completed;
        Platform.runLater(() -> {
            if (ring != null) {
                ring.stop();
            }
            if (stage != null) {
                stage.hide();
            }
            if (latch != null) {
                latch.countDown();
            }
        });
    }
}
