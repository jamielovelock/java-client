/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
 */

package microsoft.aspnet.signalr.client.transport;

import java.net.URI;

import microsoft.aspnet.signalr.client.ConnectionBase;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.http.HttpConnection;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * HttpClientTransport implementation over Server Sent Events implementation
 */
public class WebSocketTransport extends HttpClientTransport {

	private WebSocketClient mClient;

	/**
	 * Initializes the transport with a logger
	 * 
	 * @param logger
	 *            Logger to log actions
	 */
	public WebSocketTransport(Logger logger) {
		super(logger);
	}

	/**
	 * Initializes the transport with a logger
	 * 
	 * @param logger
	 *            Logger to log actions
	 * @param httpConnection
	 *            HttpConnection for the transport
	 */
	public WebSocketTransport(Logger logger, HttpConnection httpConnection) {
		super(logger, httpConnection);
	}

	@Override
	public String getName() {
		return "serverSentEvents";
	}

	@Override
	public boolean supportKeepAlive() {
		return true;
	}

	@Override
	public SignalRFuture<Void> send(ConnectionBase connection, String data,
			DataResultCallback callback) {

		SignalRFuture<Void> future = new SignalRFuture<Void>();
		if (mClient == null) {
			throw new NullPointerException(
					"Must call start in advance in order to initialize the transport");
		}
		mClient.send(data);
		return future;
	}

	@Override
	public SignalRFuture<Void> start(ConnectionBase connection,
			ConnectionType connectionType, final DataResultCallback callback) {
		log("Start the communication with the server using WebSocket",
				LogLevel.Information);

		String protocol = "ws";
		String url = connection.getUrl();

		String serverLocation = protocol + url;

		SignalRFuture<Void> future = new SignalRFuture<Void>();
		future.setResult(null);
		URI uri = URI.create(serverLocation);

		mClient = new WebSocketClient(uri) {

			@Override
			public void onOpen(ServerHandshake handshakedata) {
				callback.onData(handshakedata.toString());
			}

			@Override
			public void onMessage(String message) {
				callback.onData(message);
			}

			@Override
			public void onError(Exception ex) {
				callback.onData(ex.getMessage());
			}

			public void onClose(int code, String reason, boolean remote) {
				callback.onData(reason);
			}
		};
		mClient.connect();
		return future;
	}
}
