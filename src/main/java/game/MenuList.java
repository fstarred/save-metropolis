package game;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;


class MenuList extends List implements CommandListener {        
    
	private Game midlet;    
	private boolean gameActive = false;
    
    MenuList(Game midlet)
    {
        super("Save Metropolis", List.IMPLICIT);
        this.midlet = midlet;
        
        /* Menu list */
        append("New Game", null);
        append("Demo", null);
        append("Options", null);
        append("Instructions", null);
        append("About", null);
        append("Exit", null);
        
        /* Preparazione alla ricezione */
        setCommandListener(this);
    }    

    void setGameActive(boolean active)
    {
        if (active && !gameActive)
        {
            gameActive = true;
            insert(0, "Continue", null);
        }
        else if (!active && gameActive)
        {
            gameActive = false;
            delete(0);
        }
    }

    public void commandAction(Command cmd, Displayable dpl) {
    	if (cmd == List.SELECT_COMMAND) {
    		int index = getSelectedIndex();
        if (index != -1)  // should never be -1
        {
            if (!gameActive)
            {
                index++;
            }
    		switch (index) {
    			case 0: 
    				midlet.continueGame();
    				break;
    			/* New Game */    		
    			case 1:    				
    				midlet.startGame();
    				break;
    			/* Demo */
    			case 2:
    				midlet.startDemo();
    				break;
    			/* Instructions */
    			case 3:
    				midlet.showOptions();
    				break;
    				/* Option */
    			case 4:
    				midlet.showInstructions();
    				break;
    			/* About */
    			case 5:
    				midlet.showAbout();
    				break;
    			/* Exit */    				
    			case 6:
    				midlet.notifyDestroyed();
    				break;
    		}
        }
    	}
    }
}
