# 技术选型与架构建议：健壮高效易用的数据库查询 API 服务

## 一、引言

基于详细的项目需求分析，本建议旨在为“健壮高效易用的数据库查询 API 服务”项目提供具体的技术选型考量和初步的架构设计思路。目标是构建一个满足高性能、高可靠性、高安全性、强扩展性及优秀可观测性的现代化 API 服务。

## 二、核心技术栈与选型理由

### 1. 后端框架与语言
*   **Java 17+ & Spring Boot 3.x**: 
    *   **理由**: Java 17 (LTS) 提供了最新的语言特性、性能优化和长期支持。Spring Boot 3.x 基于 Spring Framework 6.x，全面拥抱 Java 17+，并默认集成了 Jakarta EE 9+ 规范，提供了更现代化的开发体验。其自动配置、起步依赖 (Starters)、嵌入式服务器等特性极大简化了微服务应用的开发与部署。选择 Spring Boot 能够充分利用其庞大的生态系统、成熟的社区支持以及丰富的集成能力，快速构建稳定可靠的服务。

### 2. 构建工具
*   **Maven**: 
    *   **理由**: Maven 是 Java 生态中成熟且广泛使用的构建工具，具备强大的依赖管理、项目生命周期管理和插件生态。项目已明确指定 Maven，我们将遵循此选择。

### 3. 持久层框架
*   **MyBatis 与 Spring Data JPA 的权衡与建议**: 
    *   **MyBatis**: 提供了对 SQL 的完全控制，适合复杂查询、性能优化要求高的场景。开发者可以直接编写和优化 SQL 语句，灵活性高。
    *   **Spring Data JPA**: 提供了高度抽象的持久化操作，通过约定优于配置的方式，可以快速实现 CRUD 操作，减少样板代码。对于标准化的数据访问场景，开发效率较高。
    *   **建议**: 
        *   **元数据存储 (MySQL/OceanBase)**: 对于服务自身的元数据管理，其数据结构相对固定，查询逻辑可能不那么复杂，可以优先考虑 **Spring Data JPA** 以提高开发效率。其提供的 Repository 模式能很好地满足需求。
        *   **动态 SQL 查询服务**: 考虑到此服务的核心是执行用户定义或系统预置的 SQL，且可能涉及对不同数据库的复杂查询和性能调优，**MyBatis** 更为合适。它允许开发者精确控制 SQL 的生成与执行，更好地满足动态性和性能要求。
        *   **混合使用**: Spring Boot 也支持在同一项目中同时使用 MyBatis 和 Spring Data JPA，针对不同模块或场景选择最适合的技术。例如，元数据管理用 JPA，核心 SQL 执行用 MyBatis。

### 4. 元数据存储数据库
*   **MySQL / OceanBase**: 
    *   **理由**: 两者都是成熟的关系型数据库。MySQL 应用广泛，社区支持好；OceanBase 作为分布式关系型数据库，具备高可用、可扩展的特性。选择哪一个取决于团队对特定数据库的熟悉程度、运维能力以及对数据一致性、可用性的具体要求。服务将通过 JDBC 连接，持久层框架会屏蔽底层差异。

### 5. 连接池
*   **HikariCP**: 
    *   **理由**: Spring Boot 默认推荐并集成了 HikariCP，它以其高性能和稳定性著称。我们将使用 HikariCP 来管理所有数据库连接，包括元数据存储和动态接入的目标数据源。

## 三、核心功能模块架构建议

### 1. 动态数据源管理模块
*   **设计思路**: 
    *   实现一个 `DataSourceRegistry` 服务，负责动态数据源的注册、获取和管理。
    *   支持两种接入方式：
        1.  **外部 API 调用**: 实现一个 `ExternalApiDataSourceProvider`，通过配置的第三方 API 地址和认证信息，获取连接参数。
        2.  **本地元数据查询**: 实现一个 `MetadataDbDataSourceProvider`，从元数据库（MySQL/OB）中读取预存的连接信息。
    *   使用 Spring 的 `AbstractRoutingDataSource` 或类似机制，结合 ThreadLocal 变量（例如存储当前请求期望的 `datasource_id` 或 `sourceType`），实现运行时动态切换数据源。
    *   每个动态创建的数据源实例都应配置独立的 HikariCP 连接池参数，确保隔离性和性能。
    *   元数据表中应包含数据源标识、类型、连接URL、用户名、加密后的密码/凭证密钥、连接池参数（最大连接数、超时时间等）。

