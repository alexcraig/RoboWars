package robowars.robot;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.OutputStream;


public class Listener implements KeyListener{
	private OutputStream dataOut;
	public Listener(OutputStream dataOut){
		this.dataOut=dataOut;
	}
	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode()==KeyEvent.VK_UP){
			try {
				System.out.println("Up");
				dataOut.write(1);
				dataOut.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(arg0.getKeyCode()==KeyEvent.VK_DOWN){
			try {
				System.out.println("Down");
				dataOut.write(2);
				dataOut.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(arg0.getKeyCode()==KeyEvent.VK_LEFT){
			try {
				System.out.println("Left");
				dataOut.write(3);
				dataOut.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(arg0.getKeyCode()==KeyEvent.VK_RIGHT){
			try {
				System.out.println("Right");
				dataOut.write(4);
				dataOut.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(arg0.getKeyCode()==KeyEvent.VK_S){
			try {
				System.out.println("Stop");
				dataOut.write(5);
				dataOut.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
