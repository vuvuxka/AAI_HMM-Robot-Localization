package hmm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

// Represents the simulated robot. Contains it's true location in the room
public class Robot implements Updateable {
	public final Room room;
	private Point position; // The robots true position inside the room.
	private Direction direction;
	private Point sensorSample;
	
	private Random random = new Random();
	
	public Robot(Room room) {
		this.room = room;
		position = new Point(room.width/2, room.height/2);
		direction = Direction.North;
		sensorSample = null;
	}
	
	public Robot(Room room, HiddenRobotInformationKey key) {
		this(room);
		key.setRobot(this);
	}

	public Point getSensorLocation() {
		return sensorSample;
	}
	
	private void updateSensor() {
		int w = room.width;
		int h = room.height;
		// Generate a location for the sensor
		Point candidate;
		
		int r = random.nextInt(10);
		
		if(r < 1) {				// Spot on
			candidate = position.copy();
		} else if(r-1 < 4) {	// One step off
			ArrayList<Point> candidates = new ArrayList<Point>();
			for (int dx = -1; dx <= 1; dx++) {
				for (int dy = -1; dy <= 1; dy++) {
					if((dx == 0 && dy == 0) || !position.translate(dx, dy).inside(0, 0, w, h)) {
						continue;
					}
					candidates.add(position.translate(dx, dy));
				}
			}
			if(!candidates.isEmpty()) {
				candidate = candidates.get(random.nextInt(candidates.size()));
			} else {
				System.err.println("updateSensor: No candidates for 1 offset from " + position);
				candidate = null;
			}
		} else if(r-5 < 4) {	// Two steps off
			ArrayList<Point> candidates = new ArrayList<Point>();
			for (int dx = -2; dx <= 2; dx++) {
				for (int dy = -2; dy <= 2; dy++) {
					if((Math.abs(dx) <= 1 && Math.abs(dy) <= 1) || !position.translate(dx, dy).inside(0, 0, w, h)) {
						continue;
					}
					candidates.add(position.translate(dx, dy));
				}
			}
			if(!candidates.isEmpty()) {
				candidate = candidates.get(random.nextInt(candidates.size()));
			} else {
				System.err.println("updateSensor: No candidates for 2 offset from " + position);
				candidate = null;
			}
		} else {				// "Nothing"
			candidate = null;
		}
		
		sensorSample = candidate;
	}
	
	private void updateDirection() {
		int w = room.width;
		int h = room.height;
		//Check if pointing towards an adjecent wall
		boolean lookingAtWall = !position.translate(direction).inside(0, 0, w, h);
		int r = random.nextInt(10);
		
		if(lookingAtWall || r < 3) {
			Direction[] otherDirs = new Direction[3];
			Direction[] allDirs = Direction.values();
			int i = 0;
			for(Direction candidate: allDirs) {
				// Exclude the current direction.
				if(direction == candidate) {
					continue;
				}
				
				// Deal with walls.
				if(!position.translate(candidate).inside(0, 0, w, h)) {
					continue;
				}
				
				// If we get here then the allDirs[k] is a potential new direction.
				otherDirs[i++] = candidate;
			}
			if(i != 0) {
				direction = otherDirs[random.nextInt(i)];
			} else {
				throw new RuntimeException(
					"Could not choose new direction.\n" +
					"position:"		+ position			+ ", " +
					"direction: "	+ direction			+ ", " +
					"otherDirs: "	+ Arrays.toString(otherDirs)
				);
			}
		}
	}
	
	private void updatePosition() {
		// Take one step in the direction the robot is heading in.
		position = position.translate(direction.dx, direction.dy);
	}
	
	public void update() {
		updateDirection();
		updatePosition();
		updateSensor();
		System.err.println("Position: " + position);
	}
	
	public static class HiddenRobotInformationKey {
		Robot r;
		
		private void setRobot(Robot r) {
			this.r = r;
		}
		
		Point getPosition() {
			return r.position; 
		}
		
		Direction getDirection() {
			return r.direction;
		}
		
	}
	
	
}


