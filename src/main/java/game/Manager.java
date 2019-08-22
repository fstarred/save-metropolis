package game;


import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.Timer;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import base.Camera;
import base.Explosion;
import base.NewScoreEntry;
import base.Scenario;

public class Manager {
	
	private static int lastKeyPressed;
		
	public static int generalAnimationTick;
	public static Random random;
		
	private static int democamx;
	private static int democamy;		
		
	private static String scenarioTitle;
	private static byte level;		
	
	private static Timer timer;
	private static EventScheduler eventScheduler;
	
	private static byte gameState;
		
	public static final byte PLAYING_STATE = 0;
	public static final byte STARTLEVEL_STATE = 1;
	public static final byte GAMEOVER_STATE = 2;
	public static final byte SCENARIOCOMPLETED_STATE = 3;		
	public static final byte NEWHIGHSCORE_STATE = 4;
	public static final byte SHOWHIGHSCORES_STATE = 5;	
	public static final byte GAMECOMPLETED_STATE = 6;
	
	private final static byte totalScenarios = 4;
		
	//private static boolean paused = false;
	private static boolean scenarioLoaded = false;
	
	private static long gameDuration;
  private static long startTime;  
  
  private static int scenarioScore;
  private static int scorePoints;  
  private static Scores scores;
  private static byte nextScoreBonus;
  private static final int scoreBonus[] = {500, 1500, 3000, 99999};
		
	private static Font smallFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
	private static int smallFontHeight = smallFont.getHeight();
	
	private static NewScoreEntry newScoreEntry;
	
	private static byte heliLives;
	private static byte monstersRemains;
	private static byte totalPalaces;
	private static byte totalNavies;
	private static byte palacesDestroyed;
	private static byte naviesDestroyed;	
	
	/* snow[][] = { {idx},
	 * 							{x}, {y}, {oscillationTick}	{speed} } 
	 */
	private static int snow[][];	
	
	public static void setRunningStatus(boolean mode) {
		boolean paused = !mode;		
		if (!paused) {
			startTime = System.currentTimeMillis() - gameDuration;
		} else {
			gameDuration = System.currentTimeMillis() - startTime;
		}
	}			
	
	public static void loadScenarioParameters() {
		scenarioLoaded=true;
		InputStream is = Thread.currentThread().getClass().getResourceAsStream("/data".concat(Integer.toString(level)));
		DataInputStream dis = new DataInputStream(is);				
		try {
			// reset all			
			Navy.reset();			
			Palace.reset();			
			Projectile.reset();
			Airplane.initialize(false);			
			System.gc();
			// Title
			scenarioTitle = dis.readUTF();
			// scenarioScore
			scenarioScore = dis.readShort();
			// background colour
			int scenarioColor = dis.readInt();
			Scenario.setBackground(scenarioColor);			
			// Field
			Terrain.resetField();
			for (int idx=Terrain.fields.length; --idx>=0;) {
				byte type = dis.readByte();
				Terrain.addFieldElement(type);
			}
			// Monsters
			byte monstersOnScenario = dis.readByte();
			Monster.initialize(monstersOnScenario);
			byte totalMonsters = dis.readByte();
			Manager.setMonstersRemains(totalMonsters);
			for (int idx=monstersOnScenario; --idx>=0;)
				Monster.add(Math.abs(random.nextInt() % Scenario.WIDTH), Scenario.HEIGHT+300);						
			
			// Heli position
			byte heliPosx = dis.readByte();
			byte heliPosy = dis.readByte();
			
			Helicopter.setStartPosition(heliPosx, heliPosy);
			Helicopter.regenerate();			
				
			//		 Palaces
			totalPalaces = dis.readByte();
			Palace.initialize(totalPalaces);
			
			if (totalPalaces >0) {
				
				byte buildingType = dis.readByte();															
				Palace.loadImages(buildingType);				
				for (int idx=totalPalaces; --idx>=0;) {
					short pos = dis.readShort();
					short hb  = dis.readShort();
					short vb =  dis.readShort();
					Palace.add(pos, hb, vb);
				}			
			} 
			// Navies			
			totalNavies = dis.readByte();
			Navy.initialize(totalNavies);
			
			if (totalNavies>0) {												
				for (int idx=totalNavies; --idx>=0;) {
					short posX = dis.readShort();					
					Navy.add(posX);
				}				
			}				
			
			if (level>totalScenarios) {
				Helicopter.heli.deactivate();
				Airplane.initialize(true);
				Camera.posX=Scenario.WIDTH / 3;
				Camera.posY=0;
			}
		} catch (IOException ioe) {}
			
	}
	
