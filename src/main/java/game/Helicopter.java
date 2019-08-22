package game;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import base.Camera;
import base.Explosion;
import base.MobileObject;
import base.Scenario;

public class Helicopter extends MobileObject {
	public static final byte OBJECT_ID = 1;
	
	private static final byte HELI_WIDTH = 18;
	private static final byte HELI_HEIGHT = 8;				
	
	private boolean hasFired = false;
	private boolean isDestroyed = true;
	private boolean isActive = false;
	
	private static Image image;		
			
	private byte currentSide;
	private byte currentFrame;	
	//	3 movements in 2 cycles = 3<<8 >>1
	private static final int SPEED = 384;	
		
	private boolean changingSide;
	private int changeSideTick = 0;
	private int invulnerabilityTime = 0;
	private int timeRegenerateHeli;
	
	private static byte startPosX;
	private static byte startPosY;
	
	private static final byte sideRight = DIRECTION_RIGHT;
	//private static final byte sideLeft = DIRECTION_LEFT;
	
	private static byte mapFrames[][] = {{6, 5, 4},
										 {0},
										 {1, 2, 3}};
	
	public static Helicopter heli; 
	
	private byte directionX = DIRECTION_NONE;		
	
	public Helicopter(int x, int y) {
		super(x, y, HELI_WIDTH, HELI_HEIGHT);		
		setTypeId(OBJECT_ID);
		isDestroyed = false;						
	}
	
	public static void regenerate() {		
		heli = new Helicopter(startPosX, startPosY);		
		heli.currentSide = sideRight;		
		heli.invulnerabilityTime = 100;
		heli.isActive = true;
		Manager.setLastKeyPressed(0);				
	}	
	
	public static void setStartPosition(byte startPosX, byte startPosY) {
		Helicopter.startPosX = startPosX;
		Helicopter.startPosY = startPosY;		
	}
		
	
	public static void manageAction(int action) {					
			if (!heli.isDestroyed) {				
				boolean isOverGround = heli.y > Terrain.PIECE_HEIGHT ? true : false; 
				switch (action) {
					case 0: break;
					case Canvas.RIGHT:
						if (isOverGround) {
						heli.setVelocity(SPEED, 0);
						heli.directionX=DIRECTION_RIGHT;
						}
						break;
					case Canvas.DOWN:
						if (isOverGround) {
						heli.setVelocity(0, -SPEED);					
						heli.directionX=DIRECTION_NONE;
						}
						break;
					case Canvas.LEFT:
						if (isOverGround) {
						heli.setVelocity(-SPEED, 0);
						heli.directionX=DIRECTION_LEFT;
						}
						break;
					case Canvas.UP:
						heli.setVelocity(0, SPEED);					
						heli.directionX=DIRECTION_NONE;					
						break;
					case Canvas.FIRE:
						heli.setVelocity(0, 0);
						heli.directionX=DIRECTION_NONE;										
						break;
					case Canvas.KEY_NUM7:										
						if (isOverGround) {
						heli.fire();				
						}
						break;
					case Canvas.KEY_STAR:
						if (isOverGround) 
							heli.changingSide = true;					
						break;
				}		
				Manager.setLastKeyPressed(0);
			} 			
	}
	
	public static void loadImages() {
		//	 Se non sono mai state caricate
		if (image == null) {			
			image = Manager.createImage("/b");
		}
	}
	
	public void recharge() {
		hasFired = false;
	}
	
	public void fire() {				
		if (!hasFired) {							
			int pdiry = (-directionX * currentSide);
			hasFired = true;
			Projectile projectile = Projectile.add(x, y+2, OBJECT_ID);			
			projectile.setVelocity(1536*currentSide, 384*pdiry);			
		}
	}
	
	public void destroy() {
		if (!isDestroyed && invulnerabilityTime <= 0) {				
			isDestroyed = true; 						
			setVelocity(0, -512);			
  		Explosion explosion = Explosion.explode(6, hcenter(), vcenter());
  		explosion.setParameters(30, 1, Explosion.DIRECTION_TWICE);  		  		
  		timeRegenerateHeli = 70;
  	}
  			
	}
	
