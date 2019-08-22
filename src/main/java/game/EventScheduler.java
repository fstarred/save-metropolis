package game;

import java.util.TimerTask;

public class EventScheduler extends TimerTask {

	private byte request;
	
	
	public EventScheduler(int request) {
		super();
		this.request = (byte)request;
	}

	public void run() {
		Manager.setGameState(request);
	}

}
