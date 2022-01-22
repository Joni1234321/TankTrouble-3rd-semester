package dk.dtu.tanktrouble.app.controller.sprites;

import dk.dtu.tanktrouble.app.model.powerups.PowerUp;
import dk.dtu.tanktrouble.data.PowerUpData;
import dk.dtu.tanktrouble.data.records.PowerUpRecord;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class PowerUpSprite extends Sprite<PowerUpRecord> {
	final ImageView imageView;

	public PowerUpSprite(AnchorPane anchorPane, PowerUpRecord record) {
		super(anchorPane, record);

		Image image = PowerUpData.getImageFromClass(record.tankMod());
		imageView = new ImageView(image);

		//imageView.setEffect(getColorAdjuster(colorRecord));

		anchorPane.getChildren().addAll(imageView);
	}

	@Override
	protected void drawRecord(PowerUpRecord record, double drawMultiplier) {
		imageView.setLayoutX(record.x() * drawMultiplier);
		imageView.setLayoutY(record.y() * drawMultiplier);
		imageView.setRotate(Math.toDegrees(record.angle()));

		// SET SIZE AND HEGIHT
		imageView.setFitWidth(PowerUp.SIZE * drawMultiplier);
		imageView.setFitHeight(PowerUp.SIZE * drawMultiplier);
		imageView.setX(-PowerUp.SIZE / 2 * drawMultiplier);
		imageView.setY(-PowerUp.SIZE / 2 * drawMultiplier);
	}

	@Override
	protected PowerUpRecord lerp(PowerUpRecord start, PowerUpRecord stop, double t) {
		double x = TankMath.lerp(start.x(), stop.x(), t);
		double y = TankMath.lerp(start.y(), stop.y(), t);

		double diff = TankMath.getAngleDiff(start.angle(), stop.angle());
		double angle = TankMath.lerp(start.angle(), start.angle() + diff, t);

		return new PowerUpRecord(stop.id(), x, y, angle, stop.tankMod());
	}

	@Override
	public void destroy() {
		anchorPane.getChildren().remove(imageView);
	}
}
