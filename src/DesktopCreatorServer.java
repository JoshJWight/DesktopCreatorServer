import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * @author Josh Wight
 *
 */
public class DesktopCreatorServer {

	private static SaveThread saveThread;
	private static LayerThread layerThread;
	private static ArrayList<DownloadThread> downloadThreads;
	private static Monitor m;
	private static WebsocketServerIO server;
	
	public static void main(String args[])
	{
		startThreads();
	}

	private static void startThreads()
	{
		m = new Monitor();
		
		saveThread = new SaveThread(m);
		saveThread.start();
		layerThread = new LayerThread(m);
		layerThread.start();
		
		downloadThreads = new ArrayList<DownloadThread>();
		for(int i=0; i<20; i++)
		{
			DownloadThread d = new DownloadThread(m);
			d.start();
			downloadThreads.add(d);
		}
		
		try {
			server = new WebsocketServerIO(1515);
			server.m = m;
			server.start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
	}
}
