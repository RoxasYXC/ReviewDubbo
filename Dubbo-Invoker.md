# Dubbo-Invoker
- Invoker
	- Invoker封装了Provider的地址信息和Service的抽象
	- Cluster 将 Directory 中的多个 Invoker 伪装成一个 Invoker，对上层透明，伪装过程包含了容错逻辑，调用失败后，重试另一个
	- Directory是一个invokkerList，内容会根据注册广播变更
	- Router负责从Directory中选取子集
	- LoadBalance 根据LB策略选取合适的Invoker，获取最终要执行的Invoker

