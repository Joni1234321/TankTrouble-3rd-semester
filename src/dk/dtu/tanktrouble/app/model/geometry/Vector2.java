package dk.dtu.tanktrouble.app.model.geometry;

import java.io.Serializable;

public class Vector2 implements Serializable {
    public final double x;
    public final double y;

    public Vector2 (double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Add two vectors and return new vector
    public Vector2 add (Vector2 v0) {
        return new Vector2(x+v0.x, y+v0.y);
    }

    // Subtract v0 from this and return new vector
    public Vector2 subtract (Vector2 v0) {
        return new Vector2(x-v0.x, y-v0.y);
    }

    // Multiply vector with value
    public Vector2 multiply (double val) {
        return new Vector2(val*x, val*y);
    }

    // Normalize vector
    public Vector2 normalize () {
        return multiply(1/length());
    }

    // Get length of vector
    public double length () {
        return Math.sqrt(x*x + y*y);
    }

    // Get determinant of two vectors
    public double det (Vector2 v0) {
        return x*v0.y - v0.x*y;
    }

    // Get dot product of two vectors
    public double dot (Vector2 v0) {
        return x*v0.x + y*v0.y;
    }

    // Returns the intersection point, otherwise null
    public static double intersect (Vector2 p1, Vector2 r1, Vector2 p2, Vector2 r2) {
        double det = r1.det(r2);

        if (det != 0) { // If lines are parallel, they are not counted as intersecting

            double t = (r2.y*(p2.x-p1.x) - r2.x*(p2.y-p1.y)) / det;
            double s = (r1.y*(p2.x-p1.x) - r1.x*(p2.y-p1.y)) / det;

            if (0 <= s && s <= 1 && 0 <= t && t <= 1) {
                return t-0.001;
            }
        }
        return Double.NaN;
    }

    public Vector2 rot90 () {
        return new Vector2(-y, +x);
    }

    public Vector2 reflect (Vector2 d) {
        Vector2 n = rot90().normalize();
        return d.subtract(n.multiply( 2*(d.dot(n)) )).normalize();
    }
}