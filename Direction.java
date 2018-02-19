package hmm;

public enum Direction {
	North(0, -1), West(-1, 0), South(0, 1), East(1, 0);
	private Direction opposite;
	public final int dx, dy; 
	
	static {
		North.opposite = South;
		South.opposite = North;
		West.opposite = East;
		East.opposite = West;
	}
	
	Direction(int dx, int dy) {
		this.dx = dx;
		this.dy = dy;
	}
	
	public Direction opposite() {
		return opposite;
	}
	
	public String toString() {
		return name();
	}
	
}