### 2. SQL 查询服务模块
*   **内置静态 SQL 管理**: 
    *   **存储**: SQL 模板初始存储在项目的 `src/main/resources/sql-templates/` 目录下，按 `sqlCode.sql` 命名。服务启动时，通过 `InitializingBean` 或 `@PostConstruct` 读取这些文件，解析并持久化到元数据库的 `sql_templates` 表中（包含 `sqlCode`, `sql_content`, `description`, `version`, `last_modified_by`, `last_modified_date` 等字段）。
    *   **动态修改**: 提供管理 API，允许通过 `sqlCode` 更新数据库中的 SQL 内容。考虑版本控制或审计日志。
*   **批量执行**: 
    *   API 接收 `List<SqlExecutionRequest>`，每个 `SqlExecutionRequest` 包含 `sqlCode` 和 `Map<String, Object> params`。
    *   **执行策略**: 
        *   默认可以采用**并发执行**以提高效率。使用 `CompletableFuture` 和自定义的 `ExecutorService`（配置合理的线程池大小）来并行处理每个 SQL 执行请求。
        *   如果业务场景有严格的顺序要求，可以提供一个参数让调用方选择串行执行。
    *   **结果封装**: 每个 SQL 执行后，无论成功失败，都记录其 `sqlCode`、执行状态（`SUCCESS`, `FAILURE`）、结果数据（成功时）或错误信息（失败时）。最终聚合成一个 JSON 数组返回。
*   **SQL 执行扩展性 (动态 SQL)**: 
    *   定义一个 `DynamicSqlProvider` SPI 接口，例如：
        ```java
        public interface DynamicSqlProvider {
            String getSql(String identifier, Map<String, Object> context);
        }
        ```
    *   系统可以加载实现了此接口的插件。例如，可以开发 `ScriptCenterSqlProvider` 从脚本中心获取 Groovy/Python 脚本执行后返回 SQL，或 `ConfigCenterSqlProvider` 从配置中心（如 Apollo）获取 SQL 字符串。
    *   查询服务优先查找内置静态 SQL，若未找到或明确指定动态来源，则调用相应的 `DynamicSqlProvider`。

### 3. API 接口设计
*   **RESTful 规范**: 严格遵循，例如：
    *   数据源管理: `POST /api/v1/datasources`, `GET /api/v1/datasources/{id}`
    *   SQL模板管理: `POST /api/v1/sql-templates`, `PUT /api/v1/sql-templates/{sqlCode}`
    *   SQL执行: `POST /api/v1/query/execute` (批量执行)
*   **OpenAPI 3.0 (Swagger UI)**: 
    *   使用 `springdoc-openapi-starter-webmvc-ui` 依赖自动生成 OpenAPI 文档。
    *   在 Controller 和 DTO 中使用 `@Schema`, `@Operation` 等注解详细描述 API 和数据模型。

### 4. 可视化调试前端
*   **技术选型**: React 或 Vue.js，根据团队熟悉度选择。
*   **交互流程**: 
    1.  用户在前端页面选择数据源接入方式并填写参数（或选择已配置的数据源）。
    2.  前端调用后端 `/api/v1/sql-templates` 获取可选的 `sqlCode` 列表。
    3.  用户选择 `sqlCode` 并输入参数。
    4.  前端将请求（包含数据源信息、`sqlCode`、参数）发送到后端 `/api/v1/query/execute`。
    5.  后端返回 JSON 结果，前端将其解析并以表格或树形视图展示。
*   **组件**: 使用成熟的前端 UI 库（如 Ant Design for React, Element Plus for Vue）和数据可视化库。

### 5. 其他 API 调用能力
*   **HTTP Client 封装**: 
    *   **推荐**: **Spring Cloud OpenFeign** 或 **Spring WebClient**。
        *   **Feign**: 声明式 REST 客户端，与 Spring Cloud 生态集成良好，代码简洁。如果未来有更多微服务间调用，Feign 是个不错的选择。
        *   **WebClient**: Spring 5 引入的非阻塞、响应式 HTTP 客户端。即使项目主体是同步阻塞的，WebClient 也可以用于异步调用外部 API，提高资源利用率。
    *   封装通用的请求/响应处理、错误处理、日志记录、超时配置等。
*   **外部接口配置管理**: 
    *   在元数据库中创建 `external_api_configs` 表，存储接口名称、URL、认证类型、凭证信息（加密）、超时设置等。
    *   提供管理 API 进行配置的增删改查。

## 四、非功能性需求实现策略

*   **性能 (< 300ms P95, ≥ 500 QPS)**:
    *   高效连接池 (HikariCP) 并合理配置。
    *   MyBatis 针对热点查询进行 SQL 调优，利用索引。
    *   异步化处理：批量执行并发处理，外部 API 调用使用 WebClient/Feign。
    *   缓存机制：对于不经常变化的元数据或 SQL 模板，引入本地缓存 (Caffeine) 或分布式缓存 (Redis)。
    *   合理的线程池配置。
    *   进行压力测试 (JMeter, k6) 并持续优化。
