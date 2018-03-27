# Dubbo-多协议
- dubbo 
	- 缺省协议
	- 单一长连接NIO异步通信，适合小数据量大并发的服务调用，以及消费者数远大于生产者数的情况
	- 不太适合大数据量传输
	- 大致请求流程
		- proxy -> client ->transporter -> server -> dispather -> threadpool -> implement
	- 特性
		- 基于mina 1.1.7、hessian 3.2.1
		- 连接个数：1
		- 连接方式：长连接
		- 传输协议：TCP
		- 传输方式：NIO
		- 序列化：hessian二进制序列化
		- 场景：远程服务方法调用
	- 约束
		- 传输对象（参数、返回值）需事先序列化接口
		- 返回值不能是List, Map, Number, Date, Calendar 等接口的自定义实现，只能是JDK自带的实现
		- 服务器端和客户端对领域对象并不需要完全一致，而是按照最大匹配原则。
	- 配置
		- <dubbo:protocol name=“dubbo” port=“9090” server=“netty” client=“netty” codec=“dubbo” serialization=“hessian2” charset=“UTF-8” threadpool=“fixed” threads=“100” queues=“0” iothreads=“9” buffer=“8192” accepts=“1000” payload=“8388608” />
	- 可选
			- Transporter: mina, netty, grizzy
			- Serialization: dubbo, hessian2, java, json
			- Dispatcher: all, direct, message, execution, connection
			- ThreadPool: fixed, cached

- rmi
	- RMI 协议采用 JDK 标准的 java.rmi.* 实现，采用阻塞式短连接和 JDK 标准序列化方式。
	- 特性
			- 连接个数：多连接
			- 连接方式：短连接
			- 传输协议：TCP
			- 传输方式：同步传输
			- 序列化：Java 标准二进制序列化
			- 适用范围：传入传出参数数据包大小混合，消费者与提供者个数差不多，可传文件。
			- 适用场景：常规远程服务方法调用，与原生RMI服务互操作
		- 约束
			- 序列化接口
			- dubbo 配置中的超时时间对 RMI 无效，需使用 java 启动参数设置：-Dsun.rmi.transport.tcp.responseTimeout=3000
	
	- hessian
		- Hessian协议用于集成 Hessian 的服务，Hessian 底层采用 Http 通讯，采用 Servlet 暴露服务，Dubbo 缺省内嵌 Jetty 作为服务器实现。
	- Dubbo 的 Hessian 协议可以和原生 Hessian 服务互操作
	- 特性
			- 连接个数：多连接
			- 连接方式：短连接
			- 传输协议：HTTP
			- 传输方式：同步传输
			- 序列化：Hessian二进制序列化
			- 适用范围：传入传出参数数据包较大，提供者比消费者个数多，提供者压力较大，可传文件。
			- 适用场景：页面传输，文件传输，或与原生hessian服务互操作
		- 依赖
			
			```xml
				<dependency>
			    <groupId>com.caucho</groupId>
			    <artifactId>hessian</artifactId>
			    <version>4.0.7</version>
			</dependency>
			```

		- 约束
			- 同dubbo协议
		- 配置
		- <dubbo:protocol name="hessian" port="8080" server="jetty" />

- http
	- 基于 HTTP 表单的远程调用协议，采用 Spring 的 HttpInvoker 实现
	- 特性
	     - 连接个数：多连接
			- 连接方式：短连接
			- 传输协议：HTTP
		- 传输方式：同步传输
		- 序列化：表单序列化
		- 适用范围：传入传出参数数据包大小混合，提供者比消费者个数多，可用浏览器查看，可用表单或URL传入参数，暂不支持传文件。
		- 适用场景：需同时给应用程序和浏览器 JS 使用的服务。
	- 约束
		- 参数及返回值需符合 Bean 规范
	- 配置
		- <dubbo:protocol name="http" port="8080" server="servlet"/>
		- DispatcherServlet
			
			```xml
				<servlet>
				         <servlet-name>dubbo</servlet-name>
				         <servlet-class>com.alibaba.dubbo.remoting.http.servlet.DispatcherServlet</servlet-class>
				         <load-on-startup>1</load-on-startup>
				</servlet>
				<servlet-mapping>
				         <servlet-name>dubbo</servlet-name>
				         <url-pattern>/*</url-pattern>
				</servlet-mapping>
			```
			
	- 注意点
		- 协议的端口 <dubbo:protocol port="8080" /> 必须与 servlet 容器的端口相同
		- 协议的上下文路径 <dubbo:protocol contextpath="foo" /> 必须与 servlet 应用的上下文路径相同	

- webservice
	- 基于CXF		
	
- thrift

- memcached

- redis