	public boolean isDestroyed() {
		return isDestroyed;
	}
	
	
	public void draw(Graphics gr) {				
			
		if (invulnerabilityTime <= 0 || Manager.generalAnimationTick % 3 == 0) {
			
			drawOnScenario(image, currentFrame, gr);

			if (!isDestroyed) {
				int rotor_width = (Manager.generalAnimationTick % 2 == 0) ? 2 : 6;		
				gr.setColor(0x008c8c8c);		
				int posx = currentSide == sideRight ? 12 : 5 ;
				int posy = -directionX;
				Camera.drawLineOnScreen(x + posx - rotor_width, y+7-posy, + x + posx + rotor_width, y+7+posy, gr);
			}
		}
		
	}	
	
		
	public void move() {
		if (isActive) {
			super.move();		
	
			// Check if is out of scenario borders
			//	 X coords
			if (vx<0) {
				vx = 0;
				x = 0;
			}
			else if ( (vx>>8) + width > Scenario.WIDTH ) {
				vx = (Scenario.WIDTH - width) << 8;
				x = Scenario.WIDTH - width;
			}
				
			//	 controllo coordinate Y
			//	Y coords
			if (vy<0) {
				vy = 0;
				y = 0;
			}
			else if ( (vy>>8) + height > Scenario.HEIGHT) {
				vy = (Scenario.HEIGHT - height) << 8;
				y = (Scenario.HEIGHT - height);
			}
							
			if (!changingSide) {
				currentFrame = mapFrames[currentSide+1][directionX+1];
			}	else {						
				currentFrame = mapFrames[1][0];
				if (++changeSideTick>>2 > 0) {				
					changeSideTick=0;
					currentSide = (byte)-currentSide;				
					changingSide = false;
				}
			}
			
			if (invulnerabilityTime > 0)
				--invulnerabilityTime;						
			
			if (isDestroyed && --timeRegenerateHeli<=0) {				
				byte lives = Manager.getHeliLives();				
				if (lives-->0) {
					Manager.setHeliLives(lives);
					if (lives > 0)
						Helicopter.regenerate();
				}										
			}
		}
	}
	 
	
	public static void checkCollisions() {		
		heli.manageCollisionsWithPalaces();		
		heli.manageCollisionsWithNavies();
		heli.manageCollisionsWithMonsters();
		heli.manageCollisionWithField();	
		heli.manageCollisionWithAirplane();
	}
	
	public void manageCollisionsWithNavies() {
		Navy[] navies = Navy.getNavies();
		for (int idx=navies.length; --idx>=0;) {
			if (navies[idx].collidesWith(heli))
				this.destroy();
		}
	}
	
	public void manageCollisionsWithPalaces() {
		Palace[] palaces = Palace.getPalaces();
		for (int idx=palaces.length; --idx>=0;) {
  		if (this.collidesWith(palaces[idx])) {  			
  			palaces[idx].hitsWith(this, 1);
  			locate((vx+(-velocityX))>>8, (vy+(-velocityY))>>8);
				if (!isDestroyed && palaces[idx].height>0)
					this.destroy();
					this.directionX = DIRECTION_NONE;  		  			
  		}
  	}		
	}
	
	public void manageCollisionsWithMonsters() {
		Monster[] monsters = Monster.getMonsters();
		for (int idx=monsters.length; --idx>=0;) {
  		if (this.collidesWith(monsters[idx])) {
  			this.destroy();
  		}  			
  	}		
	}			
	
	public void manageCollisionWithField() {
		// assuming piece_width is 32
		int fieldX = this.x>>5;
		if (y < Terrain.PIECE_HEIGHT) {			
			this.directionX = DIRECTION_NONE;
			if (Terrain.fields[fieldX] == Terrain.ROAD) {
				locate(x, Terrain.PIECE_HEIGHT);				
			} else {
				this.setVelocity(0, -16);
				this.destroy();
			}			
		} 
	}
		
	public void manageCollisionWithAirplane() {
		if (this.collidesWith(Airplane.airplane)) {
			this.destroy();
			Airplane.airplane.destroy();
		}
	}
	
	public void deactivate() {
		isActive = false;		
		locate(-300, 300);
		isDestroyed = true;
	}

	public void setInvulnerabilityTime(int invulnerabilityTime) {
		this.invulnerabilityTime = invulnerabilityTime;
	}

		
}
