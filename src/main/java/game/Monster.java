package game;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import base.MobileObject;
import base.Scenario;

public class Monster extends MobileObject {		
	public static final byte OBJECT_ID = 2;
	
	public static final int SCORE_POINTS = 10;
	
	private static Image image;
	private final static int monster_width = 12;
	private final static int monster_height = 12;	
		
	private int animationTick = 32767;
	private int eventTick = 0;
	private int currentFrame = 0;	
	private Palace targetPalace = null; 
	private Navy targetNavy = null;
	// coordx & coordy of target
	private int targetX = 0;
	private int targetY = 0;
	private byte targetType = 0;	
	private boolean searchingForTarget = true;
	private boolean hasReachedTarget = false;
	//	possibile actions
	private static final byte moving = 0;
	private static final byte attacking = 1;
	private static final byte falling_dead = 2;
	private static final byte dead_on_field = 3;
	
	private static final byte no_target = 0;
	private static final byte scenario_target = 1;	
	private static final byte heli_target = 2;
	
	private boolean isAlive = true; 
	
	private static Monster monsters[];	
	
	private int currentStatus = moving;
	
	private final static int speed = 512;
	private final static byte mapFrames[][] = {
																							{0, 1},	// MOVING 
																							{4, 5},	// ATTACKING_PALACE
																							{2},		// DEAD_FALLING
																							{3}			// DEAD_ON_FIELD
																					 	};		
	
	public Monster(int x, int y) {
		super(x, y, monster_width, monster_height);		
		setTypeId(OBJECT_ID);								
	}
	
	public static void reset() {
		monsters = null;
		image = null;
	}
	
	public void regenerate() {		
		currentStatus = moving;
		isAlive = true;
		searchingForTarget = true;
		hasReachedTarget = false;
	}

	
	public static void initialize(int numMonsters) {				
		monsters = new Monster[numMonsters];		
	}
	
	public static void add(int x, int y) {
		int idx=0;
		while (monsters[idx] != null) idx++;
		monsters[idx] = new Monster(x, y);
	}
			
