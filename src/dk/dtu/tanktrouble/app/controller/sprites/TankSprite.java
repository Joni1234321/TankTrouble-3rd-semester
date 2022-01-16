package dk.dtu.tanktrouble.app.controller.sprites;

import dk.dtu.tanktrouble.app.controller.sprites.records.TankRecord;
import dk.dtu.tanktrouble.app.model.Tank;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class TankSprite extends Sprite<TankRecord> {
    final ImageView imageView;

    public TankSprite(Image tankImage, AnchorPane anchorPane, TankRecord record, double hue) {
        super(anchorPane, record);

        imageView = new ImageView();
        imageView.setImage(tankImage);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setHue(hue);
        colorAdjust.setSaturation(0.8);
        imageView.setEffect(colorAdjust);

        anchorPane.getChildren().addAll(imageView);
    }

    @Override
    protected void drawRecord(TankRecord record, double drawMultiplier) {
        imageView.setLayoutX(record.x() * drawMultiplier);
        imageView.setLayoutY(record.y() * drawMultiplier);
        imageView.setRotate(Math.toDegrees(record.angle()));

        // SET WIDTH AND HEGIHT
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
    protected TankRecord lerp (TankRecord start, TankRecord stop, double t) {
        double x = TankMath.lerp(start.x(), stop.x(), t);
        double y = TankMath.lerp(start.y(), stop.y(), t);

        double diff = TankMath.getAngleDiff(start.angle(), stop.angle());
        double angle = TankMath.lerp(start.angle(), start.angle() + diff, t);

        return new TankRecord(stop.id(), x, y, TankMath.clampAngle(angle), stop.isAlive());
    }

    @Override
    public void destroy() {
        anchorPane.getChildren().remove(imageView);
    }
}