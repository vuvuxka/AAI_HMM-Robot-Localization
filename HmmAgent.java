package hmm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.text.Position;

import aima.core.util.math.Matrix;

public class HmmAgent implements Updateable {
	
	public final Robot robot;
	
	// Maintain a matrix for forward message
	private Matrix F;
	
	//transition probabilites
	private Matrix T;
	
	//Store the most probable states
	private ArrayList<State> mostProbableStates;
	
	//Store the most probable position
	private Point mostProbablePosition;
	
	public HmmAgent(Robot robot) {
		this.robot = robot;
		
		mostProbableStates = new ArrayList<State>();
		
		int w = robot.room.width;
		int h = robot.room.height;
		
		//Setup matrixes
		F = new Matrix(numberOfStates(), 1);
		
		for (int i = 0; i < numberOfStates(); i++) {
			//F.set(i, 0, 1.0/numberOfStates());
			F.set(i, 0, 10.0);
		}
		
		// Setup transition matrix 
		T = new Matrix(numberOfStates(), numberOfStates());
		for(int c = 0; c < numberOfStates(); c++) {
			if(!decode(c).equals(decode(encode(decode(c)))) || c != encode(decode(c))) {
				System.err.println("Oops!");
				System.exit(0);
			}
			
			State from = decode(c);
			
			// Count the number of "other" directions (that are allowed). If it is at a corner, only 2. 
			int count = 1;
			if (from.pos.x == 0 || from.pos.x == w-1) {
				count++;
			}
			
			if (from.pos.y == 0 || from.pos.y == h-1) {
				count++;
			}
			
			//Check if pointing towards an adjacent wall
			boolean lookingAtWall = !from.pos.translate(from.dir).inside(0, 0, w, h);
			
			double oProb = 0.3;
			if (lookingAtWall) {
				oProb = 1.0;
				count--;
			}
			oProb = oProb / (4-count); // count starts at 1 to count the current state
			
			for (Direction dir: Direction.values()) {
				State to = new State(dir, from.pos.translate(dir));
				
				if(!to.pos.inside(0, 0, w, h)) {
					continue;
				}
				
				int k = encode(to);
				
				double p;
				if(from.dir == dir) { //If we were against a wall it never reaches this
					p = 0.7;
				} else {
					p = oProb;
				}
				
				T.set(c, k, p);
			}
		}
		
		//Sanity checking all rows should sum to 1.0
		sanityCheckTransitionMatrix();
	}

	//Sensor model
	Matrix sensorModel(Point evidence) {
		Matrix O = new Matrix(numberOfStates(), numberOfStates(), 0);
		if(evidence == null) {
			for (int i = 0; i < numberOfStates(); i++) { 
				O.set(i, i, 1.0/numberOfStates());
			}
		} else {
			double sum = 0;
			for (int i = 0; i < numberOfStates(); i++) {
				State s = decode(i);
				int dist = Math.max(Math.abs(s.pos.x-evidence.x), Math.abs(s.pos.y-evidence.y));
				double p = 0.0;
				switch(dist) {
				case 0: p = 0.1/4; break;	// I Divide by 4 here because I have 4 states for each position
				case 1: p = 0.05/4; break;	// One state for each position and direction (Power set).
				case 2: p = 0.025/4; break;	// All these should add up to one.
				}
				O.set(i, i, p);
				sum += p;
			}
			// Normalize, important for readings close to the edge...
			// Or maybe I could normalize later...
			for (int i = 0; i < numberOfStates(); i++) {
				O.set(i, i, O.get(i, i)/sum);
			}
		}
		return O; //diagonal matrix of [P(evidence | X_1^s) | s = 0..S]
	};
	
	// Is notified after every step the robot takes
	public void update() {
		
		Point evidence = robot.getSensorLocation();
		
		Matrix O = sensorModel(evidence);
		
		// Calculate new probability distribution
		Matrix Ttranspose = T.transpose();
		Matrix times = Ttranspose.times(F);
		Matrix times2 = O.times(times);
		F = alphaEquals(times2);
		
		// Find most the most probable 
		mostProbableStates.clear();
		double max = 0.0;
		double fuzziness = 0.03;
		double cutoff = 0.0001;
		for (int i = 0; i < numberOfStates(); i++) {
			State s = decode(i);
			double v = F.get(i,0);
			if(v > max) {
				max = v;
				mostProbableStates.add(s);
				// Prune the list of less probable states: 
				Iterator<State> itr = mostProbableStates.iterator();
				while(itr.hasNext()) {
					int k = encode(itr.next());
					double u = F.get(k, 0);
					if(u < max - fuzziness || u < cutoff) {
						itr.remove();
					}
				}
				
			} else if(v >= max - fuzziness && v >= cutoff) {
				mostProbableStates.add(s);
			}
		}
		
		mostProbablePosition = null;
		max = -1;
		for (int x = 0; x < robot.room.width; x++) {
			for (int y = 0; y < robot.room.height; y++) {
				double sum = 0;
				for (Direction dir: Direction.values()) {
					State s = new State(dir, x, y);
					int code = encode(s);
					sum += F.get(code, 0);
				}
				if (max < sum) {
					max = sum;
					mostProbablePosition = new Point(x, y);
				}
			}
		}
		
	}
	
	public List<State> getMostProbableStates() {
		return Collections.unmodifiableList(mostProbableStates);
	}
	
	public Matrix getProbabillites() {
		return F.copy();
	}
	
	protected double sumAll(Matrix m) {
		double sum = 0.0;
		for (int i = 0; i < m.getRowDimension(); i++) {
			for (int j = 0; j < m.getColumnDimension(); j++) {
				sum += m.get(i, j);
			}
		}
		return sum;
	}
	
	protected Matrix alpha(Matrix m) {
		return m.times(1.0/sumAll(m));
	}
	
	protected Matrix alphaEquals(Matrix m) {
		return m.timesEquals(1.0/sumAll(m));
	}
	
	private int encode(State state) {
		return State.encode(state, robot.room);
	}
	
	private State decode(int code) {
		return State.decode(code, robot.room);
	}
	
	private int numberOfStates() {
		return State.numberOfStates(robot.room);
	}
	
	// For debug:
	
	private void sanityCheckTransitionMatrix() {
		//Sanity checking all rows should sum to 1.0
		for(int i = 0; i < numberOfStates(); i++) {
			State from = decode(i);
			double sum = 0.0;
			for(int k = 0; k < numberOfStates(); k++) {
				sum += T.get(i, k);
			}
			if(Math.abs(sum - 1.0) < 1e-8) {
				continue;
			}
			System.err.print(from.pos);
			System.err.print(" ");
			System.err.print(from.dir);
			System.err.print("\t");
			System.err.println(sum);
		}
	}

	public Point getMostProbablePosition() {
		return mostProbablePosition;
	}
}
