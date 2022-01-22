package dk.dtu.tanktrouble.app.controller.sprites;

import dk.dtu.tanktrouble.data.records.SpriteRecord;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

public abstract class Sprite<R extends SpriteRecord> {
	// ALL SPRITE MUST HAVE A CONSTRUCTOR OF TWO PARAMETERS
	protected R startRecord, stopRecord;
	protected final AnchorPane anchorPane;

	public Sprite(AnchorPane anchorPane, R record) {
		this.anchorPane = anchorPane;
		this.startRecord = record;
		this.stopRecord = record;
	}

	public final void draw(double t, double drawMultiplier) {
		R interpolatedRecord = lerp(startRecord, stopRecord, t);
		drawRecord(interpolatedRecord, drawMultiplier);
	}

	protected abstract void drawRecord(R record, double drawMultiplier);

	protected abstract R lerp(R start, R stop, double t);

	public final void updateStopRecord(SpriteRecord stopRecord) {
		this.startRecord = this.stopRecord;
		//noinspection unchecked
		this.stopRecord = (R) stopRecord;
	}


	public final ColorAdjust getColorAdjuster(Color color) {
		ColorAdjust adj = new ColorAdjust();

		// Because the HUE number range is wierd
		adj.setHue(color.getHue() / 180 - 1);
		adj.setSaturation(color.getSaturation() * 2 - 1);
		adj.setBrightness(color.getBrightness() * 2 - 1);

		return adj;
	}

	public abstract void destroy();
}
