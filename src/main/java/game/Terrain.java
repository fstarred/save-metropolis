package game;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import base.Camera;
import base.Scenario;

public class Terrain {

	public final static int PIECE_WIDTH = 32;
	public final static int PIECE_HEIGHT = 4;	
	
	private static Image images[];	
	private static byte fieldCounter = 0;
	// contains field type
	public static byte fields[];
	// contains tot_frame for field type
	private static byte TotalsFieldFrame[] = {1,2};
	// contains current frame for field tipe
	private static int currentFieldFrame[] = new int[2];
	// indicates all fields type
	private static final byte fieldsTypes = 2;
		
	public final static byte ROAD = 0;
	public final static byte SEA = 1;		
	
	
	/*
	 *  fields[] per ogni campo tiene il tipo road / sea
	 *  TotalsFieldFrame[] informa i frames per i tipi
	 *  currentFieldFrame[] informa il current frame per ogni field
	 */
	
	public static void resetField() {
		// Assuming field width is 32		
		fields = new byte[(int)(Scenario.WIDTH>>5)];
		fieldCounter = 0;		
	}
	
	public static void addFieldElement(byte type) {		
		fields[fieldCounter++] = type;						
	}
	
	
	public static void loadImages() {
		// Se non sono mai state caricate
		if (images == null) {
			images = new Image[2];
			images[ROAD] = Manager.createImage("/fa");
			images[SEA] = Manager.createImage("/fw");			
		}
	}
	
	public static void animate() {	
		for (int idx=fieldsTypes; --idx>=0;)
			currentFieldFrame[idx] = (Manager.generalAnimationTick >> 3) % (TotalsFieldFrame[idx]);
	}
	
	
	public static void draw(Graphics gr) {		
		if (Camera.posY <= PIECE_HEIGHT) {
			animate();
			int startPos = (Camera.posX >> 5);
			int endPos = (Camera.posX+Camera.WIDTH >> 5);
			if (endPos<fields.length) endPos++;		
			for (int idx=endPos; --idx>=startPos;) {				
				Camera.drawObjectOnScreen(idx*PIECE_WIDTH, 0, PIECE_WIDTH, PIECE_HEIGHT, currentFieldFrame[fields[idx]], images[fields[idx]], gr);								
			}
		}
	}

}
