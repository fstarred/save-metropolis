package game;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import base.MobileObject;
import base.SimpleObject;

public class Navy extends MobileObject {
	
	public final static byte NAVY_WIDTH = 64;
	public final static byte NAVY_HEIGHT = 16;
	public static final byte OBJECT_ID = 5;
	
	public static final int SPEED = 32;
	
	private int damagePointsLeft;
	
	public boolean isDestroyed = false;
	
	private static Image image;	
	
	private static Navy navies[];

	public Navy(int x) {
		super(x, Terrain.PIECE_HEIGHT, NAVY_WIDTH, NAVY_HEIGHT);
		setTypeId(OBJECT_ID);
		setVelocity(-SPEED, 0);
		damagePointsLeft = 20;
	}		
	
	public static void reset() {
		navies = null;
		image = null;
	}
	
	public static void add(int x) {
		int idx=0;
		while (navies[idx] != null) idx++;
		navies[idx] = new Navy(x);	
	}
	
	public static void initialize(int numNavies) {				
		navies = new Navy[numNavies];		
		if (numNavies > 0)
			loadImages();
		else
			image=null;
	}
	
	public static void loadImages() {		
		image = Manager.createImage("/d");
	}
	
	public static void moveAll() {
		for (int idx=navies.length; --idx>=0;) 
			navies[idx].move();
	}
	
	public void destroy() {
		setVelocity(0, -16);
		isDestroyed = true;
		Manager.setNaviesDestroyed(Manager.getNaviesDestroyed()+1);
	}
	
	public void hit(int hit_points) {		
		damagePointsLeft-=hit_points;
		if (damagePointsLeft <=0) {
			destroy();
		}
	}
	
	public static void draw(Graphics gr) {
		for (int idx=navies.length; --idx>=0;)
			navies[idx].drawInstance(gr);		
	}
	
	public void drawInstance(Graphics gr) {				
		if (isOnScreen()) {			
			
			drawOnScenario(image, gr);
		}
	}

	public static Navy[] getNavies() {
		return navies;
	}
	
	public boolean collidesWith(SimpleObject simpleObj) {
		// a little more accurate collision detection
		if (Manager.rectangleCollision(x+21, y, 30, NAVY_HEIGHT, simpleObj.x, simpleObj.y, simpleObj.width, simpleObj.height)) return true;
		if (Manager.rectangleCollision(x, y, NAVY_WIDTH, 10, simpleObj.x, simpleObj.y, simpleObj.width, simpleObj.height)) return true;
		return false;
	}

}
