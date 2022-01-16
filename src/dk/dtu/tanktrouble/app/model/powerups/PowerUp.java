package dk.dtu.tanktrouble.app.model.powerups;

import dk.dtu.tanktrouble.app.model.Tank;

public abstract class PowerUp {

    public Tank tank;
    public final double x;
    public final double y;
    public final long startTime;
    public final double duration_s;

    public PowerUp(double duration_s, long startTime, double x, double y) {
        this.startTime = startTime;
        this.duration_s = duration_s;
        this.x = x;
        this.y = y;
    }

    public abstract void activate(Tank tank);
    public abstract void deactivate(Tank tank);
}
