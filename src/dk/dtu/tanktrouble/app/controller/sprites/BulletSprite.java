package dk.dtu.tanktrouble.app.controller.sprites;

import dk.dtu.tanktrouble.app.controller.sprites.records.BulletRecord;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class BulletSprite extends Sprite<BulletRecord> {
    final double MIN_FADE = 0.2, FADE_TIME = .666, BULLET_SIZE = 0.03;
    final Circle circle;

    public BulletSprite(AnchorPane anchorPane, BulletRecord record) {
        super(anchorPane, record);

        circle = new Circle(BULLET_SIZE, Color.BLACK);
        anchorPane.getChildren().addAll(circle);
    }

    @Override
    protected void drawRecord(BulletRecord record, double drawMultiplier) {
        circle.setRadius(BULLET_SIZE * drawMultiplier);
        circle.setCenterX(record.x() * drawMultiplier);
        circle.setCenterY(record.y() * drawMultiplier);
        circle.toBack();

        double timeOut = startRecord.timeTillTimeout();
        circle.setOpacity(TankMath.lerp(MIN_FADE, 1, (timeOut / FADE_TIME)));
    }

    @Override
    protected BulletRecord lerp(BulletRecord start, BulletRecord stop, double t) {
        return new BulletRecord(stop.id(), TankMath.lerp(start.x(), stop.x(), t), TankMath.lerp(start.y(), stop.y(), t), TankMath.lerp(start.timeTillTimeout(), stop.timeTillTimeout(), t));
    }

    @Override
    public void destroy() {
        anchorPane.getChildren().remove(circle);
    }
}
