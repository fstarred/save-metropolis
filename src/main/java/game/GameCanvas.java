package game;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

import base.Camera;


public class GameCanvas extends Canvas implements Runnable {

	private Game midlet;	 
			
	private int width;
	private int height;	
	/*
	private static int clipx;
	private static int clipy;	
	private Image _buffer;	// Per implementare il double-buffering
	*/
	private static final int EXECUTION_TIME = 50;	// in milliseconds	
	
  private volatile Thread animationThread = null;
	
	public GameCanvas(Game midlet) {			
		super();
		
		this.midlet = midlet;
		
		this.width = getWidth();
		this.height = getHeight();				
		/*
		clipx = (width - Camera.WIDTH)>>1;
		clipy = (height - Camera.HEIGHT)>>1;
		
		_buffer = Image.createImage(Camera.WIDTH, Camera.HEIGHT);
		*/
	}
				
	
	protected void keyPressed(int keyCode) {
		int code = keyCode;					
		if (keyCode == Canvas.KEY_POUND) {
			stop();						
			midlet.showMenu();
		} else if (keyCode != KEY_NUM7 && keyCode != KEY_STAR)
			code = getGameAction(keyCode);
		Manager.setLastKeyPressed(code);				
	}
	
	protected void keyRepeated(int keyCode) {
		Manager.setLastKeyPressed(getGameAction(keyCode));		
	}

	protected void paint(Graphics g) {		
		g.setColor(0);
		g.fillRect(0, 0, this.width, this.height);
		
		// reduce clip only to camera size				
		g.translate((width-Camera.WIDTH)>>1, (height-Camera.HEIGHT)>>1);
		g.setClip(0, 0, Camera.WIDTH, Camera.HEIGHT);		
						    
    Manager.draw(g);
	}		
	
	public void start() 
	{  			
		Manager.setRunningStatus(true);
		animationThread = new Thread(this);
		animationThread.start();		
	}
	
	public synchronized void stop()
  {      
		Manager.setRunningStatus(false);		
		animationThread = null;		
  }
			
	public void run() {
		
		Thread currentThread = Thread.currentThread();
		try
    {
    		while (currentThread == animationThread)
        {
    			long startTime = System.currentTimeMillis();    			
            
            /*
            // Don't advance game or draw if canvas is covered by
            // a system screen.
             */ 
            if (isShown())
            {            	
            	Manager.manageKeyPressed();
      				Manager.manageGameAction();
      				repaint();
      				serviceRepaints();
            }
            
            long timeTaken = System.currentTimeMillis() - startTime;
            if (timeTaken < EXECUTION_TIME)
            {
                synchronized (this)
                {                	
                  wait(EXECUTION_TIME - timeTaken);
                }
            } else {
            	Thread.yield();
            }
        }
    }
    catch (InterruptedException e)
    {    	
        // won't be thrown
    }
		
	}

}
