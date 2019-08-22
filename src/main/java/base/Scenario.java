package base;

import game.Manager;

import javax.microedition.lcdui.Graphics;




public class Scenario {

	public final static int WIDTH = 256;
	public final static int HEIGHT = 192;
	
	private static int color;
	
	//private static Image background;
	
	public static void setBackground(int _color) {
		//background = GeneralLibrary.createImage(img);
		color = _color;
	}
	
	public static boolean isObjectOnScenario(int x, int y, int width, int height) {				
		return Manager.rectangleCollision(0, 0, WIDTH, HEIGHT, x, y, width, height);		
	}
	
	
	public static void drawBackground(Graphics gr) {
		//if (background != null) {
			
			//gr.drawImage(background, 0, Camera.Y + Camera.HEIGHT - HEIGHT, Graphics.TOP|Graphics.LEFT);			
		//}
		gr.setColor(color);
		gr.fillRect(0, 0, Camera.WIDTH, Camera.HEIGHT);
	}
	
}
