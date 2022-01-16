package dk.dtu.tanktrouble.app.model.powerups;

import dk.dtu.tanktrouble.app.model.Tank;

public class PowerUpDoubleBullets extends PowerUp {


    public PowerUpDoubleBullets(long startTime, double x, double y) {
        super(2, startTime, x, y);
    }

    @Override
    public void activate(Tank tank) {
    }

    @Override
    public void deactivate(Tank tank) {

    }


}
