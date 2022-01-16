package dk.dtu.tanktrouble.app.controller.sprites;

import javafx.scene.layout.AnchorPane;

public abstract class Sprite<R> {
    protected R startRecord, stopRecord;
    protected final AnchorPane anchorPane;

    public Sprite(AnchorPane anchorPane, R record) {
        this.anchorPane = anchorPane;
        this.startRecord = record;
        this.stopRecord = record;
    }

    public final void draw (R start, R stop, double t, double drawMultiplier) {
        startRecord = start;
        stopRecord = stop;
        draw(t, drawMultiplier);
    }
    public final void draw (double t, double drawMultiplier) {
        R interpolatedRecord = lerp(startRecord, stopRecord, t);
        drawRecord(interpolatedRecord, drawMultiplier);
    }

    protected abstract void drawRecord (R record, double drawMultiplier);
    protected abstract R lerp (R start, R stop, double t);

    public final void updateStopRecord(R stopRecord) {
        this.startRecord = this.stopRecord;
        this.stopRecord = stopRecord;
    }

    public abstract void destroy ();
}
