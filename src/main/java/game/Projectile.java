package game;

import java.util.Vector;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import base.Explosion;
import base.MobileObject;

public class Projectile extends MobileObject {
	public static final byte OBJECT_ID = 3;
	
	private static final int projectile_width = 6;
	private static final int projectile_height = 3;
	
	private static final byte maxProjectiles = 2;
	
	private byte sourceID;
	private static Image image;	
	private static Vector vprojectiles = new Vector(maxProjectiles);	
	
	public Projectile(int x, int y) {
		super(x, y, projectile_width, projectile_height);
		setTypeId(OBJECT_ID);				
	}
	
	public static void loadImages() {
		if (image==null) {
			image = Manager.createImage("/e");
		}
	}
	
	public static Projectile add(int x, int y, byte sourceID) {
		Projectile projectile = new Projectile(x, y);
		projectile.sourceID = sourceID;
		vprojectiles.addElement(projectile);		
		return projectile;
	}
		
	public static void reset() {		
		vprojectiles.removeAllElements();
	}
		
	public static void moveAll() {
		for (int idx=vprojectiles.size(); --idx>=0;) {
			((Projectile)vprojectiles.elementAt(idx)).move();
		}
	}
	
	public void remove() {
		vprojectiles.removeElement(this);
		if (sourceID == Helicopter.OBJECT_ID ) {
			Helicopter.heli.recharge();
		}		
		if (isOnScreen()) {
			Explosion explosion = Explosion.explode(6, this.hcenter(), this.vcenter());
			explosion.setParameters(50, 1, this.velocityX>0 ? DIRECTION_RIGHT : DIRECTION_LEFT);
		}
	}
	
	public void move() {
		super.move();
		switch (sourceID) {
		case Helicopter.OBJECT_ID:
			if (!isOnScreen())
				remove();
			break;
		case Airplane.OBJECT_ID:
			if (!isOnScenario())
				remove();
			break;
		}		
	}
	
	public static void draw(Graphics gr) {
		for (int idx=vprojectiles.size(); --idx>=0;) {
			((Projectile)vprojectiles.elementAt(idx)).drawInstance(gr);
		}
	}
		
	public void drawInstance(Graphics gr) {		
  	int currentFrame = velocityX > 0 ? 1 : 0;
  	//if (isOnScreen()) {
  		drawOnScenario(image, currentFrame, gr);
  	//}
	}

	public static Vector getProjectiles() {
		return vprojectiles;
	}
	
	public static void checkCollisions() {				
		for (int idx=vprojectiles.size(); --idx>=0;) {
			Projectile projectile = (Projectile)vprojectiles.elementAt(idx);			
			projectile.manageCollisionsWithPalaces();			
			projectile.manageCollisionsWithNavies();
			
			projectile.manageCollisionsWithMonsters();			
			projectile.manageCollisionsWithHelicopter();
			projectile.manageCollisionsWithAirplane();
			projectile.manageCollisionWithField();
		}
	}
	
	public void manageCollisionsWithNavies() {
		Navy navies[] = Navy.getNavies();
		for (int idx=navies.length; --idx>=0;) {
			if (collidesWith(navies[idx])) {
				if (sourceID != Helicopter.OBJECT_ID) {
					navies[idx].hit(2);
					remove();
				}
			}
		}
	}
	
	public void manageCollisionsWithHelicopter() {
		if (collidesWith(Helicopter.heli)) {
			if (sourceID != Helicopter.OBJECT_ID) {
				Helicopter.heli.destroy();
				remove();
			}
		}
	}
	
	public void manageCollisionsWithAirplane() {
		if (collidesWith(Airplane.airplane)) {
			if (sourceID != Airplane.OBJECT_ID) {
				if (!Airplane.airplane.isDestroyed()) {
					Manager.setScorePoints(Manager.getScorePoints()+Airplane.SCORE_POINTS);
				} else {
					Manager.setScorePoints(Manager.getScorePoints()+(Airplane.SCORE_POINTS>>1));
				}
					
				Airplane.airplane.destroy();
				remove();				
			}
		}
	}
	
	public void manageCollisionsWithPalaces() {
		Palace palaces[] = Palace.getPalaces();
		for (int idx=palaces.length; --idx>=0;) {
  		if (this.collidesWith(palaces[idx])) {  		
  			if (sourceID == Helicopter.OBJECT_ID) {
  				palaces[idx].hitsWith(this, 1);  				
  			}
  			else {
  				palaces[idx].hitsWith(this, 3);  				
  			}
  			remove();
  		}  			
  	}		
	}	
	
	public void manageCollisionsWithMonsters() {
		Monster monsters[] = Monster.getMonsters();
		for (int idx=monsters.length; --idx>=0;) {
			if (this.collidesWith(monsters[idx])) {
				if (this.sourceID == Helicopter.OBJECT_ID) {
					if (monsters[idx].isAlive()) {
						Manager.setScorePoints(Manager.getScorePoints()+Monster.SCORE_POINTS);
						Manager.setMonstersRemains(Manager.getMonstersRemains()-1);
					} else {
						Manager.setScorePoints(Manager.getScorePoints()+(Monster.SCORE_POINTS>>1));
					}
				}
				monsters[idx].die();												
				remove();
			}
		}
	}	
	
	public void manageCollisionWithField() {		
		if (y < Terrain.PIECE_HEIGHT) {						
			remove();
		} 		
		
	}
		
}
