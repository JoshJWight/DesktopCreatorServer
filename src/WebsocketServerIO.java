import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class WebsocketServerIO extends WebSocketServer{

	public Monitor m;
	
	public WebsocketServerIO( int port ) throws UnknownHostException {
		super( new InetSocketAddress( port ) );
		System.out.println("Server listening on port " + port);
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
		String args[] = message.split(";");
		
		if(args.length < 1) {
			System.out.println("received empty message");
			return;
		}
		
		switch(args[0]){
		case "submit-url":{
			if(args.length < 2) {
				System.out.println("received submit-url with no args");
			} else {
				System.out.println("received url " + args[1]);
				m.queueURL(args[1]);
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
