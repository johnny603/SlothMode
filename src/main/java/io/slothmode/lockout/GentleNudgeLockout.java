package io.slothmode.lockout;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * A non-fullscreen toast nudge for users who pick "gentle mode".
 *
 * Shows a small window near the top-right of the screen for the break
 * duration, then disappears. It does not block input — the user is
 * trusted to take the break.
 */
public final class GentleNudgeLockout implements LockoutImplementor {

    private static final Logger log = LoggerFactory.getLogger(GentleNudgeLockout.class);

    private volatile Popup popup;
    private volatile CountDownLatch completed;

    @Override
    public void present(int durationSeconds) {
        CountDownLatch latch = new CountDownLatch(1);
        this.completed = latch;

        Platform.runLater(() -> {
            Label sloth = new Label("🦥");
            sloth.setStyle("-fx-font-size: 36px;");

            Label message = new Label("Time for a break — " + durationSeconds + "s");
            message.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

            VBox box = new VBox(8, sloth, message);
            box.setAlignment(Pos.CENTER);
            box.setStyle(
                    "-fx-background-color: #f5f1e8;" +
                    "-fx-padding: 16;" +
                    "-fx-border-color: #6b8e6b;" +
                    "-fx-border-width: 2;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;"
            );

            Popup p = new Popup();
            p.getContent().add(box);
            p.setAutoHide(false);
            this.popup = p;

            // Place near top-right.
            var screen = javafx.stage.Screen.getPrimary().getVisualBounds();
            p.show(
                    javafx.stage.Window.getWindows().stream()
                            .filter(w -> w instanceof javafx.stage.Stage)
                            .findFirst()
                            .orElse(null),
                    screen.getMaxX() - 320,
                    screen.getMinY() + 60
            );

            log.info("Gentle nudge presented for {}s", durationSeconds);

            PauseTransition pause = new PauseTransition(Duration.seconds(durationSeconds));
            pause.setOnFinished(e -> {
                log.info("Gentle nudge elapsed");
                dismiss();
            });
            pause.play();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void dismiss() {
        CountDownLatch latch = this.completed;
        Platform.runLater(() -> {
            if (popup != null) {
                popup.hide();
            }
            if (latch != null) {
                latch.countDown();
            }
        });
    }
}
