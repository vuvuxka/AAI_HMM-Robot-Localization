package hmm;

import hmm.Robot.HiddenRobotInformationKey;

public class Statistics implements Updateable {
	
	HiddenRobotInformationKey key;
	HmmAgent agent;
	
	double averageNorm1Dist;
	double standardDesviation;
	double    numSamples;
	
	public Statistics(HiddenRobotInformationKey key, HmmAgent agent) {
		this.key = key;
		this.agent = agent;
		
		standardDesviation = 0;
		averageNorm1Dist = 0;
		numSamples = 0;
	}
	
	
	@Override
	public void update() {
		updateNorm1Dist();
	}


	private void updateNorm1Dist() {
		Point real = key.getPosition();
		Point guess = agent.getMostProbablePosition();
		
		double dist = Math.abs(real.x-guess.x) + Math.abs(real.y - guess.y);
		
		averageNorm1Dist = (averageNorm1Dist * numSamples + dist)/(numSamples+1);
		if (numSamples != 0) 
			{standardDesviation = (dist - averageNorm1Dist)*(dist - averageNorm1Dist)*(1/numSamples) + (numSamples/(numSamples + 1))*standardDesviation;}
		numSamples++;
		
		System.out.println((int)numSamples + "\tAverage: " + 
				Math.round(averageNorm1Dist*Math.pow(10,3))/Math.pow(10,3)+
				"\tStandard Desviation:" + Math.round((Math.sqrt(standardDesviation))*Math.pow(10,3))/Math.pow(10,3));
	}

}
