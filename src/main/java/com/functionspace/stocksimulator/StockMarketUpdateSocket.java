/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.functionspace.stocksimulator;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Singleton;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * This class represents web socket that pushes data to all peers connected to it.
 * 
 * @author Chaitanya
 */
@Singleton
@ServerEndpoint(value = "/stockUpdate")
public class StockMarketUpdateSocket {

    private static Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());

    /**
     * Gets called when session is established
     * 
     * @param peer 
     */
    @OnOpen
    public void onOpen(Session peer) {
        peers.add(peer);
        System.out.println("In onOpen: " + peer);
    }
    
    /**
     * Gets called when session is closed
     * @param peer 
     */
    @OnClose
    public void onClose(Session peer) {
        peers.remove(peer);
    }
    /**
     * Pushes given message as string on to the websocket and all sessions connected to it.
     * 
     * @param stockUpdatesJson
     * @throws IOException
     * @throws EncodeException 
     */
    @OnMessage
    public void broadcastStockUpdates(String stockUpdatesJson) throws IOException, EncodeException {
        //System.out.println("In broadcastStockUpdates: " + stockUpdatesJson);
        for (Session peer : peers) {
            peer.getBasicRemote().sendText(stockUpdatesJson);
        }
    }
}
