# 枫落 FengLuo

**面向 Spring Boot 服务的动态线程池管理与可观测性项目。**

枫落围绕业务线程池的注册、运行状态查看和参数调整，逐步建设配置管理、异常观测与运维管理能力。当前开发重点是把单 JVM 内的线程池管理做完整，再接入配置中心和多实例管理。

> 项目处于开发阶段。目前可以尝试本地线程池注册、查询与动态调参；远程配置下发、实例生效反馈和完整管理平台尚未完成。模块目录的存在不代表相应功能已交付。

## 项目解决什么问题

业务服务通常包含邮件发送、异步处理、后台任务等线程池。线程池创建后，需要明确它的名称、当前运行状态、参数调整方式，以及服务关闭时由谁负责释放资源。

枫落当前提供以下本地能力：

- **统一注册**：发现明确标注的线程池 Bean、字段和无参方法，保存实际对象引用。
- **原地调参**：按名称调整核心线程数、最大线程数和存活时间，业务继续使用原线程池。
- **运行状态查询**：获取线程数、活跃数、队列长度、完成任务数等本地快照。
- **队列容量调整**：自定义队列支持扩缩容，缩容保留已有任务。
- **生命周期管理**：清理已终止线程池的注册记录，并在容器关闭时执行关闭与等待。

在此基础上，项目计划提供“配置发布 → 实例应用 → 结果反馈 → 状态查询”的管理流程。任务调度、通知与搜索是平台扩展方向。

## 当前能力与进度

| 领域 | 当前情况                                      |
|---|-------------------------------------------|
| 动态线程池 | 已实现本地注册、重名检查、原地调参、状态快照、队列容量调整和关闭管理        |
| Spring 接入 | 已实现注解扫描与自动配置；保留观测启动器的兼容导入入口               |
| 观测能力 | 已有异常处理、异常切面与 JVM 指标接入代码；具体启用项取决于服务配置      |
| 账号与权限 | 已有用户、角色、菜单、用户角色关系及 JWT/Redis 认证授权代码       |
| 网关 | 已有请求标识透传、路径检查和鉴权，当前用户服务路由为 `/maple/**`    |
| 配置管理 | 已有线程池配置与快照的数据模型、Mapper 和基础服务代码，尚未完成远程应用流程 |
| 任务调度 | 已有任务定义、执行记录、分组与关联的数据模型及基础代码，调度执行链路待建设     |
| 搜索 | todo Elasticsearch 接入与索引初始化代码，业务检索能力待完善   |
| 监控中心与通知 | 模块已规划，集中管理与通知流程待建设                        |

## 项目结构

```text
FengLuo/
├── Fengluo_DynamicTP/              # 动态线程池与当前 Spring 接入实现
├── Fengluo-Observability-Starter/   # 异常处理、JVM 观测、兼容接入
├── Fengluo-Config/                 # 线程池配置与配置快照管理
├── Fengluo-Monitor/                # 监控模块集合
│   ├── Fengluo-Monitor-SDK/        # 监控接入模块，待完善
│   ├── Fengluo-Request-Service/    # 请求与异常管理方向
│   └── Fengluo-Sys-Service/        # 实例与系统监控管理方向
├── Fengluo-Task/                   # 任务定义、分组与执行记录
├── Fengluo-Notice/                 # 通知模块，待建设
├── Fengluo-Search/                 # Elasticsearch 搜索服务基础
├── Fengluo-User/                   # 账号、用户、角色与菜单
├── Fengluo-Auth/auth-service/      # 共享认证授权组件
├── Geteway/                       # 网关，沿用当前目录拼写
├── FengLuo-Api/                   # 服务间调用基础依赖
├── Fengluo-Common/                # 统一响应、异常与公共工具
├── sql/                           # 本地数据库脚本，当前被 Git 忽略
└── pom.xml                        # Maven 聚合构建与版本管理
```

## 快速体验动态线程池

### 1. 环境与构建

准备 JDK 17、Maven 3.x。在仓库根目录构建模块并安装到本地 Maven 仓库：

```shell
mvn -pl Fengluo_DynamicTP -am install
```

本地线程池管理不需要启动 MySQL、Redis 或 Nacos。当前模块仍依赖 Fengluo-Common，并包含 Spring 接入代码，尚不是独立的纯 JDK Core。