	public static void moveAll() {
		for (int idx=monsters.length; --idx>=0;)			
			monsters[idx].moveInstance();
	}		
	
	
	
	
	public void moveInstance() {
		super.move();
		if (isAlive) {
			
			//	ARTIFICIAL INTELLIGENCE SECTION
			// 1st STEP: ASSIGN TARGET
			// 2nd STEP: REACH TARGET
			// 2nd STEP:(bis) PREPARE TO ATTACK OR ASSIGN NEW TARGET
			// 3rd STEP: ATTACK TARGET
			
			if (searchingForTarget) {				
				// assign target				
				searchingForTarget = false;		
				hasReachedTarget = false;
				currentStatus = moving;
				targetPalace = null;				
				targetType = (byte)(Math.abs(Manager.random.nextInt() % 3));
				switch (targetType) {
				case no_target:										
					targetX = (Math.abs(Manager.random.nextInt() % Scenario.WIDTH))<<8;			
					targetY = (Math.abs(Manager.random.nextInt() % (Scenario.HEIGHT>>1))+Terrain.PIECE_HEIGHT)<<8;
					if (Math.abs(vx-targetX) > 12800) targetX = 12800;															
					break;
				case scenario_target:																	
					Palace palaces[] = Palace.getPalaces();
					if (palaces.length>0) {
						int total_palaces = palaces.length;
						targetPalace = 	palaces[(Math.abs(Manager.random.nextInt() % total_palaces))];					
						while (targetPalace.vblocks==0 && --total_palaces>0)
							targetPalace = palaces[total_palaces];					
						if (targetPalace.vblocks > 0) {
							int randombrickX = Math.abs(Manager.random.nextInt() % targetPalace.hblocks);			
							int randombrickY = Math.abs(Manager.random.nextInt() % targetPalace.vblocks);					
							targetX = (targetPalace.x + (Palace.BLOCK_SIZE * randombrickX))<<8;
							targetY = (targetPalace.y + (Palace.BLOCK_SIZE * randombrickY))<<8;
							if (targetPalace.getBlockStatus(randombrickX, randombrickY) == Palace.BLOCK_DESTROYED) {
								searchingForTarget = true;
							}
						} else
							searchingForTarget = true;
					} else {
						Navy navies[] = Navy.getNavies();
						int total_navies = navies.length;
						targetNavy = 	navies[(Math.abs(Manager.random.nextInt() % total_navies))];
						if (targetNavy.x < 200 && targetNavy.x > 0 && !targetNavy.isDestroyed) {
							targetX = (int)(targetNavy.x + (System.currentTimeMillis() % (Navy.NAVY_WIDTH>>1)))<<8;
							targetY = (Terrain.PIECE_HEIGHT+2)<<8;							
						} else
							searchingForTarget = true;
					}
					break;				
				case heli_target:					
					if (!Helicopter.heli.isDestroyed()) {
						targetX = (Helicopter.heli.hcenter())<<8;
						targetY = (Helicopter.heli.vcenter())<<8;
					} else
						searchingForTarget = true;
					break;
				}				
			}				
			else {
				// target assegnato
				if (!hasReachedTarget) {								
					// moves to target									
					int distanceX = (targetX - vx);
					int distanceY = (targetY - vy);				
					if (distanceX >= 0) {
						velocityX = distanceX > speed ? speed : distanceX;					
					} else if (distanceX < 0) {
						velocityX = -distanceX > speed ? -speed : distanceX;					
					} 
					if (distanceY >= 0) {
						velocityY = distanceY > speed ? speed : distanceY;					
					} else if (distanceY < 0) {
						velocityY = -distanceY > speed ? -speed : distanceY;					
					}
					if (distanceX == 0 && distanceY == 0) {
						// target reached
						if (targetType == scenario_target) {
							eventTick = 40;
							currentStatus = attacking;						
						} else {						 
							searchingForTarget = true;						
						}										
						hasReachedTarget = true;
					} 				
				} 
				else {		
					if (targetPalace != null) {
						// TARGET PALACE
						if (--eventTick == 0) {			
							// check if hit hcenter & vcenter og target palace piece
							//if (GeneralLibrary.rectangleCollision(hcenter(), vcenter(), hcenter(), vcenter(), targetPalace.x, targetPalace.y, targetPalace.width, targetPalace.height)) {
							if (vcenter() < targetPalace.height) {
								// check if target palace piece is destroyed 
								if (targetPalace.hitsWith(this, 1)) {
									searchingForTarget = true;								
								}								
							} else							
								searchingForTarget = true;
							eventTick = 40;
						}								
					} else {
						// TARGET NAVY					
						setVelocity(-Navy.SPEED, 0);
						if (collidesWith(targetNavy)) {
							if (--eventTick == 0) {
								eventTick = 40;
								targetNavy.hit(1);
								if (targetNavy.isDestroyed || targetNavy.x < 0) {
									searchingForTarget = true;
								}						
							}					
						} else
							searchingForTarget = true;
					}
				}
			}
		} else {
			// IS DEAD
			if (--eventTick == 0) {
				locate(Manager.random.nextInt() % Scenario.WIDTH, Scenario.HEIGHT+100);
				regenerate();
			}
		}
		
	}
	
	
	
	
	private void animate() {						
		currentFrame = (mapFrames[currentStatus][(animationTick >> 2) % mapFrames[currentStatus].length]);		
		if (--animationTick == 0) animationTick = 32767;
	}
	
	public void die() {
		if (isAlive) {
			isAlive = false;
			eventTick = 128;
			setVelocity(0, -768);
			currentStatus = falling_dead;		
		}
	}
	
	public static void loadImages() {
		//	 Se non sono mai state caricate
		if (image == null) {			
			image = Manager.createImage("/c");
		}
	}		
	
	public static void draw(Graphics gr) {
		for (int idx=monsters.length; --idx>=0;)
			monsters[idx].drawInstance(gr);		
	}
	
	public void drawInstance(Graphics gr) {				
		if (isOnScreen()) {			
			// not important to animate it if is out of screen
			animate();
			
			drawOnScenario(image, currentFrame, gr);
		}
	}


	public static Monster[] getMonsters() {
		return monsters;
	}
	
	public static void checkCollisions() {	
		for (int idx=monsters.length; --idx>=0;) {
			monsters[idx].manageCollisionWithField();
			monsters[idx].manageCollisionWithAirplane();
		}
	}
	
	public void manageCollisionWithField() {
		// assuming piece_width is 32					
		if (y < Terrain.PIECE_HEIGHT && isOnScenario()) {			
			int fieldX = this.x>>5;
			if (fieldX<0) fieldX = 0;
			if (Terrain.fields[fieldX] == Terrain.ROAD) {
				locate(x, Terrain.PIECE_HEIGHT);				
			} else {
				this.die();
				setVelocity(0, -16);				
			}
			if (!isAlive) {
				currentStatus = dead_on_field;				
			}
		} 		
	}
	
	public void manageCollisionWithAirplane() {
		if (collidesWith(Airplane.airplane)) {
			this.die();
			Airplane.airplane.destroy();
		}
	}
	
	public boolean isOnScenario() {
		if ( (x+width) < 0 || x >= Scenario.WIDTH )
			return false;
		return true;
	}

	public boolean isAlive() {
		return isAlive;
	}
	
}
