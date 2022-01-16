package dk.dtu.tanktrouble.app.controller.sprites.records;

import dk.dtu.tanktrouble.app.model.gameMap.GameMap;

import java.io.Serializable;
import java.util.List;

public record LevelRecord(GameMap map, List<TankRecord> tankRecords) implements Serializable {
}
