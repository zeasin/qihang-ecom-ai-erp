package cn.qihangerp.api.ai;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
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
        log.info("=============来新消息了！");
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
        // 创建HTTP客户端
        HttpClient client = HttpClient.newHttpClient();
        
        // 1. 创建新会话
        HttpRequest createSessionRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:14967/session"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();
        
        HttpResponse<String> createSessionResponse = client.send(createSessionRequest, HttpResponse.BodyHandlers.ofString());
        String sessionId = parseSessionId(createSessionResponse.body());
        
        // 2. 向会话发送消息
        String requestBody = "{\"parts\": [{\"type\": \"text\", \"text\": \"" + message + "\"}]}";
        HttpRequest sendMessageRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:14967/session/" + sessionId + "/message"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        // 发送请求并获取响应
        HttpResponse<String> response = client.send(sendMessageRequest, HttpResponse.BodyHandlers.ofString());
        
        // 解析响应，提取AI回复
        return parseAIResponse(response.body());
    }
    
    private String parseSessionId(String responseBody) {
        // 简单解析JSON，提取sessionId
        // 实际项目中建议使用JSON库
        int idIndex = responseBody.indexOf("\"id\":\"");
        if (idIndex != -1) {
            int start = idIndex + 6;
            int end = responseBody.indexOf("\"", start);
            if (end != -1) {
                return responseBody.substring(start, end);
            }
        }
        return "";
    }
    
    private String parseAIResponse(String responseBody) {
        log.info("=================AI回复==========");
        log.info(responseBody);
        // 简单解析JSON，提取AI回复
        JSONObject jsonObject = JSONObject.parseObject(responseBody);
        JSONArray jsonArray = jsonObject.getJSONArray("parts");

        return jsonArray.getJSONObject(2).getString("text");
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