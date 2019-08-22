package game;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class StartCanvas extends Canvas {

	Game midlet;
	private boolean okToLoad;
	private static Font smallFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
	private static int smallFontHeight = smallFont.getHeight();
	
	public StartCanvas(Game midlet) {
		super();
		this.midlet = midlet;		
	}

	private void dismiss()
  {                	    	
  	midlet.dismiss();    	
  }
	
	protected void paint(Graphics g) {				
		int width = getWidth();
		int height= getHeight();
		g.setColor(0);		
		g.fillRect(0, 0, width, height);
		g.setFont(smallFont);
		if (okToLoad) {			
			g.setColor(0x00dddd00);
			String title = "LOADING..."; 
			g.drawString(title,width - smallFont.stringWidth(title)>>1, height>>1, Graphics.TOP|Graphics.LEFT);
		} else {
			g.setColor(0x00dd0000);
			int posY =  (height-(smallFontHeight<<2))>>1;
			String title = "Save Metropolis";
			g.drawString(title,width - smallFont.stringWidth(title)>>1, posY, Graphics.TOP|Graphics.LEFT);
			title = "XS version";
			posY+=smallFontHeight;
			g.drawString(title,width - smallFont.stringWidth(title)>>1, posY, Graphics.TOP|Graphics.LEFT);
			title = "by Zenon66(c)";
			posY+=smallFontHeight;
			g.drawString(title,width - smallFont.stringWidth(title)>>1, posY, Graphics.TOP|Graphics.LEFT);
			posY+=smallFontHeight;	
			title = "press any key to start";
			g.drawString(title,width - smallFont.stringWidth(title)>>1, posY, Graphics.TOP|Graphics.LEFT);
		}
										
	}
	
	private static void loadImages() {
		Terrain.loadImages();
		Helicopter.loadImages();
		Monster.loadImages();		
		Airplane.loadImages();
		Projectile.loadImages();		
	}			
	
	protected void keyPressed(int arg0) {
		okToLoad = true;	
		repaint();
		serviceRepaints();		
		loadImages();
		dismiss();
	}
}
