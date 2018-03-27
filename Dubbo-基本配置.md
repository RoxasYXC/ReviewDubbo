# Dubbo-基本配置
- dubbo xml配置
	- <dubbo:service/>	provider服务配置 用于暴露一个服务，定义服务的元信息，一个服务可以用多个协议暴露，一个服务也可以注册到多个注册中心
	- <dubbo:reference/>  consumer引用配置 用于创建一个远程服务代理，一个引用可以指向多个注册中心
		- 可以使用init=“true” 进行饥饿加载
	- <dubbo:protocol/>	协议配置 由provider进行设置，consumer被动接受
	- <dubbo:application/>	当前应用信息
	- <dubbo:module/>	 当前模块信息，可选
	- <dubbo:registry/>	 服务注册中心
	- <dubbo:monitor/>	 dubbo监控中心配置，用于监控服务的健康状况
	- <dubbo:provider/> provider全局设置，当 ProtocolConfig 和 ServiceConfig 某属性没有配置时，采用此缺省值，可选
	- <dubbo:consumer/> provider全局设置，当 ReferenceConfig某属性没有配置时，采用此缺省值，可选
	- <dubbo:method/>	 用于 ServiceConfig 和 ReferenceConfig 指定方法级的配置信息
	- <dubbo:argument/>	用于指定方法参数配置
	
	- xml内相同属性的覆盖优先级
		- 方法级优先，接口级次之，全局配置再次之。
		- 如果级别一样，则消费方优先，提供方次之。	
		- ref method -> service method -> ref -> service -> consumer -> provider

- 属性配置
	- dubbo支持在classpath下配置dubbo.properties，不建议使用
	- dubbo配置的覆盖优先级
		- 命令行 -D -> xml -> properties

- API配置
	- Provider
		
		```java
			import com.alibaba.dubbo.rpc.config.ApplicationConfig;
			import com.alibaba.dubbo.rpc.config.RegistryConfig;
			import com.alibaba.dubbo.rpc.config.ProviderConfig;
			import com.alibaba.dubbo.rpc.config.ServiceConfig;
			import com.xxx.XxxService;
			import com.xxx.XxxServiceImpl;
			
			// 服务实现
			XxxService xxxService = new XxxServiceImpl();
			
			// 当前应用配置
			ApplicationConfig application = new ApplicationConfig();
			application.setName("xxx");
			
			// 连接注册中心配置
			RegistryConfig registry = new RegistryConfig();
			registry.setAddress("10.20.130.230:9090");
			registry.setUsername("aaa");
			registry.setPassword("bbb");
			
			// 服务提供者协议配置
			ProtocolConfig protocol = new ProtocolConfig();
			protocol.setName("dubbo");
			protocol.setPort(12345);
			protocol.setThreads(200);
			
			// 注意：ServiceConfig为重对象，内部封装了与注册中心的连接，以及开启服务端口
			
			// 服务提供者暴露服务配置
			ServiceConfig<XxxService> service = new ServiceConfig<XxxService>(); // 此实例很重，封装了与注册中心的连接，请自行缓存，否则可能造成内存和连接泄漏
			service.setApplication(application);
			service.setRegistry(registry); // 多个注册中心可以用setRegistries()
			service.setProtocol(protocol); // 多个协议可以用setProtocols()
			service.setInterface(XxxService.class);
			service.setRef(xxxService);
			service.setVersion("1.0.0");
			
			// 暴露及注册服务
			service.export();
		```
		
	- Consumer
		
		```java
			import com.alibaba.dubbo.rpc.config.ApplicationConfig;
			import com.alibaba.dubbo.rpc.config.RegistryConfig;
			import com.alibaba.dubbo.rpc.config.ConsumerConfig;
			import com.alibaba.dubbo.rpc.config.ReferenceConfig;
			import com.xxx.XxxService;
			
			// 当前应用配置
			ApplicationConfig application = new ApplicationConfig();
			application.setName("yyy");
			
			// 连接注册中心配置
			RegistryConfig registry = new RegistryConfig();
			registry.setAddress("10.20.130.230:9090");
			registry.setUsername("aaa");
			registry.setPassword("bbb");
			
			// 注意：ReferenceConfig为重对象，内部封装了与注册中心的连接，以及与服务提供方的连接
			
			// 引用远程服务
			ReferenceConfig<XxxService> reference = new ReferenceConfig<XxxService>(); // 此实例很重，封装了与注册中心的连接以及与提供者的连接，请自行缓存，否则可能造成内存和连接泄漏
			reference.setApplication(application);
			reference.setRegistry(registry); // 多个注册中心可以用setRegistries()
			reference.setInterface(XxxService.class);
			reference.setVersion("1.0.0");
			
			// 和本地bean一样使用xxxService
			XxxService xxxService = reference.get(); // 注意：此代理对象内部封装了所有通讯细节，对象较重，请缓存复用
		```
			
	- 	特殊场景
		- 方法级设置
			
			```java
				// 方法级配置
				List<MethodConfig> methods = new ArrayList<MethodConfig>();
				MethodConfig method = new MethodConfig();
				method.setName("createXxx");
				method.setTimeout(10000);
				method.setRetries(0);
				methods.add(method);
				
				// 引用远程服务
				ReferenceConfig<XxxService> reference = new ReferenceConfig<XxxService>(); // 此实例很重，封装了与注册中心的连接以及与提供者的连接，请自行缓存，否则可能造成内存和连接泄漏
				reference.setMethods(methods); // 设置方法级配置
			```
			
		- 点对点直连
			
			```java
				ReferenceConfig<XxxService> reference = new ReferenceConfig<XxxService>(); // 此实例很重，封装了与注册中心的连接以及与提供者的连接，请自行缓存，否则可能造成内存和连接泄漏
				// 如果点对点直连，可以用reference.setUrl()指定目标地址，设置url后将绕过注册中心，
				// 其中，协议对应provider.setProtocol()的值，端口对应provider.setPort()的值，
				// 路径对应service.setPath()的值，如果未设置path，缺省path为接口名
				reference.setUrl("dubbo://10.20.130.230:20880/com.xxx.XxxService"); 
			```
			
	- API建议只在调试或测试集成时使用

- 注解支持
	- 需2.5.7版本以上

- 启动时检查
	- 默认check = “true”，会再系统启动时检查服务是否可用
