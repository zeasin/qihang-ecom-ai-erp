# SSE 通信实现文档

## 1. 项目架构

- **前端**：Vue 2.6.12
- **后端**：Spring Boot 3.0.2
- **通信方式**：SSE (Server-Sent Events)
- **认证**：JWT token

## 2. 后端实现

### 2.1 创建 SSE 控制器

创建了 `SseController.java` 文件，实现了以下功能：

- 建立 SSE 连接
- 发送消息
- 断开连接
- 查看连接状态
- 心跳机制

```java
@RestController
@RequestMapping("/api/sse")
public class SseController {

    private static final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam String clientId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(clientId, emitter);

        // 设置超时处理
        emitter.onTimeout(() -> emitters.remove(clientId));
        emitter.onCompletion(() -> emitters.remove(clientId));

        // 发送连接成功消息
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("连接成功"));
        } catch (IOException e) {
            emitters.remove(clientId);
        }

        // 定期发送心跳
        executorService.scheduleAtFixedRate(() -> {
            try {
                if (emitters.containsKey(clientId)) {
                    emitters.get(clientId).send(SseEmitter.event()
                            .name("heartbeat")
                            .data("ping"));
                }
            } catch (IOException e) {
                emitters.remove(clientId);
            }
        }, 30, 30, TimeUnit.SECONDS);

        return emitter;
    }

    @GetMapping("/send")
    public String sendMessage(@RequestParam String clientId, @RequestParam String message) {
        SseEmitter emitter = emitters.get(clientId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(message));
                return "消息发送成功";
            } catch (IOException e) {
                emitters.remove(clientId);
                return "消息发送失败";
            }
        }
        return "客户端不存在";
    }

    @GetMapping("/disconnect")
    public String disconnect(@RequestParam String clientId) {
        SseEmitter emitter = emitters.remove(clientId);
        if (emitter != null) {
            emitter.complete();
            return "断开连接成功";
        }
        return "客户端不存在";
    }

    @GetMapping("/status")
    public String getStatus() {
        return "当前连接数: " + emitters.size();
    }
}
```

### 2.2 修改 Token 认证

修改了 `TokenService.java` 文件，支持从 URL 参数中获取 token：

```java
// 如果请求头中没有token，从URL参数获取
if (StringUtils.isEmpty(token)) {
    token = request.getParameter("token");
    // 处理URL参数中的token，确保没有Bearer前缀
    if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX)) {
        token = token.replace(Constants.TOKEN_PREFIX, "");
    }
}
```

## 3. 前端实现

### 3.1 SSE 连接初始化

在 `index.vue` 文件中实现了 SSE 连接初始化：

```javascript
initSse() {
  // 生成唯一客户端ID
  this.clientId = 'client_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  // 获取token
  const token = getToken();
  
  // 建立SSE连接，携带token
  this.sse = new EventSource(`${process.env.VUE_APP_BASE_API}/api/sse/connect?clientId=${this.clientId}&token=${token}`);
  
  // 监听连接成功
  this.sse.addEventListener('connected', (event) => {
    console.log('SSE连接成功:', event.data);
    this.isSseConnected = true;
  });
  
  // 监听消息
  this.sse.addEventListener('message', (event) => {
    console.log('收到SSE消息:', event.data);
    this.messages.push({
      content: event.data,
      time: this.formatTime(new Date()),
      isMe: false,
      avatar: ''
    });
    this.scrollToBottom();
  });
  
  // 监听心跳
  this.sse.addEventListener('heartbeat', (event) => {
    console.log('收到心跳:', event.data);
  });
  
  // 监听错误
  this.sse.onerror = (error) => {
    console.error('SSE连接错误:', error);
    this.isSseConnected = false;
    // 尝试重连
    setTimeout(() => {
      this.initSse();
    }, 5000);
  };
}
```

### 3.2 消息发送

实现了消息发送功能：

```javascript
sendMessage() {
  if (!this.inputMessage.trim()) return;
  
  // 添加用户消息
  this.messages.push({
    content: this.inputMessage,
    time: this.formatTime(new Date()),
    isMe: true,
    avatar: ''
  });
  
  // 获取token
  const token = getToken();
  
  // 通过SSE发送消息到后端
  if (this.isSseConnected) {
    // 使用fetch发送消息
    fetch(`${process.env.VUE_APP_BASE_API}/api/sse/send?clientId=${this.clientId}&message=${encodeURIComponent(this.inputMessage)}&token=${token}`)
      .then(response => response.text())
      .then(data => {
        console.log('消息发送结果:', data);
      })
      .catch(error => {
        console.error('消息发送失败:', error);
        // 发送失败时使用模拟回复
        this.generateReply(this.inputMessage);
      });
  } else {
    // SSE未连接时使用模拟回复
    this.generateReply(this.inputMessage);
  }
  
  this.inputMessage = '';
  this.scrollToBottom();
}
```

## 4. 问题修复

### 4.1 404 错误

**问题**：前端请求 SSE 连接时返回 404 错误。

**原因**：前端请求的 URL 缺少 `/api` 前缀。

**修复**：修改前端代码，添加 `/api` 前缀：
```javascript
this.sse = new EventSource(`${process.env.VUE_APP_BASE_API}/api/sse/connect?clientId=${this.clientId}&token=${token}`);
```

### 4.2 授权过期错误

**问题**：SSE 相关请求返回 401 授权过期错误。

**原因**：SSE 连接和消息发送请求没有携带 token。

**修复**：
1. 修改前端代码，在 SSE 连接 URL 中添加 token 参数
2. 修改 `TokenService.java`，支持从 URL 参数中获取 token
3. 在消息发送请求中添加 token 参数

### 4.3 消息发送失败

**问题**：消息发送失败，没有发送动作。

**原因**：`sendMessage` 方法中的 `token` 变量未定义。

**修复**：在 `sendMessage` 方法中添加获取 token 的代码：
```javascript
const token = getToken();
```

## 5. 技术要点

1. **SSE 连接管理**：使用 `ConcurrentHashMap` 存储客户端连接，确保线程安全。
2. **心跳机制**：通过定时任务发送心跳消息，保持连接活跃。
3. **错误处理**：实现了连接错误重连机制，提高系统稳定性。
4. **认证处理**：支持从 URL 参数中获取 token，解决 SSE 连接无法携带请求头的问题。
5. **降级策略**：当 SSE 连接失败时，使用模拟回复，确保用户体验。

## 6. 测试方法

1. 启动前端和后端服务
2. 打开首页，查看浏览器控制台，确认 SSE 连接成功
3. 发送消息，确认消息能够正常发送和接收
4. 查看后端控制台，确认连接状态和消息处理
5. 模拟网络断开，确认重连机制正常工作

## 7. 注意事项

1. **CORS 配置**：确保后端配置了正确的 CORS 策略，允许前端跨域请求。
2. **Token 安全性**：虽然 SSE 连接使用 URL 参数传递 token，但在生产环境中应考虑使用 HTTPS 加密传输。
3. **连接限制**：根据服务器性能，合理设置最大连接数，避免资源耗尽。
4. **心跳间隔**：根据网络环境，调整心跳间隔，平衡实时性和网络开销。

## 8. 后续优化

1. **连接池管理**：实现更高效的连接池管理，优化连接资源使用。
2. **消息队列**：引入消息队列，处理高并发场景下的消息积压。
3. **监控系统**：添加 SSE 连接监控，及时发现和处理异常。
4. **性能优化**：优化 SSE 连接处理逻辑，提高系统吞吐量。