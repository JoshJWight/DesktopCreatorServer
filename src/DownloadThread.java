import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

/**
 * DownloadThread.java 
 */

/**
 * @author Josh Wight
 *
 */
public class DownloadThread extends Thread{

	private Monitor m;
	
	/**
	 * 
	 */
	public DownloadThread(Monitor m) {
		this.m=m;
	}
	
	public void run(){
		while(true){
			String url = m.nextURL();
			System.out.println("Downloading " + url);
			BufferedImage img;
			try {
				img = downloadImage(url);
				
				
				if(img!=null){
					m.queueImage(img);
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			yield();
		}
	}
	public BufferedImage downloadImage(String imageUrl) throws IOException {
		URL url = new URL(imageUrl);
		
		URLConnection connection = url.openConnection();
		connection.setConnectTimeout(1000);
		connection.connect();
		
		InputStream is = connection.getInputStream();

		BufferedImage img = ImageIO.read(is);

		is.close();
		
		return img;
	}

}
