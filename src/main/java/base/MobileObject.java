package base;


public class MobileObject extends SimpleObject {

	
	// virtual velocity
	public int velocityX; 
	public int velocityY; 
	// virtual coordinates
	public int vx;
	public int vy;
	// old positions
	//public int vxold;
	//public int vyold;
		
	public static final byte DIRECTION_NONE = 0;	
	public static final byte DIRECTION_RIGHT = 1;
	public static final byte DIRECTION_LEFT = -1;
	public static final byte DIRECTION_UP = 1;	
	public static final byte DIRECTION_DOWN = -1;
	
	public MobileObject(int x,int y,int width,int height) {
		super(x, y, width, height);
		vx = x << 8;
		vy = y << 8;
	}
	
	public MobileObject() {
	}
		
	
	public void setVelocity(int velocityX, int velocityY) {
		this.velocityX = velocityX;		
		this.velocityY = velocityY;
	}
	
	public void locate(int x, int y) {
		super.locate(x, y);
		//this.vxold = vx;
		//this.vyold = vy;		
		this.vx = x<<8;
		this.vy = y<<8;
	}
		
	public void move() {
		moveX();
		moveY();
	}			
			
	private void moveX() {
		//vxold = vx; 
		vx+=velocityX;						
				
		x= vx >> 8;
	}
	
	private void moveY() {
		//vyold = vy;
		vy+=velocityY;											
		
		y= vy >> 8;
	}
		
		
}
