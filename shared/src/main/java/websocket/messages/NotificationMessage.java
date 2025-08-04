package websocket.messages;

public class NotificationMessage extends ServerMessage {

    public NotificationMessage(String msg) {
        super(ServerMessageType.NOTIFICATION);
        this.msg = msg;
    }


}
