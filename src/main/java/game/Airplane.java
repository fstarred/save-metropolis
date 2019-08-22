package game;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import base.Camera;
import base.Explosion;
import base.MobileObject;
import base.Scenario;

public class Airplane extends MobileObject {
	
	public static final byte OBJECT_ID = 6;
		
	private static final int airplane_width = 24;
	private static final int airplane_height = 6;
	public static final int SCORE_POINTS = 20;
	
	private static Image image;
	
	private boolean isDestroyed;
	private boolean hasFired;		
	private boolean friendMode;
	
	private int timeToGenerate;	
	private int targetX;
	private static final int LIMIT_Y = 64;
	
	private static final short SPEED = 384;

	public static Airplane airplane;
	
	public Airplane() {
		super(-50, 0, airplane_width, airplane_height);
		setTypeId(OBJECT_ID);
		//timer = 300;
	}	
	
	public static void loadImages() {
		if (image==null) {
			image = Manager.createImage("/a");
		}
	}
	
	public static void initialize(boolean friendMode) {
		airplane = new Airplane();
		airplane.friendMode = friendMode;
	}
	
	public void generate() {		
		byte directionX = Helicopter.heli.x < (Camera.WIDTH>>1) ? DIRECTION_LEFT : DIRECTION_RIGHT;
		int posX = directionX == DIRECTION_LEFT ? Scenario.WIDTH : -airplane_width;						
		int posY = (int)(System.currentTimeMillis() % (LIMIT_Y - airplane_height)) + Scenario.HEIGHT - LIMIT_Y;		
		locate(posX, posY);		
		setVelocity((SPEED*directionX), 0);		
		targetX = 0;
		hasFired = friendMode;
		isDestroyed = false;				 
	}
	
	
	public void deactivate(int _timer) {
		isDestroyed = true;
		timeToGenerate = _timer;
	}
	
	
	public void draw(Graphics gr) {		
		if (isOnScreen()) {
			int currentFrame = velocityX > 0 ? 1 : 0;
			drawOnScenario(image, currentFrame, gr);
		}
	}
	
	public void move() {	
		super.move();		
		
		// if is destroyed count time until regenerate
			// if no target assigned it's assigned
			// if go out scenario destroy it
			// if not has fired 
				// if is on screen try to fires on heli
				// if not is on screen fires on main scenario's target 
		
		
		if (!isDestroyed) {					
			if (targetX == 0) {
				// Assign target
				Palace palaces[] = Palace.getPalaces();						
				for (int idx=palaces.length; --idx>=0;) {
					if (palaces[idx].vblocks>0) {
						targetX = palaces[idx].x + (int)(System.currentTimeMillis() % palaces[idx].width);
					}
				}
				if (targetX == 0) {
					Navy navies[] = Navy.getNavies();
					for (int idx=navies.length; --idx>=0;) {
						if (navies[idx].x > 20 && navies[idx].x < (Scenario.WIDTH - 20)) {
							targetX = navies[idx].x;
						}
					}					
				}				
				if (targetX==0)
					targetX = 2001; // a fake target
			}
			if (isOnScenario()) {
				if (!hasFired) {					
					// try to hit helicopter
					if (Math.abs(y - Helicopter.heli.y) < 20) {			
						boolean canFire = false;
						byte directionX = DIRECTION_NONE;
						// check if helicopter is ahead airplane
						if (velocityX > 0 && x<Helicopter.heli.x) {
							canFire = true;
							directionX = DIRECTION_RIGHT;
						}	
						else if (velocityX < 0 && Helicopter.heli.x<x ) {
							canFire = true;
							directionX = DIRECTION_LEFT;
						}
						if (canFire) {
							// fire probabilites 25% approx
							if (Manager.random.nextInt() % 50 < -26) {
								fire(directionX, DIRECTION_NONE);
							}
						}
					}	else {
						// hit target (palace or navy)
						if (Math.abs(x - targetX) <= 5) {								
							fire(DIRECTION_NONE, DIRECTION_DOWN);
						}						
					}
				}
			}
			else {
				// is alive but is out of scenario				
				deactivate(300);
			}
		}
		else {
			// is destroyed
			if (--timeToGenerate == 0) {
				generate();
			}
		}		
	}
	
	public boolean isOnScenario() {
		if ( (x+width) < 0 || x >= Scenario.WIDTH )
			return false;
		return true;
	}
	
	public void fire(byte dirX, byte dirY) {
		if (!hasFired) {			
			hasFired = true;
			Projectile projectile = Projectile.add(x, y+2, OBJECT_ID);										
			projectile.setVelocity(1536*dirX, 768*dirY);						
		}
	}
	
	public void destroy() {
		if (!isDestroyed) {
			deactivate(500);
			Explosion explosion = Explosion.explode(6, hcenter(), vcenter());
			explosion.setParameters(25, 1, Explosion.DIRECTION_TWICE);
			setVelocity(velocityX, -256);			
		}
	}
	
	public static void checkCollisions() {
		Airplane.airplane.manageCollisionWithField();
	}
	
	public void manageCollisionWithField() {
		// assuming piece_width is 32					
		if (y < Terrain.PIECE_HEIGHT && isOnScenario()) {			
			int fieldX = this.x>>5;
			if (fieldX<0) fieldX = 0;
			if (Terrain.fields[fieldX] == Terrain.ROAD) {
				locate(x, Terrain.PIECE_HEIGHT);				
				setVelocity(0, 0);
				Explosion explosion = Explosion.explode(6, hcenter(), vcenter());
				explosion.setParameters(30, 1, Explosion.DIRECTION_TWICE);
			} else {					
				setVelocity(0, -16);				
			}				
		} 		
		
	}

	public boolean isDestroyed() {
		return isDestroyed;
	}
		
}
