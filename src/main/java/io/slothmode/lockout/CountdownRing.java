package io.slothmode.lockout;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * A calm countdown: a circular ring that drains as time passes, with the
 * remaining seconds centered inside. The grey background ring and the green
 * foreground arc share the same geometry so the green arc sits exactly on
 * top of the grey at all times — it can never "wander".
 */
public final class CountdownRing extends StackPane {

    private static final double RADIUS = 80;
    private static final double STROKE = 8;

    private final Arc ring;
    private final Label seconds;
    private final Timeline timeline;

    public CountdownRing(int totalSeconds) {
        // Grey track: a full circle, same radius and stroke width.
        Circle background = new Circle(RADIUS);
        background.setFill(Color.TRANSPARENT);
        background.setStroke(Color.web("#dddddd"));
        background.setStrokeWidth(STROKE);

        // Green foreground arc: same radius, same stroke width, sits exactly
        // on the grey track. length starts at 360 and drains to 0.
        ring = new Arc(0, 0, RADIUS, RADIUS, 90, 360);
        ring.setType(ArcType.OPEN);
        ring.setFill(Color.TRANSPARENT);
        ring.setStroke(Color.web("#6b8e6b"));
        ring.setStrokeWidth(STROKE);

        seconds = new Label(format(totalSeconds));
        seconds.setStyle("-fx-font-size: 32px; -fx-text-fill: #333;");

        getChildren().addAll(background, ring, seconds);
        setMinSize(200, 200);

        // Smooth drain: interpolate ring.length from 360 to 0 over the whole
        // duration at 60fps. One KeyValue is enough — the timeline fills in
        // the intermediate frames.
        KeyValue drain = new KeyValue(ring.lengthProperty(), 0);
        KeyFrame end = new KeyFrame(Duration.seconds(totalSeconds), drain);
        timeline = new Timeline(end);
        timeline.setCycleCount(1);

        // The countdown number ticks once per second (readable).
        // It lives on a separate Timeline so it can stop with the ring.
        Timeline ticker = new Timeline();
        for (int i = 1; i <= totalSeconds; i++) {
            final int remaining = totalSeconds - i;
            ticker.getKeyFrames().add(new KeyFrame(
                    Duration.seconds(i),
                    e -> seconds.setText(format(remaining))
            ));
        }
        ticker.setCycleCount(1);
        this.ticker = ticker;
    }

    private final Timeline ticker;

    public void start() {
        timeline.playFromStart();
        ticker.playFromStart();
    }

    public void stop() {
        timeline.stop();
        ticker.stop();
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
