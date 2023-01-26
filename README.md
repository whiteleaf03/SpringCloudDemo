学习视频:https://www.bilibili.com/video/BV1LQ4y127n4

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

```bash
startup.cmd -m standalone
```

​			访问 http://192.168.220.1:8848/nacos/index.html 默认账号名和密码为 nacos

​			**坑：启动除8848端口外，还会占用9848 9849端口（与默认端口相差1000， 1001），如果这两个端口被占用，启动失败**

​			开启HyperV后，端口被系统保留的解决方法 https://blog.csdn.net/crayon0/article/details/127444776

​			管理员启动cmd

```bash
关闭网络
net stop winnat
net stop LanmanWorkstation
net stop WlanSvc

指定不允许被保留的端口
netsh int ipv4 add excludedportrange protocol=tcp startport=8080 numberofports=4

启动网络
net start winnat
net start LanmanWorkstation
net start WlanSvc
```

​			注：在解除9849端口保留时 我的电脑提示：另一个程序正在使用此文件，进程无法访问。但端口并未被占用且nacos启动并无问题

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

​	官方文档：https://sentinelguard.io/zh-cn/docs/introduction.html

​	安装方式：https://github.com/alibaba/Sentinel

​		github下载jar包，使用命令启动Sentinel控制台

​		其中 `-Dserver.port=8080` 用于指定 Sentinel 控制台端口为 `8080`

​		从 Sentinel 1.6.0 起，Sentinel 控制台引入基本的**登录**功能，默认用户名和密码都是 `sentinel`

```bash
java -Dserver.port=8080 -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard.jar
```

​	pom依赖

​		黑马程序员教程视频 https://www.bilibili.com/video/BV1R7411774f?p=20

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

​		官方文档（版本1.8.6）

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-core</artifactId>
    <version>1.8.6</version>
</dependency>
```

**坑**：不知道为啥 按视频依赖可以在控制台中看到自己的服务 而按官方文档走看不到



# 网关

## Gateway

​	pom依赖

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

​	**注：除此之外还需引入注册中心依赖（网关本身也是微服务，需注册到注册中心）**

​	**坑：还需引入Feign相关依赖 同上 需将Ribbon除外 并引入SpringCloudLoadbalancer**

​	**通过Springboot配置文件 配置路由 断言 过滤器**

```yml
spring:
  cloud:
    gateway:
      routes:
        - id: NacosConsumer # 路由id 在所有路由中唯一即可
          uri: lb://NacosConsumer # 路由目的地 支持http和lb lb为负载均衡 http为固定地址
          predicates: # 路由断言 判断请求是否符合路由规则的条件	
            - Path=/message/** # 按路径匹配 要求/message开头
            - After=2021-07-04T19:16:43.338+08:00[Asia/Shanghai] # 在该时间之后 还有Before 之前 Between 之间
            - Cookie=chocolate # 必须包含某些cookie
            - Header=X-Request-Id # 必须包含某些header
            - Host=**.example.org # 必须访问某域名(host)
            - Method=GET POST # 请求必须是制定方式
            - Query=name # 请求必须包含指定参数
            - RemoteAddr=192.168.0.1/24 # 请求者的ip必须是指定范围
          filters: # 针对单个路由
    		
       default-filters: # 全局过滤器 对所有请求都用作用
        - AddRequestHeader=Check, SpringCloudGateway # 请求头中添加 Check=SpringCloudGateway
```

路由断言示例参考官网文档：https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#gateway-request-predicates-factories

路由过滤器示例参考官网文档：https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#gatewayfilter-factories

​	**全局过滤器 GlobalFilter** 对进入当前网关的所有请求起效

```java
/**
 * 全局过滤器 以身份认证为例
 * 需继承GlobalFilter接口
 */
@Component
@Order(-1) // 在过滤器链中的位置 除注解外可继承Order接口实现
public class AuthorizeFilter implements GlobalFilter {
    /**
     * 全局过滤器 判断是否含有authorization参数且参数值为admin
     * 是 放行给下一个过滤器
     * 否 拦截并返回401
     * @param exchange 请求上下文 可获取Request Response等信息
     * @param chain    过滤器链中的下一个过滤器
     * @return 标志当前过滤器业务结束
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        MultiValueMap<String, String> params = request.getQueryParams();
        String auth = params.getFirst("authorization");
        if ("admin".equals(auth)) {
            return chain.filter(exchange);
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
```

​		**三个不同过滤器能串联的原因：**

​			默认过滤器和路由过滤器在底层中属于GatewayFilter类

​			GatewayFilterAdapter类实现了GatewayFilter接口，并含有GlobalFilter类型的成员变量

​			默认过滤器  路由过滤器  全局过滤器基于此进行串联

​		**过滤器执行顺序：**

​			每个过滤器都必须指定一个int类型的Order的值，Order值越小，优先级越高，执行顺序越靠前

​			全局过滤器Order的值由开发者决定

​			默认过滤器和路由过滤器的Order由Spring指定，默认按配置文件中的声明顺序从1递增

​			当过滤器值一样时，按照  **默认过滤器 > 路由过滤器 > 全局过滤器**  的顺序执行

​	**跨域配置**

```yaml
spring:
  cloud:
	gateway:
      globalcors: # 全局跨域处理
        add-to-simple-url-handler-mapping: true # 解决options请求被拦截问题
        cors-configurations:
          '[/**]':
            allowed-origins: # 允许哪些网站的跨域请求
              - "http://localhost:8080"
              - "http://www.whiteleaf03.cn"
            allowed-methods: # 允许跨域的方式
              - "GET"
              - "POST"
              - "DELETE"
              - "PUT"
              - "OPTIONS"
            allowed-headers: "*" # 允许在请求头中携带的头信息
            # allowed-credentials: true # 是否允许携带cookie 该项会报错并且在官方文档中并未找到该项
            max-age: 360000 # 这次跨域检测的有效期
```

官方文档：https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#cors-configuration

