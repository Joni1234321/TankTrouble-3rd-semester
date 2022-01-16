package dk.dtu.tanktrouble.data;

import dk.dtu.tanktrouble.app.controller.sprites.records.BulletRecord;
import dk.dtu.tanktrouble.app.controller.sprites.records.TankRecord;

import java.util.List;

public record GameSnapshot(List<TankRecord> tankRecords, List<BulletRecord> bulletRecords, long time) { }
