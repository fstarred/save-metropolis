package game;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import base.Camera;
import base.Explosion;
import base.SimpleObject;

public class Palace extends SimpleObject {		
	public static final byte OBJECT_ID = 4;
		
	public int hblocks;
	public int vblocks;	
	
	private static Image images[];
	
	/*		BRICK PARTS		*/
	public final static byte BLOCK_SIZE = 12;
		
	public final static int BLOCK_OKAY = 0;
	public final static int BLOCK_DAMAGED = 1;
	public final static int BLOCK_VERY_DAMAGED = 2;
	public final static int BLOCK_DESTROYED = 3;			
	
	// single brick status
	private byte block_status[][];
	
	private static Palace[] palaces;
	
	/*////////////////  */
	
	/* types */		
	public final static byte LOW_BUILDING = 0;
	public final static byte MEDIUM_BUILDING = 1;
	public final static byte HIGH_BUILDING = 2;
	
	private int collapseToHeight;
			
	public Palace(int x, int width, int height) {
		super(x, Terrain.PIECE_HEIGHT, width, height);
				
		setTypeId(OBJECT_ID);
		
		this.hblocks = width / BLOCK_SIZE;
		this.vblocks = height / BLOCK_SIZE;								
		
		block_status = new byte[hblocks][vblocks];
		
		collapseToHeight = height;
		
		// Inizializza i mattoni a stato ok
		for (int vidx=vblocks; --vidx>=0;) {
			for (int hidx=hblocks; --hidx>=0;) {
				block_status[hidx][vidx] = BLOCK_OKAY;
			}									
		}										
	}		
		
	public static void initialize(int numPalaces) {								
		palaces = new Palace[numPalaces];				
	}	
		
	public static void add(int x, int hblocks, int vblocks) {
		int idx=0;
		while (palaces[idx] != null) idx++;
		palaces[idx] = new Palace(x, hblocks*BLOCK_SIZE, vblocks*BLOCK_SIZE);
	}
		
	
	public static void loadImages(byte buildingType) {							
		images = new Image[4];
		String titlesImg=null;
		switch (buildingType) {
		case HIGH_BUILDING:
			titlesImg = "/hb_";
			break;
		case MEDIUM_BUILDING:
			titlesImg = "/mb_";
			break;
		case LOW_BUILDING:
			titlesImg = "/lb_";
			break;
		}			
		for (int idx=0; idx<images.length; idx++) {
			images[idx] = Manager.createImage(titlesImg.concat(Integer.toString(idx+1)));
		}		
	}
	
	public static void reset() {
		palaces = null;
		images = null;
	}
	
		
	public boolean hitsWith(SimpleObject simpleObj, int damage) {
		
		int tmpX = simpleObj.hcenter();		
		int tmpY = simpleObj.vcenter();
		int pieceh;
		
		if (tmpX >= x && tmpX <= (x+width)) {
			pieceh = (tmpX-x-1) / BLOCK_SIZE;
		} else {
			tmpX = simpleObj.x;
			pieceh = (tmpX >= this.x) ? (tmpX-x-1) / BLOCK_SIZE : (tmpX + simpleObj.width - x) / BLOCK_SIZE;
		}
		int piecev = (tmpY < (this.y+this.height)) ? (tmpY-y) / BLOCK_SIZE : (simpleObj.y-y-1) / BLOCK_SIZE; 
				
		int refBlock = block_status[pieceh][piecev];		
		// already destroyed return because not important to see if collapse
		if ((refBlock+1)>BLOCK_DESTROYED) return true;
						
		refBlock+=damage;
		block_status[pieceh][piecev] = (byte)refBlock; 
			
		if (refBlock >= BLOCK_DESTROYED) {						
			block_status[pieceh][piecev] = BLOCK_DESTROYED;
			
			checkPossibleCollapse();			
			return true;
		}
		
		return false;						
		
	}
	
