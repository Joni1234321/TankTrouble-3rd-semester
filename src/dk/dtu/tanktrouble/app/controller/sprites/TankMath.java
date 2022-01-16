package dk.dtu.tanktrouble.app.controller.sprites;

//MathTank
public class TankMath {
    private static final double PI2 = Math.PI * 2;

    // t is being set to between 1 and 0
    public static double lerp (double from, double to, double t) {
        if (t > 1) return to;
        else if (t < 0) return from;

        return from + (to - from) * t;
    }


    //region angle clipping
    public static double clampAngle(double angle) {
        return (((angle % PI2) + PI2) % PI2); // positive modulo: https://stackoverflow.com/questions/5385024/mod-in-java-produces-negative-numbers
    }
    public static double getAngleDiff(double from, double to) {
        double dAngle = to - from;

        if (dAngle > Math.PI) {
            // ROTATE LEFT
            dAngle -= PI2;
        }
        else if (dAngle < -Math.PI) {
            // ROTATE RIGHT
            dAngle += PI2;
        }
        return dAngle;
    }
    //endregion

    public static long getTimeOffset() {
        return System.currentTimeMillis()*1000000 - System.nanoTime();
    }
}
