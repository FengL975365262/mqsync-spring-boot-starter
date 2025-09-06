package dundunvon.asia.mqsync.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import dundunvon.asia.mqsync.api.MQMessageRequestManage;
import dundunvon.asia.mqsync.api.MQRequestMessageSender;
import dundunvon.asia.mqsync.model.MQRepsonseSyncObject;
/**
 * 
 * @param <T> 泛型指的是应答原调用方需要返回的应答类型
 */
@Component
@ConditionalOnProperty(name="mq.sync.request.actor",havingValue = "true")
public class MQMessageRequestManageImpl<T> implements MQMessageRequestManage<T> {

	final private static String BUSINESSKEYNAME = "MQ_SYNC_BUSINESSKEY";

	@Autowired
	@Qualifier("requestSender")
	private MQRequestMessageSender requestSender;

	private ThreadLocal<String> localBusinessKey = new ThreadLocal<String>();

	private ThreadLocal<MQRepsonseSyncObject<T>> localRequestObj = new ThreadLocal<MQRepsonseSyncObject<T>>();

	private ConcurrentHashMap<String, MQRepsonseSyncObject<T>> mesgMap = new ConcurrentHashMap();

	public void setRequestSender(MQRequestMessageSender requestSender) {
		this.requestSender = requestSender;
	}
	
	public void sendRequest(String destnation,String payload, String businesskey) {
		this.localBusinessKey.set(businesskey);
		this.localRequestObj.set(new MQRepsonseSyncObject<T>(businesskey));
		org.springframework.messaging.Message<String> message = MessageBuilder.withPayload(payload)
				.setHeader(MQMessageRequestManageImpl.BUSINESSKEYNAME, businesskey) // 设置业务键，方便后续查询追踪消息
				.build();
		try {
			requestSender.send(destnation,message, businesskey);
		} catch (Exception e) {
			this.localBusinessKey.remove();
			this.localRequestObj.remove();
		}
	}

	@Override
	public T syncRequest(String businessKey, long timeout, TimeUnit timeunit) throws TimeoutException {
		if (this.localBusinessKey.get() != businessKey || this.localRequestObj.get() == null) {
			return null;
		}
		MQRepsonseSyncObject<T> respobj = this.localRequestObj.get();
		// 清空threadlocal对象
		this.localBusinessKey.remove();
		this.localRequestObj.remove();
		// 设置超时时间
		respobj.setTimeout(timeout >= 0L ? timeout : respobj.getTimeout());
		respobj.setTimeunit(timeunit != null ? timeunit : respobj.getTimeunit());
		// 放入单例实例中
		this.mesgMap.put(businessKey, respobj);
		return respobj.sendResponse();
	}

	public void recvResponse(Object obj) {
		String businessKey = null;
		if (obj instanceof MessageExt) {
			MessageExt response = (MessageExt) obj;
			businessKey = response.getUserProperty(MQMessageRequestManageImpl.BUSINESSKEYNAME);
		} else if (obj instanceof Message) {
			Message response = (Message) obj;
			businessKey = response.getUserProperty(MQMessageRequestManageImpl.BUSINESSKEYNAME);
		} else if (obj instanceof org.springframework.messaging.Message) {
			org.springframework.messaging.Message response = (org.springframework.messaging.Message) obj;
			businessKey = response.getHeaders().get(MQMessageRequestManageImpl.BUSINESSKEYNAME).toString();
		}
		if (businessKey != null)
			this.localBusinessKey.set(businessKey);
	}

	@Override
	public void syncResponse(T obj) {
		String businesskey = this.localBusinessKey.get();
		
		this.localBusinessKey.remove();

		if(businesskey==null)return;
		
		MQRepsonseSyncObject<T> respobj = this.mesgMap.remove(businesskey);
		if (null == respobj)
			return;
		respobj.recvResponse(obj);
	}

}