	private static void initializeScenario() {		
		loadScenarioParameters();		  	
	  gameDuration = 0;	  
	  generalAnimationTick = 2147483647;
	  setPalacesDestroyed(0);
	  setNaviesDestroyed(0);
	}
		
  public static void startNewGame() {
  	initialize();  	
  	level = 1;
  	Manager.setHeliLives(3);  	  	  	  	
  	initializeScenario();
  	setGameState(STARTLEVEL_STATE);  	
  }
  
  
  public static void startDemo() {
  	initialize();
  	Manager.setHeliLives(0);  	
  	level = (byte)((System.currentTimeMillis() % 2)+1);
  	initializeScenario();  	
  	Helicopter.heli.deactivate();
  	setGameState(GAMEOVER_STATE);
  }
  
  private static void initialize() {
  	Camera.posX = 0;
  	Camera.posY = 0;
  	setScorePoints(0);
  	nextScoreBonus = 0;
  	scores = null;
  	newScoreEntry = null;
  	if (eventScheduler!=null) {
  		eventScheduler.cancel();
  		eventScheduler = null;
  	}
  	timer = new Timer();
  	random = new Random();
  	generalAnimationTick = 2147483647;  	
  }
  
    
  public static void draw(Graphics gr) {  	  	
  	switch (gameState) {
  	case PLAYING_STATE:
  	case GAMEOVER_STATE:
  	case SHOWHIGHSCORES_STATE:
  	case NEWHIGHSCORE_STATE:
  	case SCENARIOCOMPLETED_STATE:
  	case GAMECOMPLETED_STATE:
  	// Background  	
  	Scenario.drawBackground(gr);
  	  
  	// Palaces
  	Palace.draw(gr);
  	
  	// Navies
  	Navy.draw(gr);  	  	
  	
	  // Helicopter
	  Helicopter.heli.draw(gr);
  	
  	// Monsters
  	Monster.draw(gr);
  	
  	// Airplane
  	Airplane.airplane.draw(gr);
  	  	
  	// Field
  	Terrain.draw(gr);
  	
  	// Esplosioni
  	Explosion.draw(gr);  	  	  	
  	
  	// Proiettili
  	Projectile.draw(gr);
  	break;
  	}
  	
  	gr.setFont(smallFont);
  	String title;
  	int posX;
  	int posY;
  	  	
  	switch (gameState) {
		case PLAYING_STATE:
			gr.setColor(0x00000000);
			gr.drawString(Integer.toString(monstersRemains), 0, 0, Graphics.TOP|Graphics.LEFT);
			gr.drawString(Integer.toString(heliLives), 40, 0, Graphics.TOP|Graphics.LEFT);
			title = "s:0000"; 
			posX = Camera.WIDTH - smallFont.stringWidth(title);
			title = Integer.toString(scorePoints);
			gr.drawString(title, posX, 0, Graphics.TOP|Graphics.LEFT);
			//gr.drawString(Long.toString((System.currentTimeMillis() - startTime) / 1000), 110, 0, Graphics.TOP|Graphics.LEFT);
			if (Airplane.airplane.isOnScenario()) {			
				if (generalAnimationTick % 2 == 0) {
					title = "a ";
					posX-= smallFont.stringWidth(title);
					gr.drawString(title, posX, 0, Graphics.TOP|Graphics.LEFT);
				}
			}	  				
			break;
		case SCENARIOCOMPLETED_STATE:
			gr.setColor(0x00dddd00);
			// scenario cleared
			title = "SCENARIO CLEARED";
			posX = (Camera.WIDTH - smallFont.stringWidth(title))>>1;
			posY = (Camera.HEIGHT - smallFontHeight)>>1;
			gr.drawString(title, posX, posY, Graphics.TOP|Graphics.LEFT);
			// score + [score]
			title = "score + ".concat(Integer.toString(scenarioScore));			
			posX = (Camera.WIDTH - smallFont.stringWidth(title))>>1;
			gr.drawString(title, posX, posY+smallFontHeight, Graphics.TOP|Graphics.LEFT);
			break;
		case GAMEOVER_STATE:
			gr.setColor(0x00dd0000);
			title = "GAME OVER";
			posX = (Camera.WIDTH - smallFont.stringWidth(title))>>1;
			posY = (Camera.HEIGHT - smallFontHeight)>>1;
			gr.drawString(title, posX, posY, Graphics.TOP|Graphics.LEFT);
			break;  	 
  	case STARTLEVEL_STATE:
  		gr.setColor(0);
  		gr.fillRect(0, 0, Camera.WIDTH, Camera.HEIGHT);
  		gr.setColor(0x00dddd00);
  		title = scenarioTitle.substring(0, scenarioTitle.indexOf(",")+1);
  		posX = (Camera.WIDTH - smallFont.stringWidth(title))>>1;
  		posY = (Camera.HEIGHT)>>1;  		
  		gr.drawString(title, posX, posY, Graphics.TOP|Graphics.LEFT);
  		
  		title = scenarioTitle.substring(scenarioTitle.indexOf(",")+1);
  		posX = (Camera.WIDTH - smallFont.stringWidth(title))>>1;
  		posY+= smallFontHeight;
  		gr.drawString(title, posX, posY, Graphics.TOP|Graphics.LEFT);
  		break;
  	case NEWHIGHSCORE_STATE:
  		// Title String
  		gr.setColor(0x00FF0000);	
  		title = "Congratulations";
  		posX = (Camera.WIDTH - smallFont.stringWidth(title)) >> 1;
  		gr.drawString(title, posX, 0, Graphics.TOP|Graphics.LEFT);
  		title = "Enter your name";
  		posX = (Camera.WIDTH - smallFont.stringWidth(title)) >> 1;
  		posY = smallFontHeight;
  		gr.drawString(title, posX, posY, Graphics.TOP|Graphics.LEFT);
  		
  		// Name 
  		gr.setColor(0);
  		String newEntry = String.valueOf(newScoreEntry.getNameEntry());
  		posX = (Camera.WIDTH-smallFont.stringWidth(newEntry))>>1;
  		posY = (Camera.HEIGHT - smallFontHeight)>>1;
  		gr.drawString(newEntry, posX, posY, Graphics.TOP|Graphics.LEFT);
  		if ((System.currentTimeMillis() % 2000) > 1000) 
  			gr.drawChar('_', posX + newScoreEntry.getCursorCoordX(smallFont), posY , Graphics.TOP|Graphics.LEFT);
  		break;  	
  	case SHOWHIGHSCORES_STATE:
  		// Draw the "High score" string  		
  		title = "High Scores";
      gr.setFont(smallFont);
      gr.setColor(0x0000ff00);
      posX = (Camera.WIDTH - smallFont.stringWidth(title)) >> 1;
      int y0 = smallFontHeight;
      posY = y0;
      gr.drawString(title, posX, posY, Graphics.BOTTOM|Graphics.LEFT);

      gr.setColor(0);
      // Draw the best player names
      posX = (Camera.WIDTH >> 1) - 2;
      for (int i = 0; i < scores.names.length; i++) {
          posY += smallFontHeight;
          gr.drawString(scores.names[i], posX, posY, Graphics.BOTTOM|Graphics.RIGHT);
      }

      // Draw the best scores.
      gr.setColor(0x00FF0000);
      posY = y0;
      posX = (Camera.WIDTH >> 1) + 2;
      char[] scoreString = new char[4];
      for (int i = 0; i < scores.names.length; i++) {
          posY += smallFontHeight;
          Scores.toCharArray(scores.values[i], scoreString);
          gr.drawChars(scoreString, 0, 4, posX, posY, Graphics.BOTTOM|Graphics.LEFT);
      }
  		break;
  	case GAMECOMPLETED_STATE:
  		byte snowSize = 0;
  		gr.setColor(0x00ffffff);
  		
  		for (int idx=snow.length; --idx>=0;) {
  			snowSize = snow[idx][3] < 128 ? (byte)1 : (byte)2;
  			Camera.fillRectOnScreen(snow[idx][0]>>8, snow[idx][1]>>8, snowSize, snowSize, gr);
  		}
  		
  		gr.setColor(0x0000ff00);
  		title = "Congratulations!";  		
  		posX = (Camera.WIDTH - smallFont.stringWidth(title))>>1;
  		posY = smallFontHeight; 		
  		gr.drawString(title, posX, posY, Graphics.TOP|Graphics.LEFT);
  		
  		title = "world has been saved";
  		posX = (Camera.WIDTH - smallFont.stringWidth(title))>>1;
  		posY+=smallFontHeight;  		
  		gr.drawString(title, posX, posY, Graphics.TOP|Graphics.LEFT);
  		
  		title = "from monster's menace!!!";
  		posX = (Camera.WIDTH - smallFont.stringWidth(title))>>1;
  		posY+=smallFontHeight;
  		gr.drawString(title, posX, posY, Graphics.TOP|Graphics.LEFT);
  		  		  		
  		if (generalAnimationTick % 100 < 10) {
  			gr.setColor(0x00dd0000);
  			title = "Press 7";
  			posX = (Camera.WIDTH - smallFont.stringWidth(title))>>1;
  			posY = (Camera.HEIGHT+smallFontHeight)>>1;
  			gr.drawString(title, posX, posY, Graphics.TOP|Graphics.LEFT);  			
  		}
  		break;
  	}
  	
  }
   
    
  public static void manageKeyPressed() {
  	switch (gameState) {
  	case PLAYING_STATE: case SCENARIOCOMPLETED_STATE:  	
			Helicopter.manageAction(lastKeyPressed);
			break;
  	case GAMEOVER_STATE: case SHOWHIGHSCORES_STATE: case GAMECOMPLETED_STATE:	  		
			switch (lastKeyPressed) {
			case (Canvas.DOWN): democamx=0; democamy=-1; break;
			case (Canvas.LEFT): democamx=-1; democamy=0; break;
			case (Canvas.UP): democamx=0; democamy=1; break;
			case (Canvas.RIGHT): democamx=1; democamy=0; break;
			case (Canvas.FIRE): democamx=0; democamy=0; break;
			case Canvas.KEY_NUM7:
				if (gameState == GAMECOMPLETED_STATE) {
					setGameState(GAMEOVER_STATE);
				}
			}
			Camera.moveCamera(Camera.posX + democamx, Camera.posY + democamy);
			setLastKeyPressed(0);
			break;
  	case NEWHIGHSCORE_STATE:
  		switch (lastKeyPressed) {
			case (Canvas.DOWN): newScoreEntry.cursorDown(); break;
			case (Canvas.LEFT): newScoreEntry.cursorLeft(); break;
			case (Canvas.UP): newScoreEntry.cursorUp(); break;
			case (Canvas.RIGHT): newScoreEntry.cursorRight(); break;
			case (Canvas.FIRE):
				scores.addHighScore(scorePoints, String.valueOf(newScoreEntry.getNameEntry()));
				setScorePoints(0);
				setGameState(SHOWHIGHSCORES_STATE);
				break;
			}
  		setLastKeyPressed(0);
  		break;  		
  	case STARTLEVEL_STATE:  		
  		if (lastKeyPressed == Canvas.FIRE) {
  			if (level<=totalScenarios)
  				setGameState(PLAYING_STATE);
  			else  				
  				setGameState(GAMECOMPLETED_STATE);
  		}
  		break;  		
		} 	
			  	
  }
           
  
  public static void checkCollisions() {
  	Helicopter.checkCollisions();
  	Projectile.checkCollisions();
  	Monster.checkCollisions();
  	Airplane.checkCollisions();
  }
  
