package dk.dtu.tanktrouble.data;

import javafx.scene.paint.Color;

import java.io.*;

public class ColorSerialized implements Serializable {
	private Color color;

	public ColorSerialized(Color color) {
		this.color = color;
	}

	public double hue() {
		return color.getHue();
	}

	public double sat() {
		return color.getSaturation();
	}

	public double bri() {
		return color.getBrightness();
	}

	public Color color() {
		return color;
	}

	@Serial
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		double hue = in.readDouble();
		double sat = in.readDouble();
		double bri = in.readDouble();
		color = Color.hsb(hue, sat, bri);
	}

	@Serial
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeDouble(color.getHue());
		out.writeDouble(color.getSaturation());
		out.writeDouble(color.getBrightness());
	}


}
