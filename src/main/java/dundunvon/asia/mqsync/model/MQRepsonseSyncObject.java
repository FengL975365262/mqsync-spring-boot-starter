package dundunvon.asia.mqsync.model;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.Data;

/**
 * 当请求在controller处理完后，准备应答时，构造该对象并进入阻塞
 */
@Data
public class MQRepsonseSyncObject<T> {

	private String requestKey;
	private Long requestTime = System.currentTimeMillis();
	private Long timeout = 3L;
	private TimeUnit timeunit = TimeUnit.SECONDS;
	final private Object lockobject = new Object();
	private T response;
	private volatile boolean isResponsed = false;

	public MQRepsonseSyncObject() {
	}

	public MQRepsonseSyncObject(String businesskey) {
		this.requestKey = businesskey;
	}

	public T sendResponse() throws TimeoutException {
		if (response == null) {
			synchronized (lockobject) {
				try {
					if (response == null)
						lockobject.wait(timeunit.toMillis(timeout));
				} catch (InterruptedException e) {
					
				}
			}
		}
		this.isResponsed = true;
		return this.response;
	}

	public void recvResponse(T response) {

		this.response = response;
		synchronized (lockobject) {
			lockobject.notify();
		}
	}

}
