import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

/**
 * LayerThread.java 
 */

/**
 * @author Josh Wight
 *
 */

//not currently in use
public class LayerThread extends Thread{

	private Monitor m;
	public LayerThread(Monitor m) {
		this.m=m;
	}
	
	public void run()
	{
		
		
		while(true){
			BufferedImage img = m.dequeueImage();
			
			if(img!=null)
			{
				m.applyToWall(img);
			}
			
			yield();	
		}
		
	}
	

}
