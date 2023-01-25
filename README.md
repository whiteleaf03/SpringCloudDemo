# Maven配置

## 父工程

注意SpringBoot和SpringCloud和SpringCloudAlibaba版本间存在对应关系

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>SpringCloud</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <modules>
        <module>NacosProvider1</module>
        <module>NacosProvider2</module>
        <module>NacosConsumer1</module>
    </modules>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.7</version>
    </parent>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>2021.0.4</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>2021.0.4.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

```java
/**
 * 在主类上加上@EnableDiscoveryClient来将微服务注册到注册中心
 * 在Spring Cloud Edgware版本后可省略
 */
@SpringBootApplication
public class ServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class);
    }
}
```

# 消息队列

## 	RabbitMQ

# 注册中心

## 	Euraka

## 	Nacos

​		官方文档：https://nacos.io/zh-cn/

​		安装方式：https://github.com/alibaba/nacos

​			github下载压缩包，解压后在bin文件夹下输入

```
startup.cmd -m standalone
```

​			访问 http://192.168.220.1:8848/nacos/index.html 默认账号名和密码为 nacos

​			**坑：启动除8848端口外，还会占用9848 9849端口（与默认端口相差1000， 1001），如果这两个端口被占用，启动失败**

​		pom依赖

```xml
<dependency>
	<groupId>com.alibaba.cloud</groupId>
	<artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

​		Spring配置文件(yml为例)

```yaml
spring:
  application:
  	# 服务名
    name: NacosProvider
  cloud:
    nacos:
      discovery:
      	# nacos注册中心地址
        server-addr: localhost:8848
```

​		注册服务

```java
/**
 * 在主类上加上@EnableDiscoveryClient来将微服务注册到注册中心
 * 在Spring Cloud Edgware版本后可省略
 */
@SpringBootApplication
public class ServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class);
    }
}
```

​		Ribbon负载均衡

​			在RestTemplate生成处添加@LoadBalanced注解

```java
@Bean
@LoadBalanced
public RestTemplate restTemplate() {
	return new RestTemplate();
}
```

​			在方法调用处使用服务名代替ip和端口

```java
/**
 * 使用ip和端口访问微服务
 */
List<ServiceInstance> instances = discoveryClient.getInstances("NacosProvider"); //根据服务名获取微服务实例列表
ServiceInstance instance = instances.get(0); //取出微服务实例
restTemplate.getForObject("http://" + instance.getHost() + ":" + instance.getPort() + "/message", String.class); //调用微服务

/**
 * 使用Ribbon负载均衡调用微服务
 */
restTemplate.getForObject("http://NacosProvider/message", String.class);
```

​			Ribbon负载均衡策略

​			配置

```yaml
  server-name: # 当前注册服务的name
    ribbon:
      NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RoundRobinRule # 负载均衡策略
```

​			类型

| 名称                      | 解释                                                 |
| ------------------------- | ---------------------------------------------------- |
| RoundRobinRule            | 轮询策略（默认）                                     |
| RandomRule                | 随机策略                                             |
| BestAvailableRule         | 存活服务中并发量最小的                               |
| WeightedResponseTimeRule  | 针对响应时间加权轮训                                 |
| AvailabilityFilteringRule | 先过滤故障服务和大于阈值的高并发服务，剩余服务中轮询 |
| ZoneAvoidanceRule         | 从最佳区域实例中选择一个最优性能的服务实例           |
| RetryRule                 | 选择一个服务实例，如果失败重新选一个服务实例重试     |

### Feign

​	pom依赖

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

```java
/**
 * 在主类上使用@EnableFeignClients启用Feign
 */
@EnableFeignClients
@SpringBootApplication
public class ServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class);
    }
}
```

​	**坑：Spring Cloud Feign在新版本不再使用Ribbon而是使用spring-cloud-loadbalancer**

​	可能出现

```
No Feign Client for loadBalancing defined. Did you forget to include spring-cloud-starter-loadbalancer?
```

​	解决方案	参考https://blog.csdn.net/qq_43788878/article/details/115764008

​		去除Ribbon（好像不去除也行）

```xml
<dependency>
   <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    <exclusions>
        <exclusion>
            <groupId>com.netflix.ribbon</groupId>
            <artifactId>ribbon</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

​		引入SpringCloudLoadBalancer

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-loadbalancer</artifactId>
</dependency>
```



# 流量控制

## Sentinel

# 网关

## SpringCloudGateway