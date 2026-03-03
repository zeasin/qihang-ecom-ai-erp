# 项目运行指南

## 1. 项目结构

```
qihang-ecom-ai-erp/
├── api/                # 后端 API 模块
├── core/               # 核心模块
│   ├── security/       # 安全认证模块
│   └── ...
├── vue/                # 前端 Vue 项目
├── SSE-Communication-Implementation.md  # SSE 通信实现文档
└── Project-Run-Guide.md                 # 项目运行指南
```

## 2. 运行环境

### 2.1 后端环境

- **Java**: JDK 17 或更高版本
- **Maven**: 3.6.0 或更高版本
- **Spring Boot**: 3.0.2
- **Redis**: 用于 token 缓存（可选）
- **MySQL**: 数据库

### 2.2 前端环境

- **Node.js**: 20.20.0（使用 nvm 管理）
- **npm**: 随 Node.js 安装
- **Vue**: 2.6.12
- **Element UI**: 前端组件库

## 3. 环境配置

### 3.1 后端配置

1. **数据库配置**：
   - 修改 `api/src/main/resources/application.yml` 文件中的数据库连接信息

2. **Redis 配置**（可选）：
   - 修改 `api/src/main/resources/application.yml` 文件中的 Redis 连接信息

3. **端口配置**：
   - 默认后端端口：8087
   - 可在 `application.yml` 文件中修改

### 3.2 前端配置

1. **环境变量**：
   - 开发环境：`vue/.env.development`
   - 生产环境：`vue/.env.production`

2. **API 代理配置**：
   - 修改 `vue/vue.config.js` 文件中的代理配置
   - 默认代理到 `http://localhost:8087`

3. **端口配置**：
   - 默认前端端口：88
   - 可在 `vue/vue.config.js` 文件中修改

## 4. 启动步骤

### 4.1 启动后端服务

#### 方法一：使用 Maven 直接运行

```bash
# 在项目根目录执行
mvn clean install spring-boot:run -pl api -am
```

#### 方法二：打包后运行

```bash
# 在项目根目录执行
mvn clean package

# 运行打包后的 jar 文件
java -jar api/target/api-2.2.0.jar
```

### 4.2 启动前端服务

1. **激活 Node.js 环境**：
   ```bash
   nvm use 20.20
   ```

2. **安装依赖**：
   ```bash
   cd vue
   npm install
   ```

3. **启动开发服务器**：
   ```bash
   npm run dev
   ```

## 5. 常见问题及解决方法

### 5.1 端口占用问题

**症状**：启动服务时提示端口已被占用

**解决方法**：

1. **查找占用端口的进程**：
   ```bash
   # Windows
   netstat -ano | findstr :8087
   
   # Linux/Mac
   lsof -i :8087
   ```

2. **终止占用端口的进程**：
   ```bash
   # Windows
   taskkill /F /PID <进程ID>
   
   # Linux/Mac
   kill -9 <进程ID>
   ```

### 5.2 Maven 依赖问题

**症状**：Maven 构建时出现依赖解析错误

**解决方法**：

1. **清理 Maven 本地仓库**：
   ```bash
   mvn dependency:purge-local-repository
   ```

2. **重新构建**：
   ```bash
   mvn clean install -U
   ```

### 5.3 前端依赖问题

**症状**：npm 安装依赖时出现错误

**解决方法**：

1. **清理 npm 缓存**：
   ```bash
   npm cache clean --force
   ```

2. **重新安装依赖**：
   ```bash
   rm -rf node_modules
   npm install
   ```

### 5.4 SSE 连接问题

**症状**：SSE 连接失败，返回 404 或 401 错误

**解决方法**：

1. **检查后端服务是否运行**：
   - 确认后端服务在 8087 端口正常运行

2. **检查 SSE 路径配置**：
   - 确认前端 SSE 连接 URL 正确：`${process.env.VUE_APP_BASE_API}/api/sse/connect`

3. **检查 Token 配置**：
   - 确认 `TokenService.java` 支持从 URL 参数获取 token
   - 确认前端请求携带了 token 参数

## 6. 开发流程

### 6.1 后端开发

1. **修改代码**：在 `api` 模块中修改后端代码
2. **构建项目**：`mvn clean install`
3. **重启服务**：重新启动后端服务

### 6.2 前端开发

1. **修改代码**：在 `vue` 模块中修改前端代码
2. **热更新**：前端开发服务器会自动热更新
3. **构建生产版本**：`npm run build`

## 7. 部署建议

### 7.1 后端部署

1. **打包**：`mvn clean package`
2. **部署**：将 `api/target/api-2.2.0.jar` 部署到服务器
3. **启动**：`java -jar api-2.2.0.jar`

### 7.2 前端部署

1. **构建**：`npm run build`
2. **部署**：将 `vue/dist` 目录下的文件部署到静态文件服务器
3. **配置**：配置 Nginx 或其他 web 服务器，将 API 请求代理到后端服务

## 8. 技术栈

### 8.1 后端技术栈

- **Spring Boot 3.0.2**：后端框架
- **MyBatis-Plus**：ORM 框架
- **Redis**：缓存
- **JWT**：认证
- **SSE**：服务器推送

### 8.2 前端技术栈

- **Vue 2.6.12**：前端框架
- **Element UI**：组件库
- **Axios**：HTTP 客户端
- **EventSource**：SSE 客户端

## 9. 项目维护

### 9.1 日志管理

- 后端日志：`api/logs` 目录
- 前端日志：浏览器控制台

### 9.2 监控

- 后端监控：可集成 Spring Boot Actuator
- 前端监控：可集成前端监控工具

### 9.3 版本管理

- 使用 Git 进行版本控制
- 遵循语义化版本规范

## 10. 总结

本项目采用前后端分离架构，后端使用 Spring Boot 3.0.2，前端使用 Vue 2.6.12，通过 SSE 实现实时通信。项目运行需要 Java 17+、Maven 3.6+、Node.js 20.20.0 环境。

启动流程：
1. 启动后端服务：`mvn clean install spring-boot:run -pl api -am`
2. 启动前端服务：`nvm use 20.20 && cd vue && npm run dev`

访问地址：
- 前端：`http://localhost:88`
- 后端 API：`http://localhost:8087/api`
- SSE 连接：`http://localhost:88/dev-api/api/sse/connect`