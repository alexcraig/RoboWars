package robowars.robot;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

public class SquareComponent extends JComponent{
	private static final long serialVersionUID = 1L;
	private float r,gr,b=0;
    public void updateColor(int r, int g , int b){
    	this.r=r;
    	this.b=b;
    	this.gr=g;
    	repaint();
    }
	public void paint(Graphics g){
      int height = 200;
      int width = 120;
      g.setColor(new Color((int)r,(int)gr,(int)b));
      g.fillRect(0,0,height,width);
    }
}

