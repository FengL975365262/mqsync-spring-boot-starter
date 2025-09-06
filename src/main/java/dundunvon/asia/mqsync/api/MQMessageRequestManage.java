package dundunvon.asia.mqsync.api;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface MQMessageRequestManage<T> {
	/**
	 * 发送mq请求消息
	 * @param destnation
	 * @param payload
	 * @param businesskey
	 */
	public void sendRequest(String destnation,String payload, String businesskey);
	
	/**
	 * 阻塞当前请求，等待应答
	 * @param request
	 * @return
	 * @throws TimeoutException 
	 */
	public T syncRequest(String businessKey,long timeout,TimeUnit timeunit) throws TimeoutException;
	
	/**
	 * 收到应答消息，获取businesskey
	 * @param message
	 */
	public void recvResponse(Object message);
	
	/**
	 * 向MQ接收同步应答消息
	 * @return T 应答消息类型
	 */
	public void syncResponse(T obj);
	
}
