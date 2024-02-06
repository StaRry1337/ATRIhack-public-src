package tcy.addon.atrihack.events;

import net.minecraft.text.Text;

public class ChatMessageReceivedEvent {

    public boolean cancelled;
    private Text message;

    public ChatMessageReceivedEvent(Text message) {
        this.message = message;
    }

    public Text getMessage() {
        return message;
    }

    public void setMessage(Text message) {
        this.message = message;
    }

    public void call() {
    }
}
