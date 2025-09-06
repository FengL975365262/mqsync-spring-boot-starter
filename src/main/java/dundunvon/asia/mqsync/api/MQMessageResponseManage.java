package dundunvon.asia.mqsync.api;

public interface MQMessageResponseManage {
	
	/**
	 * 收到请求消息，获取businesskey
	 * @param message
	 */
	public void recvRequest(Object message);
	
	/**
	 * 发送应答消息
	 * @param destnation
	 * @param payload
	 */
	public void sendResponse(String destnation,String payload);
	
}
