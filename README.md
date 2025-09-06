# mqsync-spring-boot-starter
简单的mq消息异步转同步的实现
<br/>
## 使用方式
### 环境 
springboot3以上，使用一款mq
### 代码
#### 配置application.yml
1.  mq.sync.request.actor: true #作为请求方时
2.  mq.sync.response.actor: true #作为应答方时
#### 实现MQRequestMessageSender.java接口，并注入到容器中
1.  作为请求方时，指定bean名称为**requestSender**
2.  作为应答方时，指定bean名称为**responseSender**
#### 应答消息的消息接收由自己定义，但需要完成以下操作。
1.  收到应答消息时，调用MQMessageRequestManage.recvResponse再进行业务处理。
2.  当完成应答消息处理后，调用MQMessageRequestManage.syncResponse将结果应答给原请求阻塞线程。
#### 请求方消息发送和阻塞等待应答
1.  请求方通过MQMessageRequestManage.sendRequest发送消息
2.  后任意时刻，都可以调用MQMessageRequestManage.syncRequest进入阻塞等待应答
3.  阻塞默认的超时时间是7秒，可根据需要自行设置
#### 应答方处理和请求方类似
1.  收到请求时，首先调用MQMessageResponseManage.recvRequest再进行业务处理
2.  后再任意时刻都可调用MQMessageResponseManage.sendResponse发送应答消息
<br/>
## 整体流程如下
<img width="3724" height="1764" alt="image" src="https://github.com/user-attachments/assets/b6b05d2b-6c9f-4c52-bb61-0d6f358587ae" />
