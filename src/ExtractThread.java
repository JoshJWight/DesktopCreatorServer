import java.awt.image.BufferedImage;

public class ExtractThread extends Thread{
	private Monitor m;
	/**
	 * 
	 */
	public ExtractThread(Monitor m) {
		this.m=m;
	}
	
	public void run()
	{
		while(true){
			try{
				BufferedImage img = m.dequeueImage();
				BufferedImage objImg = ObjectExtractor.cutOutObject(img);
				System.out.println("saving " + objImg.hashCode());
				DBSession.session.storeObject(new ImageObject(objImg.hashCode() + "", objImg));
			} catch(Exception e) {
				e.printStackTrace();
			}
			yield();
		}
	}
}
