/**
 * SaveThread.java 
 */

/**
 * @author Josh Wight
 *
 */
public class SaveThread extends Thread{

	private Monitor m;
	/**
	 * 
	 */
	public SaveThread(Monitor m) {
		this.m=m;
	}
	
	public void run()
	{	
		int ticker = 0;
		
		while(true){
			long startTime = System.currentTimeMillis();
			
			if(ticker==0){
				m.saveWall();
			}
			else{
				m.saveWall2AndApply();
			}
			
			ticker = (ticker+1) %2;
			
			long diff = System.currentTimeMillis() - startTime;
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
		}
	}

}
