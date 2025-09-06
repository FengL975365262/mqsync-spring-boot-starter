package dundunvon.asia.mqsync.api;

import org.springframework.messaging.Message;

public interface MQRequestMessageSender {

	public void send(String destnation,Message<String> Message,String businessKey);
	
}
