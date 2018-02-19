package hmm;

import hmm.Robot.HiddenRobotInformationKey;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import aima.core.util.math.Matrix;

@SuppressWarnings("hiding")
public class MainWindow<Direction> extends JFrame implements Updateable {
	
	private HmmAgent agent;
	private HiddenRobotInformationKey key;
	private Grid grid;
	
	public MainWindow(HmmAgent agent, HiddenRobotInformationKey key) {
		super("HMM");
		this.agent = agent;
		this.key = key;
		
		grid = new Grid(agent.robot.room);
		add(grid);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}
	
	public void update() {
		// Pull in information from agent.
		final List<State> mostProbableStates = new ArrayList<State>(agent.getMostProbableStates());
		final Matrix probabillites = agent.getProbabillites();
		final Point bestPosition = agent.getMostProbablePosition();
		
		// Get the robots true location. No need to copy here. They are immutable.
		final Point pos = key.getPosition();
		final hmm.Direction dir = key.getDirection();
		
		// Request a redraw of the drawing panel.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				grid.setProbabilites(probabillites);
				grid.setMostProbableStates(mostProbableStates);
				grid.setBestPosition(bestPosition);
				grid.setRealPosition(pos);
				grid.setRealDirection(dir);
				grid.repaint();
			}
		});
	}
	
	private static class Grid extends JComponent implements ComponentListener {
		
		private final static Color[] gradient;
		
		static {
			gradient = new Color[256];
			for (int i = 0; i < gradient.length; i++) {
				gradient[i] = new Color(i, 255 - i, 0);
			}
		}
		
		private static Color sampleGradient(double v, double min, double max) {
			if(min < v && v < max) {
				// Currently I sample the closest color in the gradient
				// So I don't need to create any new color objects...
				return gradient[(int)Math.round((gradient.length-1)*(v-min)/(max-min))];
			} else if(v <= min) {
				return gradient[0];
			} else if(v >= max) {
				return gradient[gradient.length-1];
			}
			// Should not happen...
			return Color.pink;
		}
		
		private Room room;
		
		private int pixelsPerCell = 0;
		
		private Polygon northPoly;
		private Polygon southPoly;
		private Polygon eastPoly;
		private Polygon westPoly;
		private Polygon[] quadrants;
		
		private Matrix probabilities;
		private ArrayList<State> mostProbableStates;
		private Point realPosition;
		private hmm.Direction realDirection;
		
		private Point bestPosition;
		
		public Grid(Room room) {
			this.room = room;
			
			addComponentListener(this);
			
			northPoly = new Polygon();
			westPoly = new Polygon();
			southPoly = new Polygon();
			eastPoly = new Polygon();
			quadrants = new Polygon[]{northPoly, westPoly, southPoly, eastPoly};
			
			probabilities = new Matrix(numberOfStates(), 1);
			mostProbableStates = new ArrayList<State>();
			realPosition = new Point(-1, -1);
			realDirection = hmm.Direction.North;
		}
		
		public void setProbabilites(Matrix probabilites) {
			this.probabilities = probabilites.copy();
		}
		
		public void setMostProbableStates(List<State> mostProbableStates) {
			this.mostProbableStates.clear();
			this.mostProbableStates.addAll(mostProbableStates);
		}
		
		public void setRealPosition(Point position) {
			this.realPosition = position.copy();
		}
		
		public void setRealDirection(hmm.Direction dir) {
			this.realDirection = dir;
		}
		
		public void setBestPosition(Point pos) {
			bestPosition = pos;
		}
		
		@Override
		public Dimension getMinimumSize() {
			return new Dimension(room.width*8, room.height*8);
		};
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(room.width*32, room.height*32);
		}
		@Override
		public Dimension getMaximumSize() {
			return new Dimension(1000, 800);
		}
		
		public void componentResized(ComponentEvent e) {
			// Calculate the size of the grid cells
			pixelsPerCell = Math.min(getWidth()/room.width, getHeight()/room.height);
			
			// Update the cell quadrants
			northPoly = new Polygon(new int[]{0, pixelsPerCell, pixelsPerCell/2}, new int[]{0, 0, pixelsPerCell/2}, 3);
			westPoly = new Polygon(new int[]{0, pixelsPerCell/2, 0}, new int[]{0, pixelsPerCell/2, pixelsPerCell}, 3);
			southPoly = new Polygon(new int[]{0, pixelsPerCell/2, pixelsPerCell}, new int[]{pixelsPerCell, pixelsPerCell/2, pixelsPerCell}, 3);
			eastPoly = new Polygon(new int[]{pixelsPerCell/2, pixelsPerCell, pixelsPerCell}, new int[]{pixelsPerCell/2, 0, pixelsPerCell}, 3);
			quadrants = new Polygon[]{northPoly, westPoly, southPoly, eastPoly};
		}
		
		@Override
		public void paint(Graphics graphics) {
			super.paint(graphics);
			Graphics2D g = (Graphics2D) graphics;
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			int width = room.width;
			int height = room.height;
			
			Color oldColor = g.getColor();
			
			// Draw probabilities
			for (int i = 0; i < numberOfStates(); i++) {
				State s = decode(i);
				int d = s.dir.ordinal();
				int x = s.pos.x;
				int y = s.pos.y;
				
				Polygon poly = new Polygon(quadrants[d].xpoints, quadrants[d].ypoints, quadrants[d].npoints);
				poly.translate(x * pixelsPerCell, y * pixelsPerCell);
				
				g.setColor(sampleGradient(probabilities.get(i, 0), 0, 0.5));
				g.fillPolygon(poly); // Sets the color of each triangle depending of the
			}						// the possibilities
			
			// Draw grid lines
			g.setColor(Color.black);
			
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					g.drawRect(x * pixelsPerCell, y * pixelsPerCell, pixelsPerCell, pixelsPerCell);
					
				}
			}
			
			// Draw true position...
			Stroke oldStroke = g.getStroke();
			g.setStroke(new BasicStroke(4));
			g.setColor(Color.yellow);
			
			g.drawLine( // Draw the real position in yellow
				realPosition.x * pixelsPerCell + pixelsPerCell/2,
				realPosition.y * pixelsPerCell + pixelsPerCell/2,
				realPosition.x * pixelsPerCell + pixelsPerCell/2 + pixelsPerCell/3*realDirection.dx,
				realPosition.y * pixelsPerCell + pixelsPerCell/2 + pixelsPerCell/3*realDirection.dy
			);
			
			g.setStroke(oldStroke);
			
			// Draw most probable positions...
			g.setColor(Color.blue);
			
			for(State s: mostProbableStates) {
				int x = s.pos.x;
				int y = s.pos.y;
				g.drawLine(
					x * pixelsPerCell + pixelsPerCell/2,
					y * pixelsPerCell + pixelsPerCell/2,
					x * pixelsPerCell + pixelsPerCell/2 + pixelsPerCell/3*s.dir.dx,
					y * pixelsPerCell + pixelsPerCell/2 + pixelsPerCell/3*s.dir.dy
				);
			}
			
			if(bestPosition != null) {
				g.drawOval(
					bestPosition.x * pixelsPerCell + pixelsPerCell/2 - 3,
					bestPosition.y * pixelsPerCell + pixelsPerCell/2 - 3,
					6, 6
				);
			}
			
			g.setColor(oldColor);
		}
		
		private State decode(int code) {
			return State.decode(code, room);
		}
		
		private int numberOfStates() {
			return State.numberOfStates(room);
		}
		
		public void componentHidden(ComponentEvent e) { }
		public void componentMoved(ComponentEvent e) { }
		public void componentShown(ComponentEvent e) { }
		
		private static final long serialVersionUID = 87389957599762108L;
	}
	
	
	
	
	private static final long serialVersionUID = 7828545057334247155L;
	
}
