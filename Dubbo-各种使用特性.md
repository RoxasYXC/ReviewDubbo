# Dubbo实用特性
- 集群容错
	- 可自行扩展实现xxx implements com.alibaba.dubbo.rpc.cluster.Cluster
		- <dubbo:protocol cluster="xxx" />
	- failover
		- 可配置重试次数
			- <dubbo:reference retries="2" />
	- failfast
		- 只试一次，失败抛出异常
	- failsafe
		- 忽略异常
	- failback
		- 失败自动恢复，后台记录失败请求，定时重发。
	- forking
		- 并行调用多个服务器，只要一个成功即返回。通常用于实时性要求较高的读操作，但需要浪费更多服务资源。可通过 forks="2" 来设置最大并行数。
	- boardcast
		- 广播调用所有提供者，逐个调用，任意一台报错则报错 2。通常用于通知所有提供者更新缓存或日志等本地资源信息。
	- 配置方式
		- <dubbo:reference cluster="failsafe" />

- LB
	- 默认LB为Random
	- 可自行扩展 xxx implements com.alibaba.dubbo.rpc.cluster.LoadBalance
		- <dubbo:protocol loadbalance="xxx" />
	- Random
		- 随机LB
	- RoundRobin
		- 轮询LB
	- LeastActive
		- 最少活跃数LB，处理时+1，处理完-1，处理慢的机器少处理请求
	- ConsistentHash
		- 一致性Hash，相同参数的请求总是发到同一提供者。
		- 当某一台提供者挂时，原本发往该提供者的请求，基于虚拟节点，平摊到其它提供者，不会引起剧烈变动。
		- 默认对第一个参数hash <dubbo:parameter key="hash.arguments" value="0,1" />
		- 默认160个虚拟节点，<dubbo:parameter key="hash.nodes" value="320" />

- 线程模型
	-  Dispatcher
		- all 所有消息都派发到线程池，包括请求，响应，连接事件，断开事件，心跳等。
		- direct 所有消息都不派发到线程池，全部在 IO 线程上直接执行。
		- message 只有请求响应消息派发到线程池，其它连接断开事件，心跳等消息，直接在 IO 线程上执行。
		- execution 只请求消息派发到线程池，不含响应，响应和其它连接断开事件，心跳等消息，直接在 IO 线程上执行。
		- connection 在 IO 线程上，将连接断开事件放入队列，有序逐个执行，其它消息派发到线程池。
	- ThreadPool
		- fixed 固定大小线程池，启动时建立线程，不关闭，一直持有。(缺省)
		- cached 缓存线程池，空闲一分钟自动删除，需要时重建。
		- limited 可伸缩线程池，但池中的线程数只会增长不会收缩。只增长不收缩的目的是为了避免收缩时突然来了大流量引起的性能问题。

- 只订阅
	- 某服务只订阅，不注册，通过直连来进行测试
	- <dubbo:registry address="10.20.153.10:9090" register="false" />
	- 和直连一起使用

- 只注册
	- 两个registry，其中一个服务不完整或未发布的情况
	- <dubbo:registry id="qdRegistry" address="10.20.141.150:9090" subscribe="false" />
	
- 静态管理服务
	- 有时候希望人工管理服务提供者的上线和下线，此时需将注册中心标识为非动态管理模式。
	- <dubbo:registry address="10.20.141.150:9090" dynamic="false" />
		
		```java
			RegistryFactory registryFactory = ExtensionLoader.getExtensionLoader(RegistryFactory.class).getAdaptiveExtension();
			Registry registry = registryFactory.getRegistry(URL.valueOf("zookeeper://10.20.153.10:2181"));
			registry.register(URL.valueOf("memcached://10.20.153.11/com.foo.BarService?category=providers&dynamic=false&application=foo"));
		```
		
- 多协议
	- 在不同服务上支持不同协议或者同一服务上同时支持多种协议。
	- 不同服务不同协议
		
		```xml
			<?xml version="1.0" encoding="UTF-8"?>
			<beans xmlns="http://www.springframework.org/schema/beans"
			    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			    xmlns:dubbo="http://code.alibabatech.com/schema/dubbo"
			    xsi:schemaLocation="http://www.springframework.org/schema/beanshttp://www.springframework.org/schema/beans/spring-beans.xsdhttp://code.alibabatech.com/schema/dubbohttp://code.alibabatech.com/schema/dubbo/dubbo.xsd"> 
			    <dubbo:application name="world"  />
			    <dubbo:registry id="registry" address="10.20.141.150:9090" username="admin" password="hello1234" />
			    <!-- 多协议配置 -->
			    <dubbo:protocol name="dubbo" port="20880" />
			    <dubbo:protocol name="rmi" port="1099" />
			    <!-- 使用dubbo协议暴露服务 -->
			    <dubbo:service interface="com.alibaba.hello.api.HelloService" version="1.0.0" ref="helloService" protocol="dubbo" />
			    <!-- 使用rmi协议暴露服务 -->
			    <dubbo:service interface="com.alibaba.hello.api.DemoService" version="1.0.0" ref="demoService" protocol="rmi" /> 
			</beans>
		```
	
	- 同服务多协议
		
		```xml
			<?xml version="1.0" encoding="UTF-8"?>
			<beans xmlns="http://www.springframework.org/schema/beans"
			    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			    xmlns:dubbo="http://code.alibabatech.com/schema/dubbo"
			    xsi:schemaLocation="http://www.springframework.org/schema/beanshttp://www.springframework.org/schema/beans/spring-beans.xsdhttp://code.alibabatech.com/schema/dubbohttp://code.alibabatech.com/schema/dubbo/dubbo.xsd">
			    <dubbo:application name="world"  />
			    <dubbo:registry id="registry" address="10.20.141.150:9090" username="admin" password="hello1234" />
			    <!-- 多协议配置 -->
			    <dubbo:protocol name="dubbo" port="20880" />
			    <dubbo:protocol name="hessian" port="8080" />
			    <!-- 使用多个协议暴露服务 -->
			    <dubbo:service id="helloService" interface="com.alibaba.hello.api.HelloService" version="1.0.0" protocol="dubbo,hessian" />
			</beans>
		```
		
