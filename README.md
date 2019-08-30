# SnowFlake-MybatisPlus-zk-spring-boot-starter
## 使用方法
```xml
<dependency>
   <groupId>com.github.maojx0630</groupId>
   <artifactId>SnowFlake-MybatisPlus-zk-spring-boot-starter</artifactId>
   <version>0.1</version>
</dependency>
```
配置zk地址
```yaml
zk:
  connect: 127.0.0.1:2181
```
调用IdUtils的next或nextStr即可,或使用mybatisPlus 的IdWork亦可
##注意
* 若启动时zk不可用,且开启探测则会使用默认workerId,dataCenterId
* 所有配置信息见ZookeeperConfig.java