  private static void checkForNewStates() {  	
		boolean isGameOver = false;
		if (totalPalaces>0) {
			if (totalPalaces == palacesDestroyed)
			isGameOver = true;		
		} else {
			if (naviesDestroyed > 0)
				isGameOver = true;
		}
		if (heliLives<=0)
			isGameOver=true;
		if (isGameOver) {									
			setGameState(GAMEOVER_STATE);			
		} else if (monstersRemains<=0) {
			setGameState(SCENARIOCOMPLETED_STATE);
		}
  }
  
  private static void manageScenarioEvents() {
		Palace.managePalacesEvents();  		  		
		Navy.moveAll();		
  }    
  
  private static void createSnow() {
  	snow = new int[50][4];
		int refsnow[][] = snow;
		final byte x = 0;
  	final byte y = 1;
  	final byte o = 2;
  	final byte s = 3;	  	
  	final int minSpeedY = 32;
  	final int maxSpeedY = 192;
		final int distXbetweenSnow = Scenario.WIDTH / (refsnow.length+1);
		final int startLimitHeight = (Scenario.WIDTH * 12)>>7;
		int posX=0;
		for (int idx=refsnow.length; --idx>=0;) {
			posX+=distXbetweenSnow;
			refsnow[idx][x] = (posX<<8);
			refsnow[idx][y] = ((Scenario.HEIGHT + (random.nextInt() % startLimitHeight))<<8);
			refsnow[idx][o] = 0;
			refsnow[idx][s] = Math.abs(random.nextInt() % (maxSpeedY-minSpeedY)) + minSpeedY;  			  			
		}
  }
  
