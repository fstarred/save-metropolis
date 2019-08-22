package base;

import game.Manager;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class SimpleObject {	
	private byte typeId;
	
	public int x;
	public int y;
	public int width;
	public int height;	
	
	public SimpleObject(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;		
	}
	
	public SimpleObject() {
		
	}
			
	public int hcenter() {
		return (x + (width>>1));
	}
	
	public int vcenter() {
		return (y + (height>>1));
	}

	//Le coordinate riguardano l'oggetto
	public boolean isOnScreen() {
		return Camera.isObjectOnScreen(x, y, width, height);
	}
	
	public boolean isOnScenario() {
		return Scenario.isObjectOnScenario(x, y, width, height);
	}
	
	public void locate(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	
	/* Metodo per disegnare gli oggetti in base alla posizione della camera */
  public void drawOnScenario(Image img, Graphics gr) {
  	Camera.drawObjectOnScreen(this.x, this.y, this.height, img, gr);  	
  }
  
  /* Metodo per disegnare gli oggetti in base al frame e alla posizione della camera */
  public void drawOnScenario(Image img, int frame, Graphics gr) {
  	Camera.drawObjectOnScreen(this.x, this.y, this.width, this.height, frame, img, gr);
  }
  
  public boolean collidesWith(SimpleObject simpleObj) {
  	return Manager.rectangleCollision(x, y, width, height, simpleObj.x, simpleObj.y, simpleObj.width, simpleObj.height);
  }

	public byte getTypeId() {
		return typeId;
	}

	public void setTypeId(byte typeId) {
		this.typeId = typeId;
	}

	
}
