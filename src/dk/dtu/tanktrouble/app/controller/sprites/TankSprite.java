package dk.dtu.tanktrouble.app.controller.sprites;

import dk.dtu.tanktrouble.app.model.Tank;
import dk.dtu.tanktrouble.data.records.TankRecord;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import static dk.dtu.tanktrouble.app.controller.TankTroubleAnimator.loadImage;

public class TankSprite extends Sprite<TankRecord> {
	private static final Image tankImage = loadImage("/tank.png");

	final ImageView imageView;

	public TankSprite(AnchorPane anchorPane, TankRecord record) {
		super(anchorPane, record);

		imageView = new ImageView(tankImage);
		imageView.setEffect(getColorAdjuster(Color.hsb((record.hue() + 1) * 180, 1, 0.5)));

		anchorPane.getChildren().addAll(imageView);
	}

	@Override
	protected void drawRecord(TankRecord record, double drawMultiplier) {
		imageView.setLayoutX(record.x() * drawMultiplier);
		imageView.setLayoutY(record.y() * drawMultiplier);
		imageView.setRotate(Math.toDegrees(record.angle()));

		// SET SIZE AND HEGIHT
		imageView.setFitWidth(Tank.WIDTH * drawMultiplier);
		imageView.setFitHeight(Tank.WIDTH * drawMultiplier);
		imageView.setX(-Tank.WIDTH / 2 * drawMultiplier);
		imageView.setY(-Tank.HEIGHT / 2 * drawMultiplier);


		if (!startRecord.isAlive()) {
			imageView.setOpacity(0.1);
		} else {
			imageView.setOpacity(1);
			imageView.toFront();
		}
	}

	@Override
	protected TankRecord lerp(TankRecord start, TankRecord stop, double t) {
		double x = TankMath.lerp(start.x(), stop.x(), t);
		double y = TankMath.lerp(start.y(), stop.y(), t);

		double diff = TankMath.getAngleDiff(start.angle(), stop.angle());
		double angle = TankMath.lerp(start.angle(), start.angle() + diff, t);
		double hue = TankMath.lerp(start.hue(), stop.hue(), t);

		return new TankRecord(stop.id(), x, y, TankMath.clampAngle(angle), stop.isAlive(), hue);
	}

	@Override
	public void destroy() {
		anchorPane.getChildren().remove(imageView);
	}
}