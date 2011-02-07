package robowars.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class ImageStreamView extends SurfaceView implements SurfaceHolder.Callback {
	private ImageThread drawThread;

	public ImageStreamView(Context context, AttributeSet attrs) {
		super(context, attrs);

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        drawThread = new ImageThread(holder);
	}
	
	public void setImage(Bitmap image) {
		drawThread.setImage(image);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		new Thread(drawThread).start();
		Log.i("ImageStreamTest", "Rendering thread started.");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		drawThread.terminate();
	}
	
	private class ImageThread implements Runnable {
		private SurfaceHolder holder;
		private Bitmap image;
		private Object imageLock;
		private boolean terminateFlag;
		
		public ImageThread(SurfaceHolder holder) {
			this.holder = holder;
			image = null;
			imageLock = new Object();
			terminateFlag = false;
		}
		
		public void setImage(Bitmap image) {
			synchronized(this.imageLock) {
				this.image = image;
			}
			//Log.i("ImageStreamTest", "Image set.");
		}
		
		public void terminate() {
			terminateFlag = true;
		}

		@Override
		public void run() {
			while(!terminateFlag) {
				Canvas drawCanvas = holder.lockCanvas();
				if(drawCanvas == null) continue;
				
				synchronized(this.imageLock) {
					if(image != null) {
						// Scale to full size of canvas for now
						drawCanvas.drawBitmap(image, null, new Rect(0, 0, drawCanvas.getWidth(), drawCanvas.getHeight()),
								null);
						//Log.i("ImageStreamTest", "Drawing image.");
					} else {
						drawCanvas.drawRGB(255, 0, 0);
					}
				}
				holder.unlockCanvasAndPost(drawCanvas);
				
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
}
