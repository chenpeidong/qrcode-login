package site.syso.controller.websocket;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Controller;

@Controller
@ServerEndpoint(value = "/ws/qrcode/{ticket}")
public class ScanQRCodeServer {

    private static ConcurrentHashMap<String, Session> sockets = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("ticket") String ticket) {
        System.out.println("socket open .");
        System.out.println("ticket:" + ticket);
        sockets.put(ticket, session);
        System.out.println("size:" + sockets.size());
    }

    @OnClose
    public void onClose(@PathParam("ticket") String ticket) {
        System.out.println("close:" + ticket);
        System.out.println("socket close .");
        sockets.remove(ticket);
        System.out.println("size:" + sockets.size());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println(session.getId() + " say " + message);
    }

    @OnError
    public void onError(@PathParam("ticket") String ticket, Throwable error) {
        sockets.remove(ticket);
        error.printStackTrace();
    }


    public void response(String ticket, String message) throws IOException {
        System.out.println("response : ticket:" + ticket + ", message:" + message);
        sockets.get(ticket).getBasicRemote().sendText(message);
    }

}
