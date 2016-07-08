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
public class LayerThread extends Thread{

	private Monitor m;
	/**
	 * 
	 */
	public LayerThread(Monitor m) {
		this.m=m;
	}
	
	public void run()
	{
		
		
		while(true){
			long startTime = System.currentTimeMillis();
			
			long lastTime = startTime;
			
			while(m.hasNextImg())
			{
				BufferedImage img = m.dequeueImage();
				
				if(img!=null)
				{
					m.applyToWall(img);
				}
				
				yield();
			}
			
			long diff = System.currentTimeMillis() - startTime;
			try {
				Thread.sleep(Math.max(1000 - diff, 1));
			} catch (InterruptedException e) {
			}
		}
	}
	

}