*   **可靠性 (幂等、重试、熔断、限流)**:
    *   **幂等设计**: 关键的写操作（如创建/更新数据源、SQL模板）通过唯一业务ID或请求ID + Token机制保证幂等。
    *   **失败重试**: 使用 Spring Retry 或 Resilience4J 的 Retry模块，针对可重试的异常（如网络抖动）进行配置化重试（次数、间隔）。
    *   **熔断**: 集成 Resilience4J CircuitBreaker，对外部 API 调用和不稳定的数据源连接进行熔断保护，防止雪崩。
    *   **限流**: 使用 Resilience4J RateLimiter 或 Spring Cloud Gateway（如果作为API网关）进行接口限流，保护服务。
*   **安全 (HTTPS, OAuth2/JWT, SQL注入防护)**:
    *   **HTTPS**: 部署时配置 SSL/TLS 证书。
    *   **认证授权**: Spring Security 集成 OAuth2.0 (Resource Server) 或 JWT。客户端请求携带 Token，服务端验证 Token 有效性及权限。
    *   **参数校验**: 使用 Bean Validation (Hibernate Validator) 对 API 入参进行严格校验。
    *   **SQL注入防护**: 
        *   MyBatis: 使用 `#{}` 占位符，避免使用 `${}` 进行字符串拼接。
        *   JPA: 默认使用参数化查询。
        *   对动态传入的 SQL 片段（如果未来支持）进行严格的白名单校验或语法解析。
*   **扩展性 (模块化, SPI, 配置中心)**:
    *   **模块化设计**: 遵循高内聚低耦合原则，按功能划分模块 (e.g., `datasource-manager`, `sql-executor`, `api-gateway-adapter`)。
    *   **SPI/插件机制**: 如前述 `DynamicSqlProvider` 示例，为易变点或扩展点定义接口。
    *   **配置中心 (Apollo/Consul)**: 集成 Spring Cloud Config Client，连接到 Apollo 或 Consul。将应用配置（数据库连接、线程池大小、重试次数、外部API地址等）集中管理，支持动态刷新。
*   **可观测性 (Prometheus, Grafana, Zipkin/Jaeger, Logging)**:
    *   **Metrics**: `spring-boot-starter-actuator` + `micrometer-registry-prometheus` 导出 Prometheus 指标。关注 JVM 指标、Tomcat 指标、连接池指标、API QPS/延迟等。
    *   **Dashboard**: 提供预设的 Grafana 仪表盘 JSON 配置，导入 Grafana 进行可视化。
    *   **Tracing**: 集成 `spring-cloud-starter-sleuth` (Brave) 或 Micrometer Tracing (OpenTelemetry) + Zipkin/Jaeger exporter，实现分布式链路追踪。
    *   **Logging**: 使用 SLF4J + Logback/Log4j2。配置结构化日志输出 (JSON格式)，包含 traceId, spanId，便于 ELK/EFK 栈收集分析。
*   **测试覆盖率 (单元 ≥ 80%, 集成 ≥ 70%)**: 
    *   **单元测试**: JUnit5 + Mockito。针对 Service 层、Util 类等进行测试。
    *   **集成测试**: Spring Boot Test (`@SpringBootTest`) + Testcontainers。Testcontainers 用于启动真实的数据库 (MySQL/OB) 实例进行持久层和业务逻辑集成测试。
*   **部署与运维 (Docker/JAR, 健康检查)**:
    *   **Dockerfile**: 提供优化的 Dockerfile，构建多阶段镜像，减小镜像体积。
    *   **JAR**: Maven 构建可执行 JAR 包。
    *   **健康检查**: Actuator 提供 `/actuator/health` 端点，可定制检查数据库连接、磁盘空间等。Kubernetes 等编排平台可利用此端点进行 liveness 和 readiness 探测。
*   **文档 (OpenAPI, 示例, FAQ)**:
    *   **OpenAPI**: 已通过 springdoc-openapi 实现。
    *   **示例代码**: 提供 Postman 集合或 curl 命令示例。
    *   **FAQ与错误码**: 编写 `FAQ.md` 和 `ERROR_CODES.md`，详细说明常见问题及解决方案、API 返回的错误码含义。

## 五、总结

本技术选型与架构建议旨在为项目提供一个坚实的基础。在实际开发过程中，可能需要根据具体场景和遇到的问题进行调整和细化。建议采用敏捷开发模式，小步快跑，持续集成和测试，确保项目质量和进度。

