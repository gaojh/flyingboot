# flyingboot

## 项目介绍
基于Netty的轻量级web快速开发框架。  
使用netty+completeableFuture 异步方式提高吞吐量。  
可用于网关开发等系统，在使用习惯上模仿了spring的相关注解，学习成本低。

不扯啥重复造轮子，这个框架是在做api网关的过程中抽取出来的，本着实用方便的原则，封装成了类似于springboot的样子。

## 接入使用
### 1. 添加maven依赖
```xml
<dependency>
    <groupId>com.github.gaojh</groupId>
    <artifactId>flyingboot</artifactId>
    <version>3.0.0</version>
</dependency>
```
### 2. 添加项目配置文件
flyingboot默认读取application.properties配置文件中的配置  
所有配置项可以使用Environment来进行获取

application.properties默认参数如下，如果需要修改，可以在文件中重新定制:
```properties
#httpserver端口，默认8080
server.port=8080

#flyingboot通用连接池的core size 默认600
flying.thread.core.size=600

#flyingboot通用连接池的max size 默认2000
flying.thread.max.size=2000

#flyingboot通用连接池的keeplive time，默认0
flying.thread.keepalive.time=0
```

### 3. 新建启动类
类似于springboot的Application类
```java
@ComponentScan({"com.github.gaojh.example"})
public class FlyingbootDemo {
    public static void main(String[] args) {
        new Flying().port(12345).run(FlyingbootDemo.class);
    }
}
```
如果不加@ComponentScan注解，则包扫描路径直接设置为启动的包路径。

### 4. 添加Controller
```java
@Controller
public class DemoController {

    @Autowired
    private DemoService demoService;

    @RequestMapping("/hello")
    public String hello(@RequestParam String name){
        return demoService.getName(name);
    }

    @RequestMapping("/demo")
    public Object demo(@RequestBody DemoBean demoBean){
        return demoBean;
    }

    @RequestMapping("/h2/*")
    public String h2(){
        return "h2";
    }
}
```
到此时，一个简单的Flyingboot项目已经可以运行了，跟springboot很相似。

### 5. 如何使用过滤器
```java
@Interceptor(pathPatterns = {"/**"}, ignorePathPatterns = {"/hello"}, order = 5)
public class DemoInterceptor implements HandlerInterceptor {
    private static Logger logger = LoggerFactory.getLogger(DemoInterceptor.class);

    @Override
    public HandlerResponse preHandle(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        logger.info("demo");
        return HandlerResponse.success();
    }

    @Override
    public void postHandle(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        logger.info("demo postHandle");
    }

}
```
过滤器需要实现HandlerInterceptor接口，里面有三个方法  
1、preHandle前置处理器，在调用业务方法之前调用，如果返回true，继续调用下个过滤器，如果返回false，则不调用下个过滤器。  
2、postHandle后置处理器，在调用业务方法之后调用。  

@Interceptor注解必须要加上，否则会扫描不到该过滤器。  
1、pathPatterns是用于匹配过滤的url。  
2、ignorePathPatterns是用户匹配忽略过滤的url。  
3、order指定过滤器的顺序

### 6. 如何使用动态Controller
1、首先定义Handler
```java
@Component
public class DemoDynamicHandler implements RouterHandler {

    @Override
    public Object handle(HttpRequest httpRequest) {
        return "ok";
    }
}
```
实现RouterHandler接口，实现handle方法，这个方法就是类似Controller中的方法是一样的。

2、其次添加动态路由到Flyingboot
```java
@com.github.gaojh.mvc.annotation.Setup
public class Setup implements SetupRunner {
    @Override
    public void run() {
        Routers.me.get("/taobao", httpRequest -> "123123123");
    }
}
```
这样就可以添加动态的router
Routers可以动态添加路由，非常适合动态路由使用场景

### 7. 启用websocket
在application.properties中添加配置  
```properties
flying.websocket.enable=true
```
或者
```java
public static void main(String[] args) {
    new Flying().port(11111).enableWebsocket(true).run(FlyingbootDemo.class);
}
```

添加websocketHandler
```java
@Component
public class MyWebSocketHandler implements WebSocketHandler {

    @Override
    public void onHandshake(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {
        System.out.println(fullHttpRequest.uri());
    }

    @Override
    public void onMessage(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("msg: "+msg);
        sendMessage(ctx,"回复："+msg);
    }

    @Override
    public void onClose(ChannelHandlerContext ctx) throws Exception {

    }

}
```

这样websocket就可以使用了
## 其他
相关example请参考flyingboot-test项目
本项目还在不断的迭代中，欢迎关注并提出意见！
