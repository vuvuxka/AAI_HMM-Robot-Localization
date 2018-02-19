package hmm;

public class State {
		final Direction dir;
		final Point pos;
		
		State(Direction dir, int x, int y) {
			this.dir = dir;
			this.pos = new Point(x, y);
		}
		
		public State(Direction dir, Point p) {
			this(dir, p.x, p.y);
		}

//		public State copy() {
//			return new State(dir, pos);
//		}
		
		public static int encode(State state, Room room) {
			int width = room.width;
			
			int dir = state.dir.ordinal();
			int x = state.pos.x;
			int y = state.pos.y;
			
			return dir+4*(x+width*y);
		}
		
		public static State decode(int code, Room room) {
			int width = room.width;
			
			int dir = code%4; code /=4;
			int x = code%width; code /= width;
			int y = code;
			return new State(Direction.values()[dir], x, y);
		}
		
		public static int numberOfStates(Room room) {
			return 4*room.width*room.height;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dir == null) ? 0 : dir.hashCode());
			result = prime * result + ((pos == null) ? 0 : pos.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			State other = (State) obj;
			if (dir != other.dir)
				return false;
			if (pos == null) {
				if (other.pos != null)
					return false;
			} else if (!pos.equals(other.pos))
				return false;
			return true;
		}
		
		
	}