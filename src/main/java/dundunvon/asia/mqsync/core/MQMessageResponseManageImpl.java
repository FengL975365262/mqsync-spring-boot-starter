package dundunvon.asia.mqsync.core;

import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import dundunvon.asia.mqsync.api.MQMessageResponseManage;
import dundunvon.asia.mqsync.api.MQRequestMessageSender;

@Component
@ConditionalOnProperty(name="mq.sync.response.actor",havingValue = "true")
public class MQMessageResponseManageImpl implements MQMessageResponseManage{
	final private static String BUSINESSKEYNAME = "MQ_SYNC_BUSINESSKEY";
	private ThreadLocal<String> localbusinesskey = new ThreadLocal<String>();
	
	@Autowired
	@Qualifier("responseSender")
	private MQRequestMessageSender responseSender;
	
	public void setRequestSender(MQRequestMessageSender responseSender) {
		this.responseSender = responseSender;
	}
	@Override
	public void recvRequest(Object obj) {
		String businessKey = null;
		if (obj instanceof MessageExt) {
			MessageExt response = (MessageExt) obj;
			businessKey = response.getUserProperty(MQMessageResponseManageImpl.BUSINESSKEYNAME);
		} else if (obj instanceof Message) {
			Message response = (Message) obj;
			businessKey = response.getUserProperty(MQMessageResponseManageImpl.BUSINESSKEYNAME);
		} else if (obj instanceof org.springframework.messaging.Message) {
			org.springframework.messaging.Message response = (org.springframework.messaging.Message) obj;
			businessKey = response.getHeaders().get(MQMessageResponseManageImpl.BUSINESSKEYNAME).toString();
		}
		if (businessKey != null)
			this.localbusinesskey.set(businessKey);
	}

	@Override
	public void sendResponse(String destnation, String payload) {
		String businesskey = this.localbusinesskey.get();
		org.springframework.messaging.Message<String> message = MessageBuilder.withPayload(payload)
				.setHeader(MQMessageResponseManageImpl.BUSINESSKEYNAME, businesskey) // 设置业务键，方便后续查询追踪消息
				.build();
		try {
			responseSender.send(destnation,message, businesskey);
		} catch (Exception e) {
			this.localbusinesskey.remove();
		}
	}
	
}
