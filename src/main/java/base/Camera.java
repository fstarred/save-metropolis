package base;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import game.Manager;



public class Camera {

	public static int posX = 0;
	public static int posY = 0;
	public static final int WIDTH = 128;
	public static final int HEIGHT = 128;
		
	public static void moveCamera(int newX,int newY) {
		// Limite margine destro e margine sinistro
		if (newX > (Scenario.WIDTH - Camera.WIDTH) )
			newX = Scenario.WIDTH - Camera.WIDTH; 
		else if (newX < 0)
			newX = 0;	
		// Limite margine altro e margine basso
		if (newY > (Scenario.HEIGHT - Camera.HEIGHT))
			newY = Scenario.HEIGHT - Camera.HEIGHT;
		else if (newY < 0)
			newY = 0;
		posX = newX;
		posY = newY;
	}
	
	/*
	 * return true if object inside screen
	 */
	public static boolean isObjectOnScreen(int x, int y, int width, int height) {				
		return Manager.rectangleCollision(posX, posY, WIDTH, HEIGHT, x, y, width, height);		
	}
		
	/*
	 * centerize camera to object center
	 */
	public static void followObjectAtCenter(SimpleObject simpleObj) {
		followCoordAtCenter(simpleObj.x, simpleObj.y, simpleObj.width, simpleObj.height);		
	}
	
	/*
	 * centerize camera to x and y coordinates
	 */
	public static void followCoordAtCenter(int x, int y, int width, int height) {
		int moveToX;
		int moveToY;
				
		moveToX = x - (Camera.WIDTH >> 1) + (width>>1);
		moveToY = y - (Camera.HEIGHT>> 1) + (height>>1);
		moveCamera(moveToX, moveToY);		
	}
	
	/*
	 * camera follows coordinates on margin (useless)
	 */
	public static void followCoordOnMargin(int x, int y, int width, int height) {
		int moveToX = 0;
		int moveToY = 0;
				
		int camera_fragmented = Camera.WIDTH >> 4;
		
		if (x+width > Camera.posX + camera_fragmented * 10)
			moveToX = x + width - ((WIDTH * 10) >> 4);		
		else if (x < Camera.posX + (camera_fragmented * 6))
			moveToX = x - (camera_fragmented * 6);
		else
			moveToX = posX;		
		
		if (y+height > Camera.posY + camera_fragmented*10)
			moveToY = y + height - ((HEIGHT * 10) >> 4);
		else if (y < Camera.posY + (camera_fragmented * 6))
			moveToY = y - (camera_fragmented * 6);
		else
			moveToY = posY;
		
		Camera.moveCamera(moveToX, moveToY);
	
	}
	
	/*
	 * camera follows an object on margin 
	 */
	public static void followObjectOnMargin(MobileObject mobileObj) {
		followCoordOnMargin(mobileObj.x, mobileObj.y,mobileObj.width, mobileObj.height);		
	}

	/* Metodo per disegnare gli oggetti in base al frame e alla posizione della camera */
	public static void drawObjectOnScreen(int x, int y, int width, int height, int curFrame, Image img, Graphics gr) {
		final int clipx = gr.getClipX();
		final int clipy = gr.getClipY();
		final int posOnCameraX = x - posX;
		final int posOnCameraY = posY + HEIGHT - y - height;
		final int tmpClipX = posOnCameraX >= clipx ? posOnCameraX : clipx;		
		final int tmpClipY = posOnCameraY >= clipy ? posOnCameraY : clipy;		
		final int x0 = posOnCameraX + width;
		final int y0 = posOnCameraY + height;
		int clipWidth = x0 <= WIDTH ? width : WIDTH - posOnCameraX;
		if (posOnCameraX < clipx) clipWidth+=posOnCameraX; 
		int clipHeight= y0 <= HEIGHT? height: HEIGHT - posOnCameraY;		
		
		gr.setClip(tmpClipX, tmpClipY, clipWidth, clipHeight);
		gr.drawImage(img, posOnCameraX - (width*curFrame), posOnCameraY, Graphics.TOP|Graphics.LEFT);
		gr.setClip(clipx, clipy, WIDTH, HEIGHT);
	}

	/* Metodo per disegnare gli oggetti in base alla posizione della camera */
	public static void drawObjectOnScreen(int x, int y, int height, Image img, Graphics gr) {
		gr.drawImage(img, x - posX, posY + HEIGHT - y - height, Graphics.TOP|Graphics.LEFT);
	}

	/* Metodo per disegnare un rettangolo in base alla posizione della camera */
	public static void fillRectOnScreen(int x, int y, int width, int height, Graphics gr) {  	
		int posOnCameraX = x - posX;
		int posOnCameraY = posY + HEIGHT - y - height;  	
		gr.fillRect(posOnCameraX, posOnCameraY, width, height);  			  	  	
	}

	/* Metodo per disegnare una linea in base alla posizione della camera */
	public static void drawLineOnScreen(int x, int y, int x1, int y1, Graphics gr) {  	
		int posOnCameraY = posY + HEIGHT;
		gr.drawLine(x - posX, posOnCameraY-y, x1 - posX, posOnCameraY - y1);
	}
	
}
