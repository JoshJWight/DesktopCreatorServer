import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * Monitor.java 
 */

/**
 * @author Josh Wight
 *
 */
public class Monitor {

	public static final float WEIGHT = 0.2f;
	
	public static final int MAIN_HEIGHT = 1080;
	public static final int MAIN_WIDTH = 1920;
	
	public static final int QUEUE_SIZE = 10;
	public static final int URL_QUEUE_SIZE = 1000000;
	
	public static final String PATH_TO_IMAGES = "image/";
	
	private BufferedImage wall;
	
	private int driveBatchSize = 0;
	
	private LinkedList<BufferedImage> imgQueue;
	private LinkedList<String> urlQueue;
	
	private long lastLayerTime;
	
	/**
	 * 
	 */
	public Monitor() {
		wall = readImage("wallpaper.png");
		
		if(wall==null)
		{
			wall = readImage("wallpaper2.png");
			if(wall==null)
			{
				wall = readImage("default.png");
				if(wall==null)
				{
					System.err.println("ALL WALLPAPER IMAGES CORRUPTED!");
				}
			}
		}
		
		imgQueue = new LinkedList<BufferedImage>();
		urlQueue = new LinkedList<String>();
		
	}
	
	public BufferedImage getWallImg() {
		synchronized(wall) {
			BufferedImage b = new BufferedImage(wall.getWidth(), wall.getHeight(), wall.getType());
		    Graphics g = b.getGraphics();
		    g.drawImage(wall, 0, 0, null);
		    g.dispose();
		    return b;
		}
	}
	
	public long getLastLayerTime()
	{
		return lastLayerTime;
	}
	
	public void queueImage(BufferedImage img){
		synchronized(imgQueue){
		while(imgQueue.size()>=QUEUE_SIZE)
		{
			try {
				imgQueue.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		imgQueue.addLast(img);
		imgQueue.notifyAll();
		}
	}
	
	public BufferedImage dequeueImage()
	{
		synchronized(imgQueue){
		while(imgQueue.size()==0)
		{
			try {
				imgQueue.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		}
		BufferedImage img = imgQueue.removeFirst();
		imgQueue.notifyAll();
		return img;
		}
	}
	
	public void saveWall()
	{
		synchronized(wall){
			writeImage(wall, "wallpaper.png");
		}
	}
	public void saveWall2AndApply()
	{
		synchronized(wall){
			writeImage(wall, "wallpaper2.png");
			writeImage(wall, PATH_TO_IMAGES + "wallpaper.png");
		}
	}
	
	public void writeImage(BufferedImage img, String path)
	{
		if(img==null)
		{
			System.err.println("tried to write null image");
			return;
		}
		File f = new File(path);
		try {			
			ImageIO.write(img, "png", f);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int[][] getRGBArray(BufferedImage img)
	{
		int arr[][] = new int[img.getHeight()][img.getWidth()];
		
		for(int x=0; x<img.getWidth(); x++)
		{
			for(int y=0; y<img.getHeight(); y++)
			{
				arr[y][x] = img.getRGB(x, y);
			}
		}
		
		return arr;
	}
	
	public void queueURL(String url) {
		synchronized(urlQueue) {
			//just pitch urls if we're over the limit.
			if(urlQueue.size()<=URL_QUEUE_SIZE){
				urlQueue.addLast(url);
				urlQueue.notifyAll();
			}
		}
	}
	
	public String nextURL()
	{
		synchronized(urlQueue){
			while(urlQueue.size()==0)
			{
				try {
					urlQueue.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					return null;
				}
			}
			String url = urlQueue.removeFirst();
			urlQueue.notifyAll();
			return url;
			
		}
	}
	public BufferedImage readImage(String path)
	{
		File f = new File(path);
		try {
			BufferedImage img = ImageIO.read(f);
			return img;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void applyToWall(BufferedImage img){
		synchronized(wall){
			lastLayerTime = System.currentTimeMillis();
			applyImage(wall, img, WEIGHT);
		}
		
	}
	
	public void applyImage(BufferedImage original, BufferedImage img, float weight)
	{
		
		Random rand = new Random();
		
		int xOffset = rand.nextInt(Math.max(1, original.getWidth() - img.getWidth()));
		int yOffset = rand.nextInt(Math.max(1, original.getHeight() - img.getHeight()));
		
		for(int x=0; x<img.getWidth(); x++)
		{
			for(int y=0; y<img.getHeight(); y++)
			{
				if(y >=original.getHeight() || x >=original.getWidth())
				{
					continue;
				}
				
				int pixel1 = original.getRGB(xOffset + x, yOffset + y);
				int pixel2 = img.getRGB(x, y);
				
				int result = weightRGB(pixel1, pixel2, weight);
				original.setRGB(xOffset + x, yOffset + y, result);
			}
		}
		
	}
	public int weightRGB(int rgb1, int rgb2, float weight)
	{
		Color c1 = new Color(rgb1);
		Color c2 = new Color(rgb2);
		
		int red = (int)((float)(c1.getRed()) + ((float)(c2.getRed()) - (float)(c1.getRed())) * weight);
		int green = (int)((float)(c1.getGreen()) + ((float)(c2.getGreen()) - (float)(c1.getGreen())) * weight);
		int blue = (int)((float)(c1.getBlue()) + ((float)(c2.getBlue()) - (float)(c1.getBlue())) * weight);
		
		Color c3 = new Color(red, green, blue);
		
		return c3.getRGB();
	}
	
}
