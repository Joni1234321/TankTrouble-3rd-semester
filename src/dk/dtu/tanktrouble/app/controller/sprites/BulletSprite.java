package dk.dtu.tanktrouble.app.controller.sprites;

import dk.dtu.tanktrouble.data.records.BulletRecord;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;

public class BulletSprite extends Sprite<BulletRecord> {
	final double MIN_FADE = 0.1, FADE_TIME = .666;
	final Circle circle;

	public BulletSprite(AnchorPane anchorPane, BulletRecord record) {
		super(anchorPane, record);

		circle = new Circle(record.size(), record.colorRecord().color());

		anchorPane.getChildren().addAll(circle);
	}

	@Override
	protected void drawRecord(BulletRecord record, double drawMultiplier) {
		circle.setRadius(record.size() * drawMultiplier);
		circle.setCenterX(record.x() * drawMultiplier);
		circle.setCenterY(record.y() * drawMultiplier);
		circle.toBack();

		double timeOut = startRecord.timeTillTimeout();
		circle.setOpacity(TankMath.lerp(MIN_FADE, 1, (timeOut / FADE_TIME)));
		circle.setFill(record.colorRecord().color());
	}

	@Override
	protected BulletRecord lerp(BulletRecord start, BulletRecord stop, double t) {
		double x = TankMath.lerp(start.x(), stop.x(), t);
		double y = TankMath.lerp(start.y(), stop.y(), t);
		double size = TankMath.lerp(start.size(), stop.size(), t);
		double timeTillTimeout = TankMath.lerp(start.timeTillTimeout(), stop.timeTillTimeout(), t);

		return new BulletRecord(stop.id(), x, y, size, timeTillTimeout, stop.colorRecord());
	}

	@Override
	public void destroy() {
		anchorPane.getChildren().remove(circle);
	}
}
