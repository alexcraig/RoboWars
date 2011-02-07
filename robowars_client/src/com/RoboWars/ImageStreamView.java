package com.RoboWars;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * A view for displaying a bitmap image updated in real time.
 */
class ImageStreamView extends SurfaceView implements SurfaceHolder.Callback {
	
	/** The rendering thread which updates the canvas */
	private BitmapRenderThread drawThread;

	/** 
	 * Generates a new ImageStreamView, and registers itself as a callback
	 * handler for surface creation and destruction events.
	 * @param context
	 * @param attrs
	 */
	public ImageStreamView(Context context, AttributeSet attrs) {
		super(context, attrs);

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        drawThread = null;
	}
	
	/**
	 * Sets the image to display on the view. No action is performed
	 * if the surface is not currently available (i.e. if a BitmapRenderThread
	 * is not currently running)
	 * @param image	The new image to display in the view
	 */
	public void setImage(Bitmap image) {
		if(drawThread != null) {
			drawThread.setImage(image);
		}
	}

	@Override
	/**
	 * Call back for surface resizing events.
	 * TODO: This could be used to dynamically resize the displayed video while
	 * maintaining aspect ratio.
	 */
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	/**
	 * Call back for surface creation event.
	 * Generates a new rendering thread, and starts the thread.
	 */
	public void surfaceCreated(SurfaceHolder arg0) {
		drawThread = new BitmapRenderThread(this.getHolder());
		new Thread(drawThread).start();
	}

	@Override
	/**
	 * Call back for surface destruction event.
	 * Terminates the rendering thread.
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		drawThread.terminate();
		drawThread = null;
	}
	
	/**
	 * Rendering thread to continually draw a provided bitmap onto the view's
	 * canvas.
	 */
	private class BitmapRenderThread implements Runnable {
		/** The holder for the surface (used to fetch and lock drawing canvas) */
		private SurfaceHolder holder;
		
		/** The bitmap image to be drawn to the view */
		private Bitmap image;
		
		/** Mutex to ensure image is not modified while rendering thread is using it */
		private Object imageLock;
		
		/** Flag to trigger termination of the rendering thread */
		private boolean terminateFlag;
		
		/**
		 * Generates a new BitmapRenderThread
		 * @param holder	The holder a drawing canvas should be fetched from.
		 */
		public BitmapRenderThread(SurfaceHolder holder) {
			this.holder = holder;
			image = null;
			imageLock = new Object();
			terminateFlag = false;
		}
		
		/**
		 * Sets the bitmap image for the thread to render.
		 * @param image	The new image to render.
		 */
		public void setImage(Bitmap image) {
			synchronized(this.imageLock) {
				this.image = image;
			}
		}
		
		/**
		 * Sets the termination flag of the rendering thread (the thread will
		 * terminate once any current rendering operation is complete)
		 */
		public void terminate() {
			terminateFlag = true;
		}

		@Override
		/**
		 * Continually draws the provided bitmap onto the view's canvas
		 */
		public void run() {
			while(!terminateFlag) {
				Canvas drawCanvas = holder.lockCanvas();
				if(drawCanvas == null) continue;
				
				synchronized(this.imageLock) {
					if(image != null) {
						// Scale to full size of canvas for now
						drawCanvas.drawBitmap(image, null, new Rect(0, 0, drawCanvas.getWidth(), drawCanvas.getHeight()),
								null);
					} else {
						// Just draw a red background if no video is available
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