  private static void animateSnow() {
  	final int snowSpeedX = 64;
  	final byte maxOscillation = 5;
  	final byte x = 0;
  	final byte y = 1;
  	final byte o = 2;
  	final byte s = 3;
  	final int minSpeedY = 32;
  	final int maxSpeedY = 192;
  	final int limitY = (Scenario.WIDTH * 12)>>7;
  	
  	int refsnow[][] = snow;  	  	
  	for (int idx=refsnow.length; --idx>=0;) {
  		if (refsnow[idx][y] > (Terrain.PIECE_HEIGHT<<8)) {  			
	  		if (--refsnow[idx][o] == -maxOscillation) {
	  			refsnow[idx][o] = maxOscillation;
	  		}
	  		refsnow[idx][x] = (refsnow[idx][o]>0) ? refsnow[idx][x]+snowSpeedX : refsnow[idx][x]-snowSpeedX;
	  		refsnow[idx][y]-=refsnow[idx][s];
  		} else {  			
  			refsnow[idx][x] = refsnow[idx][x] - ((++refsnow[idx][o])<<6);
  			refsnow[idx][y] = ((Scenario.HEIGHT + (random.nextInt() % limitY))<<8);
  			refsnow[idx][o] = 0;
  			refsnow[idx][s] = (int)(System.currentTimeMillis() % (maxSpeedY-minSpeedY)) + minSpeedY;  			
  		}
  	}
  }
  