	private void checkPossibleCollapse() {		
		// if already collapsing, don't collapse in a higher y
		int tot_pix_destroyed = 0;
		int possible_collapse;
		boolean requestedCollapse = false;
		int perc_to_destroy = ((hblocks<<8) * 5) >> 3;
		byte refBlocks[][] = block_status;
		int vidx = -1;
		while (!requestedCollapse && ++vidx<vblocks) {			
			tot_pix_destroyed = 0;
			for (int hidx=hblocks; --hidx>=0;) {
				if (refBlocks[hidx][vidx] >= BLOCK_DESTROYED) tot_pix_destroyed++;
			}			
			if ((tot_pix_destroyed<<8) >= perc_to_destroy) {				
				requestedCollapse = true;					
			}
		}		
		if (requestedCollapse) {
			tot_pix_destroyed = 0;
			possible_collapse = vidx * BLOCK_SIZE;
			perc_to_destroy = (((vblocks - 1 - vidx) * hblocks)<<8)>>1;
			for (; vidx<vblocks; vidx++) {
				for (int hidx=hblocks; --hidx>=0;) {
					if (refBlocks[hidx][vidx] >= BLOCK_DESTROYED) tot_pix_destroyed++;
				}
			}
			if ((tot_pix_destroyed<<8) >= perc_to_destroy) {								
				collapseToHeight = possible_collapse;							
			}
		}
	}
	
	public static void managePalacesEvents() {		
  	for (int idx=palaces.length; --idx>=0;) {  		
  		palaces[idx].manageCollapse();  		
  	}  	
  }    
		
		
	public void manageCollapse() {
		if (height > collapseToHeight) {
			if (--height % BLOCK_SIZE == 0) {
				vblocks--;
				Explosion explosion = Explosion.explode(6, x+((hblocks*BLOCK_SIZE)>>1), height);
				explosion.setParameters(20, 2, Explosion.DIRECTION_TWICE);
								
				if (height == collapseToHeight) {					
					if (vblocks<=0)
						Manager.setPalacesDestroyed(Manager.getPalacesDestroyed()+1);
					/*
					if (vblocks <= 0) {
						boolean allPalacesRaised = true;
						for (int idx=palaces.length; --idx>=0;) {
							if (palaces[idx].vblocks > 0) allPalacesRaised = false;
						}
						if (allPalacesRaised)
							Manager.setGameState(Manager.GAMEOVER_STATE);
					}	
					*/				
				}
				
			}								
		}
	}
		
	public static void draw(Graphics gr) {
		for (int idx=palaces.length; --idx>=0;) {
			palaces[idx].drawInstance(gr);
		}
	}
	
	public void drawInstance(Graphics gr) {		
		if (isOnScreen() && vblocks>0) {			
			byte refBlock[][] = block_status;
			int vidx = vblocks-1;
			for (int hidx=hblocks; --hidx>=0;) {				
				Camera.drawObjectOnScreen(x+(hidx*BLOCK_SIZE), y+(vidx*BLOCK_SIZE)+((height-1) % BLOCK_SIZE)-BLOCK_SIZE+1, BLOCK_SIZE, images[refBlock[hidx][vidx]],gr);
			}
						
			// inside some controls to avoid graphics operation out of screen!
			for (; --vidx>=0;) {
				if (y+((vidx+1)*BLOCK_SIZE) > Camera.posY)
					for (int hidx=hblocks; --hidx>=0;) {
						int posh = x+(hidx*BLOCK_SIZE); 
						if (posh < (Camera.posX+Camera.WIDTH) && posh+BLOCK_SIZE> Camera.posX) {							
							Camera.drawObjectOnScreen(x+(hidx*BLOCK_SIZE), y+(vidx*BLOCK_SIZE), BLOCK_SIZE, images[refBlock[hidx][vidx]],gr);
						}
					}			
			}
		}		
	}

	public static Palace[] getPalaces() {
		return palaces;
	}
	
	public byte getBlockStatus(int _x, int _y) {
		return block_status[_x][_y];
	}

}