- 多服务中心
	- 通过在xml里配置registry实现

-  服务分组
	- 当一个接口有多种实现时，可以用 group 区分。
	- 通过在xml里配置group实现
	- 可配置任意组，即只调用一个可用组的实现
	
- 多版本
	- 通过在xml里配置version实现
	- provider和consumer一一对应即可
	- 可配置任意版本，即调用一个可用版本的实现

- 分组聚合
	- merger="true"
	- 结合group使用
	- SPI com.alibaba.dubbo.rpc.cluster.Merger

- 结果缓存
	-  结果缓存，用于加速热门数据的访问速度，Dubbo 提供声明式缓存，以减少用户加缓存的工作量 
	- cache="lru"
	- 缓存方式
		- lru 基于最近最少使用原则删除多余缓存，保持最热的数据被缓存。
		- threadlocal 当前线程缓存，比如一个页面渲染，用到很多 portal，每个 portal 都要去查用户信息，通过线程缓存，可以减少这种多余访问。
		- jcache 与 JSR107 集成，可以桥接各种缓存实现。

- 异步调用
	- async="true"
	- sent="true" 等待消息发出，消息发送失败将抛出异常。
	- sent="false" 不等待消息发出，将消息放入 IO 队列，即刻返回。
	- return="false" 如果你只是想异步，完全忽略返回值，可以配置 return="false"，以减少 Future 对象的创建和管理成本

- 本地调用
	- 本地调用使用了 injvm 协议，是一个伪协议，它不开启端口，不发起远程调用，只在 JVM 内直接关联，但执行 Dubbo 的 Filter 链。
	- <dubbo:protocol name="injvm" />
	
- 事件通知
	- 在调用之前、调用之后、出现异常时，会触发 oninvoke、onreturn、onthrow 三个事件，可以配置当事件发生时，通知哪个类的哪个方法 
	- 异步回调模式：async=true onreturn="xxx"
	- 同步回调模式：async=false onreturn="xxx"
	
- 本地存根
	- 远程服务后，客户端通常只剩下接口，而实现全在服务器端，但提供方有些时候想在客户端也执行部分逻辑，比如：做 ThreadLocal 缓存，提前验证参数，调用失败后伪造容错数据等等
	- stub="com.foo.BarServiceStub"

- 本地伪装
	- 本地伪装通常用于服务降级，比如某验权服务，当服务提供方全部挂掉后，客户端不抛出异常，而是通过 Mock 数据返回授权失败。
	- mock="com.foo.BarServiceMock"
	
- 延迟暴露
	- 如果你的服务需要预热时间，比如初始化缓存，等待相关资源就位等，可以使用 delay 进行延迟暴露。
		- delay="5000"
	- 延迟到 Spring 初始化完成后，再暴露服务
		- delay="-1"
	
- 并发控制
	- 服务器端并发数控制
		- executes="10"
	- 每客户端并发数控制
		- actives="10"
	- loadbalance 属性为 leastactive，此 Loadbalance 会调用并发数最小的 Provider（Consumer端并发数）。
		
- 连接控制
	- 限制服务器端接受的连接数
		- accepts="10"
	- 客户端连接数控制
		- connections="10"
		
- 延迟连接
	- 延迟连接用于减少长连接数。当有调用发起时，再创建长连接。
		- lazy="true"
	
- 粘滞连接
	- 粘滞连接用于有状态服务，尽可能让客户端总是向同一提供者发起调用，除非该提供者挂了，再连另一台。
		- sticky="true"

- 令牌验证
	- 保证consumer不会绕过Registry直接调用Provider	
		- token="true"

- 线程栈自动导出
	- 当业务线程池满时，我们需要知道线程都在等待哪些资源、条件，以找到系统的瓶颈点或异常点。dubbo通过Jstack自动导出线程堆栈来保留现场，方便排查问题
	- <dubbo:parameter key="dump.directory" value="/tmp" />
		
		