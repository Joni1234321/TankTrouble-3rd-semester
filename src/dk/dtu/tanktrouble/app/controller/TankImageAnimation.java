package dk.dtu.tanktrouble.app.controller;

import javafx.animation.AnimationTimer;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;

public class TankImageAnimation extends AnimationTimer {

	private final ImageView imageView;

	private long last = System.nanoTime();
	private final ColorAdjust colorAdjust = new ColorAdjust();

	public boolean animateHue = true;

	TankImageAnimation(ImageView imageView) {
		this.imageView = imageView;
		colorAdjust.setSaturation(0.8);
	}

	@Override
	public void handle(long now) {
		double deltaTime = (double) (now - last) / 1e9;
		setRotate(getRotate() + 8 * deltaTime);

		if (animateHue) {
			double hue = getHue();
			while (hue >= 1) {
				hue -= 2;
			}
			setHue(hue + 0.05 * deltaTime);
		}

		last = now;
	}

	public double getRotate() {
		return imageView.getRotate();
	}

	protected void setRotate(double rotate) {
		imageView.setRotate(rotate);
	}

	public double getHue() {
		return colorAdjust.getHue();
	}

	protected void setHue(double hue) {
		colorAdjust.setHue(hue);
		imageView.setEffect(colorAdjust);
	}

	public AnimationSetting getImageSetting() {
		return new AnimationSetting(getHue(), getRotate());
	}

	public void setImageSetting(AnimationSetting animationSetting) {
		setHue(animationSetting.getHue());
		setRotate(animationSetting.getRotationDegrees());
	}

	public static class AnimationSetting {

		private final double hue, rotationDegrees;

		public AnimationSetting() {
			this(Math.random() * 2 - 1, 360 * Math.random());
		}

		public AnimationSetting(double hue, double rotation) {
			this.hue = hue;
			this.rotationDegrees = rotation;
		}

		public double getHue() {
			return this.hue;
		}

		double getRotationDegrees() {
			return this.rotationDegrees;
		}
	}
}