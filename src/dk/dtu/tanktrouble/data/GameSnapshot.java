package dk.dtu.tanktrouble.data;

import dk.dtu.tanktrouble.data.records.BulletRecord;
import dk.dtu.tanktrouble.data.records.PowerUpRecord;
import dk.dtu.tanktrouble.data.records.TankRecord;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public record GameSnapshot(List<TankRecord> tankRecords, List<BulletRecord> bulletRecords,
						   List<PowerUpRecord> powerUpRecords, long time) implements Serializable {

	public GameSnapshot(List<TankRecord> tankRecords, long now) {
		this(tankRecords, new ArrayList<>(), new ArrayList<>(), now);
	}
}
