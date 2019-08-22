package base;

import javax.microedition.lcdui.Font;


public class NewScoreEntry {
    
    private byte cursorPosition = 0;
    private char[] nameEntry;
    
    /** Creates a new instance of Nick */
    public NewScoreEntry(int nameLen) {
    	initialize(nameLen);
    }
    
    public void initialize(int nameLen) {
    	nameEntry = new char[nameLen];
      for (int idx=nameLen; --idx>=0;) {
          nameEntry[idx] = (char)32;
      }
      cursorPosition = 0;
    }        
    
    public void cursorUp() {
        switch (nameEntry[cursorPosition]) {        
        case 122:
        	nameEntry[cursorPosition] = (char)65;
        	break;
        case 90:
        	nameEntry[cursorPosition] = (char)32;
          break;
        case 32:
        	nameEntry[cursorPosition] = (char)97;
        	break;
        default:
        	nameEntry[cursorPosition] = (char)(nameEntry[cursorPosition]+1);
        	break; 
        }        
    }
    
    public int getCursorCoordX(Font font) {
        int len = 0;        
        for (int idx=0; idx<cursorPosition; idx++) {
        	len += font.charWidth(nameEntry[idx]);
        }
        return len;
    }
    
    
    public void cursorDown() {
      switch (nameEntry[cursorPosition]) {
      case 32:
        nameEntry[cursorPosition] = (char)90;
        break;
      case 65:
        nameEntry[cursorPosition] = (char)122;
        break;
      case 97:
        nameEntry[cursorPosition] = (char)32;
        break;
      default:
        nameEntry[cursorPosition] = (char)(nameEntry[cursorPosition]-1);
        break;                
      }        
    }
    
    public void cursorLeft() {
        if (cursorPosition != 0) cursorPosition--;
    }
    
    public void cursorRight() {
        if (cursorPosition != (nameEntry.length-1)) cursorPosition++;
    }

		public char[] getNameEntry() {
			return nameEntry;
		}

		public byte getCursorPosition() {
			return cursorPosition;
		}
		
    
}
