package org.ic4j.websocket;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;


@ClientEndpoint
public class JavaxWebsocketTransport implements WebsocketTransport {
	
    Session userSession = null;
    private MessageHandler messageHandler;   


    public JavaxWebsocketTransport(URI endpointURI) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        System.out.println("opening websocket");
        this.userSession = userSession;
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        System.out.println("closing websocket");
        this.userSession = null;
    }
    
    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null) {
        	if(message != null)
				try {
					this.messageHandler.handleMessage(message.getBytes());
				} catch (WebsocketError e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					try {
						userSession.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, e.getLocalizedMessage()));
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
        }
    }    

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param bytes Byte buffer
     */

   @OnMessage
   public void onMessage(ByteBuffer bytes) {
       if (this.messageHandler != null) {
           try {
			this.messageHandler.handleMessage(bytes.array());
		} catch (WebsocketError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				userSession.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, e.getLocalizedMessage()));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
       }
    }
   
   /**
    * register message handler
    *
    * @param msgHandler
    */
   public void addMessageHandler(MessageHandler msgHandler) {
       this.messageHandler = msgHandler;
   }   

   /**
    * Send a message.
    *
    * @param envelope Byte array
    */
   public void sendMessage(byte[] envelope) {
	   ByteBuffer bytes = ByteBuffer.wrap(envelope);
       this.userSession.getAsyncRemote().sendBinary(bytes);
   } 
   
   
   public void sendMessage(Object message) {
	   
		ObjectMapper objectMapper = new ObjectMapper(new CBORFactory()).registerModule(new Jdk8Module());

		ObjectWriter objectWriter = objectMapper.writerFor(message.getClass());


		byte[] buf = null;
		try {
			buf = objectWriter.writeValueAsBytes(message);
			
			ByteBuffer bytes = ByteBuffer.wrap(buf);
			this.userSession.getAsyncRemote().sendBinary(bytes);
		} catch (JsonProcessingException e) {
			// normally, rethrow exception here - or don't catch it at all.
		}	   		
   }
     

}
