package Reika.GameCalendar.Util;


public class DoublePoint {

	public final double x;
	public final double y;

	public DoublePoint(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int hashCode() {
		return Double.hashCode(x)^Double.hashCode(y);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof DoublePoint) {
			DoublePoint d = (DoublePoint)o;
			return d.x == x && d.y == y;
		}
		return false;
	}

	@Override
	public String toString() {
		return x+","+y;
	}

}
