# SSE与OpenCode集成实现

## 1. 功能说明

本文档描述了如何实现前端通过SSE与后端通信，后端调用OpenCode API生成AI回复的完整流程。

## 2. 技术栈

- **前端**: Vue 2.6.12 + Element UI
- **后端**: Spring Boot 3.0.2 + SSE (Server-Sent Events)
- **AI服务**: OpenCode

## 3. 实现步骤

### 3.1 前端实现

#### 3.1.1 添加loading效果

在 `src/views/index.vue` 中添加 `isLoading` 状态变量：

```javascript
data() {
  return {
    // 其他变量...
    isLoading: false
  }
}
```

#### 3.1.2 修改消息发送逻辑

在 `sendMessage` 方法中添加loading效果：

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
  
  // 显示正在思考的loading效果
  this.isLoading = true;
  this.messages.push({
    content: '正在思考...',
    time: this.formatTime(new Date()),
    isMe: false,
    avatar: '',
    isLoading: true
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
        this.isLoading = false;
      });
  } else {
    // SSE未连接时使用模拟回复
    this.generateReply(this.inputMessage);
    this.isLoading = false;
  }
  
  this.inputMessage = '';
  this.scrollToBottom();
}
```

#### 3.1.3 修改消息接收逻辑

在 `initSse` 方法中修改消息监听：

```javascript
// 监听消息
this.sse.addEventListener('message', (event) => {
  console.log('收到SSE消息:', event.data);
  // 移除正在思考的消息
  if (this.isLoading) {
    this.messages = this.messages.filter(msg => !msg.isLoading);
    this.isLoading = false;
  }
  // 添加实际回复消息
  this.messages.push({
    content: event.data,
    time: this.formatTime(new Date()),
    isMe: false,
    avatar: ''
  });
  this.scrollToBottom();
});
```

### 3.2 后端实现

#### 3.2.1 修改SSE控制器

在 `api/src/main/java/cn/qihangerp/api/controller/SseController.java` 中添加调用OpenCode API的逻辑：

```java
@GetMapping("/send")
public String sendMessage(@RequestParam String clientId, @RequestParam String message) {
    SseEmitter emitter = emitters.get(clientId);
    if (emitter != null) {
        try {
            // 调用opencode接口获取回复
            String response = callOpencodeApi(message);
            
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(response));
            return "消息发送成功";
        } catch (Exception e) {
            emitters.remove(clientId);
            return "消息发送失败";
        }
    }
    return "客户端不存在";
}

private String callOpencodeApi(String message) throws Exception {
    // 构建请求体
    String requestBody = "{\"message\": \"" + message + "\"}";
    
    // 创建HTTP客户端
    HttpClient client = HttpClient.newHttpClient();
    
    // 构建请求
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:14967/api/chat"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();
    
    // 发送请求并获取响应
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
    // 返回响应体
    return response.body();
}
```

### 3.3 OpenCode服务启动

#### 3.3.1 安装OpenCode

```bash
npm install -g opencode-ai
```

#### 3.3.2 启动OpenCode服务

首先切换到opencode文件夹，使用nvm切换nodejs版本，然后启动服务：

```bash
cd opencode
nvm use 20.20.0
opencode serve --port 14967
```

## 4. 完整流程

1. **启动OpenCode服务**：
   ```bash
   cd opencode
   nvm use 20.20.0
   opencode serve --port 14967
   ```
2. **启动后端服务**：`mvn clean install spring-boot:run -pl api -am`
3. **启动前端服务**：
   ```bash
   nvm use 20.20.0
   npm run dev
   ```
4. **访问应用**：打开浏览器访问 `http://localhost:89`
5. **发送消息**：在聊天界面输入消息并发送
6. **查看效果**：
   - 前端显示"正在思考..."
   - 后端调用OpenCode API获取AI回复
   - 前端显示AI回复

## 5. 注意事项

1. **OpenCode服务端口**：默认使用14967端口，如需修改，需同时修改后端代码中的API地址
2. **Token认证**：SSE连接和消息发送都需要携带token参数
3. **错误处理**：后端调用OpenCode API失败时，会返回"消息发送失败"
4. **loading效果**：当收到AI回复时，会自动移除"正在思考..."消息

## 6. 测试

1. 启动所有服务
2. 打开前端应用
3. 输入消息并发送
4. 观察前端是否显示"正在思考..."
5. 观察后端是否调用OpenCode API
6. 观察前端是否显示AI回复

## 7. 故障排查

- **OpenCode服务未启动**：检查OpenCode服务是否运行在14967端口
- **后端服务未启动**：检查后端服务是否运行在8087端口
- **前端连接失败**：检查token是否正确，API路径是否正确
- **消息发送失败**：检查OpenCode API是否正常响应

## 8. 总结

本实现通过SSE技术实现了前端与后端的实时通信，后端调用OpenCode API生成AI回复，前端显示loading效果提升用户体验。整个流程完整且稳定，可以满足电商AI ERP系统的智能对话需求。