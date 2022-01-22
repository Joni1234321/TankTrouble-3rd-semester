package dk.dtu.tanktrouble.data;

import dk.dtu.tanktrouble.app.model.powerups.*;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PowerUpData {

	//TODO Give reall nams and desciptions
	public static final List<PowerUpMetaData> powerUps = Arrays.asList(
			new PowerUpMetaData("Shotgun", "Sprays several bullets for three shots", TankModShotgun.class, "/powerups/shotgun.png"),
			new PowerUpMetaData("Double-Up", "Gives you 10 extra bullets", TankModDoubleBullets.class, "/powerups/double.png"),
			new PowerUpMetaData("Grenade", "Shoots a bullet that explodes", TankModGrenade.class, "/powerups/frag.png"),
			new PowerUpMetaData("Bounce", "Shoots a harmless rubber ball that bounces on tanks", TankModBouncyBall.class, "/powerups/bounce.png"),
			new PowerUpMetaData("Mine", "Place 4 landmines that explode upon touch", TankModMine.class, "/powerups/mine.png"),
			new PowerUpMetaData("Ray", "Shoot a ray that penetrates through walls", TankModDeathRay.class, "/powerups/ray.png"));

	public static final Map<Class<? extends TankMod>, PowerUpMetaData> dataFromClass = powerUps.stream().collect(Collectors.toMap(PowerUpMetaData::getClassObject, x -> x));

	public static Image getImageFromClass(Class<? extends TankMod> aClass) {
		return dataFromClass.get(aClass).getImage();
	}

	public static List<Class<? extends TankMod>> getActive() {
		return powerUps.stream().filter(PowerUpMetaData::isActive).map(PowerUpMetaData::getClassObject).collect(Collectors.toList());
	}

	public static void updatePowerUp(int index, boolean newState) {
		powerUps.get(index).isActive = newState;
	}

	public static class PowerUpMetaData {
		private Image image;
		String name;
		String description;
		final Class<? extends TankMod> classObject;
		String imagePath;

		public int getId() {
			return id;
		}

		int id;

		@Override
		public String toString() {
			return "PowerUpMetaData{" +
					"image=" + image +
					", name='" + name + '\'' +
					", description='" + description + '\'' +
					", classObject=" + classObject +
					", imagePath='" + imagePath + '\'' +
					", id=" + id +
					", isActive=" + isActive +
					'}';
		}

		private static int powerupCounter = 0;

		public boolean isActive = true;


		public PowerUpMetaData(String name, String description, Class<? extends TankMod> classObject, String imagePath) {
			if (imagePath.equals("")) {
				imagePath = "/powerups/none.png";
			}
			this.id = powerupCounter++;
			this.imagePath = imagePath;
			this.classObject = classObject;
			this.name = name.equals("") ? "Name not set" : name;
			this.description = description;
		}

		private Image loadImage(String path) {
			InputStream tankInputStream = getClass().getResourceAsStream(path);
			if (tankInputStream == null)
				tankInputStream = getClass().getResourceAsStream("/powerups/none.png");
			assert tankInputStream != null;
			return new Image(tankInputStream);
		}

		public Image getImage() {
			if (image == null)
				image = loadImage(imagePath);
			return image;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public Class<? extends TankMod> getClassObject() {
			return classObject;
		}

		public boolean isActive() {
			return isActive;
		}
	}
}