### 2. 在 Spring Boot 应用中引入依赖

```xml
<dependency>
    <groupId>com.fengluo</groupId>
    <artifactId>Fengluo_DynamicTP</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

当前示例使用本地安装的 SNAPSHOT 构件，不假设已发布到公共 Maven 仓库。

启用自动注册：

```yaml
monitor:
  threadpool:
    dynamic:
      enabled: true
```

### 3. 注册业务线程池

在业务配置类中声明：

```java
import com.guilu.annotation.DynamicThreadPool;
import com.guilu.DynamicThreadPool.FLBlockingQueue.CapacityBlockingQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.*;

@Configuration(proxyBeanMethods = false)
public class BusinessPoolConfiguration {
    @DynamicThreadPool(name = "field_TP")
    private final ThreadPoolExecutor executor =
            new ThreadPoolExecutor(
                    1,
                    1,
                    20,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(10)
            );

    @DynamicThreadPool(name = "dynamicTP1")
    public ExecutorService createTP() {
        return Executors.newFixedThreadPool(1);
    }
    @Bean("dynamicTP2")
    @DynamicThreadPool(name = "dynamicTP2")
    public ExecutorService createTP2() {
        return Executors.newFixedThreadPool(2);
    }
    //以dynamicTP3注册Bean与动态线程池
    @Bean
    @DynamicThreadPool(name = "dynamicTP3")
    public ExecutorService createTP3() {
        return Executors.newFixedThreadPool(2);
    }
}
```

显式 Bean 名为空时，依次使用 `@DynamicThreadPool(name)`、DynamicPool 内部名称、自动生成的 `dynamic_tp_N`。回退名称通过 Spring 别名暴露，原方法名保留。需要按名称注入时，建议显式填写 Bean 名。

### 4. 查询与调整

在线程池完成注册后调用：

```java
// 与业务提交任务时使用的是同一个线程池对象。
var executor = PoolInquirer.of().getPool("emailPool");

// 核心线程数 4、最大线程数 12、存活时间 60 秒、队列容量 200。
PoolUpdater.of()
    .update("emailPool", 4, 12, 60, TimeUnit.SECONDS, 200);

// 返回本地状态快照，目前不自动发送到远程监控平台。
var snapshot = new PoolReporter().snapshot();
```

队列容量传 `null` 表示保持不变。普通固定容量队列不支持此处动态缩放；自定义 CapacityBlockingQueue 缩容时不删除已有任务，数量降到新容量以下后才继续接收入队。

## 启动业务服务

账号与网关服务需要另行准备环境：

1. 准备 Nacos、MySQL 和 Redis，检查各模块 `bootstrap.yml` / `bootstrap-dev.yml`。
2. 在 Nacos 中准备配置引用的共享配置
3. 准备用户、角色、菜单及关联表，初始化管理员和 `/maple/...` 权限，确认权限缓存已发布。
4. 编译所需服务：`mvn -pl Geteway,Fengluo-User -am compile`。
5. 在 IDE 中先运行 `com.guilu.UserApplication`，再运行 `com.guilu.GateWayApplication`。

| 入口 | 默认地址 |
|---|---|
| 网关 | `http://localhost:10000` |
| 用户服务 | `http://localhost:10010` |
| 经网关登录 | `POST http://localhost:10000/maple/user/login` |
| 直连用户服务登录 | `POST http://localhost:10010/maple/user/login` |

上述路径依赖用户服务配置的 `/maple` context-path；网关保留此前缀。地址和端口以实际配置为准。登录需要验证码，不能只提交用户名和密码。



## 当前限制与后续方向

- 注册表使用 JVM 级静态状态，尚不支持多个独立 ApplicationContext 隔离。
- Spring 接入和核心实现仍在同一模块，后续计划拆分 Core 与 Starter。
- 已注册线程池会参与模块的关闭管理，外部共享线程池应明确生命周期所有权。
- ThreadPoolTaskExecutor 适配的是底层线程池；外层包装器配置字段不随底层修改自动同步。
- 本地调参不代表跨实例同步；Nacos 下发、配置版本、实例应用结果与失败补偿待实现。
- 任务调度引擎集成、通知投递、集中监控和业务日志检索仍需继续建设。

后续优先完善线程池管理的接入体验与可靠性，再实现远程配置闭环；性能与长时间稳定性需要独立验证。