package robowars.test;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class ImageStreamTest extends Activity {
	public static String hostIp = "192.168.1.103";
	public static int hostPort = 33331;
	
	private PrintWriter socketOut;
    private InputStream socketIn;
    private ImageStreamView imageView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        imageView = (ImageStreamView) findViewById(R.id.imageView);
        
        Socket socket;
		try {
			Log.i("ImageStreamTest", "Attempting to open socket.");
			socket = new Socket(hostIp, hostPort);
			Log.i("ImageStreamTest", "Socket open.");
			socketOut = new PrintWriter(socket.getOutputStream());
	        socketIn = new FlushedInputStream(socket.getInputStream());
		} catch (UnknownHostException e) {
			Log.e("ImageStreamTest", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("ImageStreamTest", e.getMessage());
			e.printStackTrace();
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				Bitmap image = null;
				while(true) {
					//Log.i("ImageStreamTest", "Reading socket.");
					image = BitmapFactory.decodeStream(socketIn);
					if(image == null) {
						//Log.i("ImageStreamTest", "Got null decode.");
					} else {
						//Log.i("ImageStreamTest", "Setting new image");
						imageView.setImage(image);
						socketOut.println("ACK");
						socketOut.flush();
					}
					
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		Log.i("ImageStreamTest", "Initialization complete.");
    }
    
    private class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                      int readByte = read();
                      if (readByte < 0) {
                          break;  // we reached EOF
                      } else {
                          bytesSkipped = 1; // we read one byte
                      }
               }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
}