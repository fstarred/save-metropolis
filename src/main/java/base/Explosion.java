package base;

import java.util.Vector;

import javax.microedition.lcdui.Graphics;


public class Explosion {
		
	public static final byte DIRECTION_RIGHT = 1;
	public static final byte DIRECTION_LEFT = -1;
	public static final byte DIRECTION_TWICE = 0;
			
	private final static int minAngle = 3;	// 30� default
	//private final static int MAX_ANGLE = 8; // 80� default
	private int time = 0;
	// interval time between a frame and an other
	private static final int interval = 3;
	// max number of explosions in a while 
	private static byte maxExplosions = 1;
	// limit of frames until explosion disappear
	private final static int cycle_limit = 7 * interval;
	private byte size = 1;	// 1 default
	private int vel = 50; // 50 default
	private int pointsX[];
	private int pointsY[];		
	private byte direction = DIRECTION_TWICE; // default
	private int initialX;
	private int initialY;	
	private int velx[];
	private int vely[];
	private byte angle[];
	private final static int color = 0;
	// multiplied 65563
	private static final int cosAngleX[] = {65540, 61584, 56756, 50203, 42126, 32768, 22415, 11380};	
	//private static final int sinAngleY[] = {11380, 22415, 32768, 42126, 50203, 56756, 61584, 65540};
		
	
	private static Vector vexplosions = new Vector(maxExplosions);
	
	/*
	 *  totpieces, coordX, coordY, velocity
	 */
	private Explosion(int totPieces, int x, int y) {
								
		this.initialX = x;
		this.initialY = y;
		pointsX = new int[totPieces];
		pointsY = new int[totPieces];
		velx = new int[totPieces];
		vely = new int[totPieces];		
		angle = new byte[totPieces]; 
				
		for (int idx=0; idx<totPieces; idx++) {									
			angle[idx] = (byte)((minAngle + idx-1) % cosAngleX.length); 			
		}	
									
		setConstantVel(this);    
	}
	
	// set parameters of last explosion created
	public void setParameters(int vel, int size, byte direction) {		
		this.vel = vel;
		this.size = (byte)size;		
		this.direction = direction;
		// re-inizia
		setConstantVel(this);
	}
	
	private static void setConstantVel(Explosion explosion) {
		for (int idx=0; idx<explosion.pointsX.length; idx++) {
			
			explosion.velx[idx] = cosAngleX[explosion.angle[idx]] * explosion.vel;
			//explosion.vely[idx] = sinAngleY[explosion.angle[idx]] * explosion.vel;
			explosion.vely[idx] = cosAngleX[(cosAngleX.length - 1 - explosion.angle[idx])] * explosion.vel;
		}
	}
	
	public static Explosion explode(int totPoints, int x, int y) {		
		Explosion explosion = new Explosion(totPoints, x, y);
		// if explosions count exceed limit, auto-remove older explosion
		if (vexplosions.size() > maxExplosions) vexplosions.removeElement(vexplosions.firstElement());
		if (maxExplosions > vexplosions.size())
			vexplosions.addElement(explosion);
		return explosion;
	}
	
	private static void calculateCoord(Explosion explosion) {		
		for (int idx=explosion.pointsX.length; --idx>=0;) {			
			explosion.pointsX[idx] = (explosion.velx[idx] * explosion.time)>>16;
			explosion.pointsY[idx] = ((explosion.vely[idx] * explosion.time)>>16) - ((5023 * explosion.time * explosion.time)>>10);
			explosion.pointsX[idx]>>=5;
			explosion.pointsY[idx]>>=5;						
		}
		explosion.time= explosion.time + Explosion.interval;
	}
	
	// calculate of explosions
	public static void calculate() {
		for (int idx=vexplosions.size(); --idx>=0;) {                
      Explosion explosion = (Explosion)vexplosions.elementAt(idx);
      calculateCoord(explosion);      
      if (explosion.time > Explosion.cycle_limit)
      	vexplosions.removeElement(explosion);
  	}
	}
	
	public static void draw(Graphics gr) {		
		for (int vidx=vexplosions.size(); --vidx>=0;) {                
      Explosion explosion = (Explosion)vexplosions.elementAt(vidx);
      for (int idx=explosion.pointsX.length; --idx>=0;) {
      	int x = (idx % 2 == 0 && explosion.direction == DIRECTION_TWICE || explosion.direction == DIRECTION_RIGHT) ? explosion.pointsX[idx] : (-explosion.pointsX[idx]);
      	int finalPosX = x+explosion.initialX;
      	int finalPosY = explosion.initialY + explosion.pointsY[idx];
      	
      	gr.setColor(Explosion.color);
      	Camera.fillRectOnScreen(finalPosX, finalPosY, explosion.size, explosion.size, gr);      	      	
      }
  	}
	}
	
	public static void reset() {
		vexplosions.removeAllElements();
	}

	public static void setMaxExplosions(byte maxExplosions) {
		Explosion.maxExplosions = maxExplosions;		
		vexplosions = new Vector(maxExplosions);
	}

	public static byte getMaxExplosions() {
		return maxExplosions;
	}
	
}
