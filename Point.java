package hmm;

public class Point {
	public final int x, y;
	
	public Point() {
		this(0, 0);
	}
	
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Point copy() {
		return translate(0, 0);
	}
	
	public Point translate(Direction dir) {
		return translate(dir.dx, dir.dy);
	}
	
	public Point translate(int dx, int dy) {
		return new Point(x + dx, y + dy);
	}
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point other = (Point) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	public String toString() {
		return "(" + x + ", " + y + ")";
	}

	public boolean inside(int x1, int y1, int x2, int y2) {
		return x1 <= x && x < x2 && y1 <= y && y < y2;
	}
	
}
