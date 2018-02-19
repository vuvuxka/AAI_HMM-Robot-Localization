package hmm;

import hmm.Robot.HiddenRobotInformationKey;

public class Main {
	
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		HiddenRobotInformationKey key = new HiddenRobotInformationKey();
		Room        room     = new Room(10, 10);
		Robot       robot    = new Robot(room, key);
		HmmAgent    hmmAgent = new HmmAgent(robot);
		Statistics  stats    = new Statistics(key, hmmAgent); 
		MainWindow  window   = new MainWindow(hmmAgent, key);
		
		Updateable[] objects = new Updateable[]{
				robot,
				hmmAgent,
				stats,
				window,
		};
		
		mainLoop(objects);
		System.out.println("Done!");
	}

	private static void mainLoop(Updateable[] objects) {
		int n = 1000; //Stop after 1000 frames
		long frameSpeed = (long)(10/1); // 0.25 fps
		while(n-->0) {
			for(Updateable object: objects) {
				object.update();
			}
			simpleSleep(frameSpeed);
		}
		
	}

	private static void simpleSleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.interrupted();
		}
	}
	
}
