import java.awt.image.BufferedImage;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class WebsocketServerIO extends WebSocketServer{

	public Monitor m;
	private JsonParser json;
	
	public WebsocketServerIO( int port ) throws UnknownHostException {
		super( new InetSocketAddress( port ) );
		System.out.println("Server listening on port " + port);
		json = new JsonParser();
	}

	public WebsocketServerIO( InetSocketAddress address ) {
		super( address );
	}

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		System.out.println("Connection started: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
		System.out.println("Connection ended: " + conn);
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
		JsonObject messageObj = json.parse(message).getAsJsonObject();
		
		String method = messageObj.get("method").getAsString();
		
		switch(method){
		case "submit-url":{
			String url = messageObj.get("url").getAsString();
			
			System.out.println("received url " + url);
			m.queueURL(url);
			break;
		}
		case "get-object":{
			ImageObject obj = DBSession.session.retrieveObject();
			if(obj != null) {
				System.out.println("Serving object " + obj.name);
				conn.send("{\"method\":\"get-object\", \"name\":\"" + obj.name + "\", \"image\":\"" + obj.toBase64() + "\"}");
			} else {
				System.out.println("Serving null");
				conn.send("");
			}
			
			break;
		}
		case "rate-object":{
			
			boolean rating = messageObj.get("rating").getAsBoolean();
			String name = messageObj.get("name").getAsString();
			System.out.println("received rating " + rating + " for " + name);
			if(rating == true) {
				ImageObject imgObj = DBSession.session.retrieveObject(name);
				DBSession.session.saveObject(imgObj);
				m.queueObj(imgObj);
			} else {
				DBSession.session.deleteObject(name);
			}
			break;
		}
		case "get-wallpaper":{
			BufferedImage wall = m.getWallImg();
			if(wall != null) {
				System.out.println("Serving wallpaper");
				conn.send("{\"method\":\"get-wallpaper\", \"image\":\"" + new ImageObject("temp", wall).toBase64() + "\"}");
			} else {
				System.out.println("Serving null");
				conn.send("");
			}
			
			break;
		}
		default:{
			System.out.println("received unsupported message " + message);
		}
		}
		
	}
	
	@Override
	public void onError( WebSocket conn, Exception ex ) {
		ex.printStackTrace();
		if( conn != null ) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}
}
