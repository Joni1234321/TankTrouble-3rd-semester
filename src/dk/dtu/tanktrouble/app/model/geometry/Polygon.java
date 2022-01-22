package dk.dtu.tanktrouble.app.model.geometry;

import java.io.Serializable;

public record Polygon(Vector2[] pts, int n) implements Serializable {
	public Polygon(Vector2[] pts) {
		this(pts, pts.length);
	}

	public boolean intersect(Polygon other) {
		for (int i = 0; i < other.n; i++) {
			Vector2 p1 = other.pts[i];
			Vector2 r1 = other.pts[(i + 1) % other.n].subtract(other.pts[i]);

			if (intersect(p1, r1)) return true;
		}
		return false;
	}

	public Object[] reflect(Vector2 p1, Vector2 r1) {
		double bestT = 2;
		Object[] out = null;
		for (int i = 0; i < n; i++) {
			Vector2 p2 = pts[i];
			Vector2 r2 = pts[(i + 1) % n].subtract(pts[i]);

			double t = Vector2.intersect(p1, r1, p2, r2);
			if (!Double.isNaN(t)) {
				if (t < bestT) {
					bestT = t;
					out = new Object[]{t, r2.reflect(r1)};
				}
			}
		}
		return out;
	}

	public boolean intersect(Vector2 p1, Vector2 r1) {
		return reflect(p1, r1) != null;
	}

	public boolean isInside(Vector2 p) {
		double sum = 0;

		for (int i = 0; i < n; i++) {
			Vector2 v1 = pts[i].subtract(p);
			Vector2 v2 = pts[(i + 1) % n].subtract(p);
			double angle = Math.atan2(v1.det(v2), v1.dot(v2));
			sum += angle;
		}

		return Math.abs(Math.abs(sum) - 2 * Math.PI) < 1e-9;
	}

	public Polygon moveAndRotate(Vector2 p, double angle) {
		Vector2[] newPts = new Vector2[pts.length];

		for (int i = 0; i < pts.length; i++)
			newPts[i] = pts[i].moveAndRotate(p, angle);

		return new Polygon(newPts);
	}
}