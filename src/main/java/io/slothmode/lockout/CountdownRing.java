package io.slothmode.lockout;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * A calm countdown: a circular ring that drains as time passes, with the
 * remaining seconds centered inside. Runs on the JavaFX Application Thread.
 */
public final class CountdownRing extends StackPane {

    private final Arc ring;
    private final Label seconds;
    private final Timeline timeline;

    public CountdownRing(int totalSeconds) {
        Circle background = new Circle(80);
        background.setStyle("-fx-fill: transparent; -fx-stroke: #ddd; -fx-stroke-width: 8;");

        ring = new Arc(0, 0, 80, 80, 90, 360);
        ring.setType(ArcType.OPEN);
        ring.setStyle("-fx-fill: transparent; -fx-stroke: #6b8e6b; -fx-stroke-width: 8;");
        ring.setStrokeWidth(8);

        seconds = new Label(format(totalSeconds));
        seconds.setStyle("-fx-font-size: 32px; -fx-text-fill: #333;");

        getChildren().addAll(background, ring, seconds);
        setMinSize(200, 200);

        timeline = new Timeline();
        for (int i = 0; i <= totalSeconds; i++) {
            final int remaining = totalSeconds - i;
            final double arc = 360.0 * (remaining / (double) totalSeconds);
            timeline.getKeyFrames().add(new KeyFrame(
                    Duration.seconds(i),
                    e -> {
                        ring.setLength(arc);
                        seconds.setText(format(remaining));
                    }
            ));
        }
        timeline.setCycleCount(1);
    }

    public void start() {
        timeline.playFromStart();
    }

    public void stop() {
        timeline.stop();
    }

    /** True when the animation has finished (i.e. the break is over). */
    public boolean isFinished() {
        return timeline.getStatus() == Animation.Status.STOPPED
                && timeline.getCurrentTime().greaterThanOrEqualTo(timeline.getTotalDuration());
    }

    private static String format(int seconds) {
        return seconds >= 60
                ? String.format("%d:%02d", seconds / 60, seconds % 60)
                : String.valueOf(seconds);
    }
}