  public static void manageGameAction() {  		  	
  	switch (gameState) {
  	case PLAYING_STATE: 
  	case GAMEOVER_STATE: 
  	case SCENARIOCOMPLETED_STATE:
  	case SHOWHIGHSCORES_STATE:
  	case GAMECOMPLETED_STATE:
  		Projectile.moveAll();			
    	Monster.moveAll();
    	Airplane.airplane.move();
    	Helicopter.heli.move();
  		Explosion.calculate();
  		checkCollisions();    	
  		manageScenarioEvents();  		
  		if (gameState == PLAYING_STATE)
  			checkForNewStates();  		
  		else if (gameState == GAMECOMPLETED_STATE)
  			animateSnow();
  		--generalAnimationTick;
  		break;  	  
  	case STARTLEVEL_STATE:
  		if (!scenarioLoaded) 
  			initializeScenario();  					  	  		  		  		
  		break;
  	}
  	if (gameState == PLAYING_STATE || gameState == SCENARIOCOMPLETED_STATE)
  		Camera.followObjectOnMargin(Helicopter.heli);
  }
    
	public static void setLastKeyPressed(int KeyPressed) {
		lastKeyPressed = KeyPressed;
	}

	public static byte getGameState() {
		return gameState;
	}

	public static void setGameState(byte _gameState) {
		switch (_gameState) {		
		case GAMEOVER_STATE:
			if (scores == null)
				scores = new Scores();
			if (scores.isHighScore(scorePoints)) {
				eventScheduler = new EventScheduler(NEWHIGHSCORE_STATE);
				timer.schedule(eventScheduler, 4000);
			} else {
				eventScheduler = new EventScheduler(SHOWHIGHSCORES_STATE);
				timer.schedule(eventScheduler, 8000);
			}
			break;
		case NEWHIGHSCORE_STATE:
			newScoreEntry = new NewScoreEntry(3);
			break;
		case SHOWHIGHSCORES_STATE:
			eventScheduler = new EventScheduler(GAMEOVER_STATE);
			timer.schedule(eventScheduler, 8000);
			break;
		case STARTLEVEL_STATE:							
			scenarioLoaded = false;			
			if (level<=totalScenarios)
				eventScheduler = new EventScheduler(PLAYING_STATE);
			else
				eventScheduler = new EventScheduler(GAMECOMPLETED_STATE);
			timer.schedule(eventScheduler, 4000);			
			break;
		case SCENARIOCOMPLETED_STATE:						
			setScorePoints((scorePoints+scenarioScore));
			Helicopter.heli.setInvulnerabilityTime(1000);
			level++;						
			eventScheduler = new EventScheduler(STARTLEVEL_STATE);
			timer.schedule(eventScheduler, 5000);			
			break;
		case PLAYING_STATE:			
			if (eventScheduler!=null) {
				eventScheduler.cancel();
				eventScheduler = null;
			}			
			break;
		case GAMECOMPLETED_STATE:
			if (eventScheduler!=null) {
				eventScheduler.cancel();
				eventScheduler = null;
			}			
			createSnow();
			break;			
		}				
		Manager.gameState = _gameState;		
	}		

