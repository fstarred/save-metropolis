package game;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.StringItem;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import base.Explosion;

public class Game extends MIDlet implements CommandListener {

	private static GameCanvas gameCanvas;
	private static StartCanvas startCanvas;	
	private MenuList menuList;	
	
	public Game() {
		super();			
		menuList = new MenuList(this);		
	}

	protected void startApp() throws MIDletStateChangeException {		
		startCanvas = new StartCanvas(this);				
		Display.getDisplay(this).setCurrent(startCanvas);				
	}		
	
	public void showMenu() {		
		if (gameCanvas != null)			
			menuList.setGameActive(true);		
		Display.getDisplay(this).setCurrent(menuList);
	}
	
	public void dismiss() {
		if (startCanvas!=null) {			
			startCanvas = null;
		}		
		showMenu();
	}
	
	public void startGame() {
		if (gameCanvas == null) 
			gameCanvas = new GameCanvas(this);		
		Display.getDisplay(this).setCurrent(gameCanvas);		
		Manager.startNewGame();
		gameCanvas.start();
	}
	
	public void startDemo() {
		if (gameCanvas == null) 
			gameCanvas = new GameCanvas(this);
		Display.getDisplay(this).setCurrent(gameCanvas);		
		Manager.startDemo();
		gameCanvas.start();		
	}	
	
	public void continueGame() {
		Display.getDisplay(this).setCurrent(gameCanvas);
		gameCanvas.start();
	}
	
	public void showOptions() {
		Form form = new Form("Options");
		Gauge gauge = new Gauge("Max Explosions", true, 5, Explosion.getMaxExplosions());		
		form.append(gauge);		
		Command backCommand = new Command("Back", Command.BACK, 1);
    form.addCommand(backCommand);
    form.setCommandListener(this);
		Display.getDisplay(this).setCurrent(form);
	}
	
	public void showInstructions() {
		final String instructions = "Game commands:\n" +
																"[UP, LEFT, DOWN, RIGHT, CENTER] or\n" +
																"[4] = Left,\n" +
																"[6] = Right,\n" +
																"[2] = Up,\n" +
																"[8] = Down,\n" +
																"[5] = Stop,\n" +
																"[7] = Fire,\n" +
																"[*] = Change side,\n" +
																"[#] = Back to menu\n" +
																"Bonus life\n" +
																"500, 1500, 3000 points\n" +
																"Main objective\n" +
																"kill all monsters in scenario\n" +																
																"hints:\n" +
																"where an 'a' blinks airplane is on scenario\n" +
																"don't let monsters destroy all palaces\n" +
																"protect all navies\n" +
																"destroy terrorist airplane add scores\n" +
																"Have fun !!!\n";
		Form form = new Form("Instructions");
		form.append(new StringItem(null, instructions));
		Command backCommand = new Command("Back", Command.BACK, 1);
    form.addCommand(backCommand);
    form.setCommandListener(this);
		Display.getDisplay(this).setCurrent(form);
	}
	
	public void showAbout() {
		final String about = "Save Metropolis\n" +
												 				"light version:\n" + 
																"copyright of Zenon66(c)\n" +
																"All Rights reserved\n\n" +
																"(c) 2006\n" +
																"Zenon66\n\n" +
																"www.zenon66.altervista.org\n\n" +
																"version:1.0.1\n" +
																"mail to: zenon66@gmail.com\n";
		Form form = new Form("About");
		form.append(new StringItem(null, about));
		Command backCommand = new Command("Back", Command.BACK, 1);
    form.addCommand(backCommand);
    form.setCommandListener(this);
		Display.getDisplay(this).setCurrent(form);
	}
	
	public void quit() {
		try {
			destroyApp(false);
		} catch (MIDletStateChangeException msce) {}
		notifyDestroyed();
	}
	
	
	protected void pauseApp() {
		if (gameCanvas != null) {
			gameCanvas.stop();
		}
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		if (gameCanvas!=null) {
			gameCanvas.stop();
		}
		// release possible resources
		Helicopter.heli = null;	
		Airplane.airplane = null;
		Monster.reset();
		Palace.reset();
		Navy.reset();
		gameCanvas = null;
		startCanvas = null;
	}
	
	public void commandAction(Command c, Displayable d) {
		Display.getDisplay(this).setCurrent(menuList);
		Form f = (Form)d;
		if (f.getTitle() == "Options") {
			Gauge gauge = (Gauge)f.get(0);
			Explosion.setMaxExplosions((byte)gauge.getValue());			
		}
	}
		
}
