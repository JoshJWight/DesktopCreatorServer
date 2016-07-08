import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;

public class ServerIO implements Runnable{
	private Monitor m;
	private ServerSocket socket;
	
	private static final int PORT = 1515;
	
	public ServerIO(Monitor m) {
		this.m = m;
		
	}
	
	public void run() {
		try {
			socket = new ServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		System.out.println("Server started.");
		System.out.println("Inet address: " + socket.getInetAddress());
		System.out.println("Port: " + socket.getLocalPort());
		
		while(true) {
			try {
				Socket s = socket.accept();
				System.out.println("Received connection");
				(new ServerThread(s)).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public class ServerThread extends Thread {
		private Socket client;
		private BufferedReader in;
		private OutputStream out;
		
		private boolean error = false;
		
		private static final String IMG_COMMAND = "img";
		
		public ServerThread(Socket c) {
			client = c;
			try {
				in = new BufferedReader(new InputStreamReader(c.getInputStream()));
				out = c.getOutputStream();
			} catch (IOException e) {
				error = true;
				e.printStackTrace();
			}
		}
		
		public void run() {
			if(!error) {
				try {
					String line = in.readLine();
					if(line!=null && line.trim().equals(IMG_COMMAND)) {
						System.out.println("Received request for image");
						BufferedImage img = m.getWallImg();
						ImageIO.write(img, "png", out);
					} else {
						while(line!=null) {
							System.out.println("Received URL: " + line);
							m.queueURL(line);
							line = in.readLine();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