	public static int getScorePoints() {		
		return scorePoints;
	}

	public static void setScorePoints(int scorePoints) {
		Manager.scorePoints = scorePoints;
		if (Manager.scorePoints >= scoreBonus[nextScoreBonus]) {
			setHeliLives(heliLives+1);
			++nextScoreBonus;
		}
	}

	public static byte getMonstersRemains() {
		return monstersRemains;
	}

	public static void setMonstersRemains(int monstersRemains) {		
		Manager.monstersRemains = (byte)monstersRemains;
	}

	public static byte getHeliLives() {
		return heliLives;
	}

	public static void setHeliLives(int heliLives) {
		if (gameState != SCENARIOCOMPLETED_STATE)
			Manager.heliLives = (byte)heliLives;
	}

	public static byte getPalacesDestroyed() {
		return palacesDestroyed;
	}

	public static void setPalacesDestroyed(int palacesDestroyed) {
		Manager.palacesDestroyed = (byte)palacesDestroyed;
	}

	public static byte getNaviesDestroyed() {
		return naviesDestroyed;
	}

	public static void setNaviesDestroyed(int naviesDestroyed) {	
		Manager.naviesDestroyed = (byte)naviesDestroyed;
	}

	public static boolean rectangleCollision(int ax0, int ay0, int ax1, int ay1, int bx0, int by0, int bx1, int by1) {
	  
		//	 Verifica se il rettangolo 1 si trova a SX o DX del rettangolo 2
		if ( ((ax0+ax1) < bx0) || (ax0 > (bx0+bx1) ) )
			return false;
	
		//		 Verifica se il rettangolo 1 si trova in ALTO o in BASSO del rettangolo 2
		if ( ((ay0+ay1) < by0) || (ay0 > (by0+by1) ) )
			return false;
	
		return true;
	}

	//method needed by lots of classes, shared by putting it here
	public static Image createImage(String filename) {		
    Image image = null;
    try {
        image = Image.createImage(filename);
    }
    catch (java.io.IOException ex) {
        // just let return value be null
    }
    return image;	    
	}

	
}
