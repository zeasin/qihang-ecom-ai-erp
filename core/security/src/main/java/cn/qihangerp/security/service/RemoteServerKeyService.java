package cn.qihangerp.security.service;

import cn.qihangerp.security.common.Constants;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 远程服务器密钥验证服务
 *
 * @author qihang
 */
@Service
@AllArgsConstructor
public class RemoteServerKeyService {

    private final Environment environment;
    private final RestTemplate restTemplate;

    /**
     * 验证远程服务器密钥是否有效
     *
     * @return 验证结果
     */
    public boolean validateRemoteServerKey() {
        // 先进行本地验证
        if (!validateLocalKey()) {
            return false;
        }

        // 再进行远程服务器验证
        return validateRemoteKey();
    }

    /**
     * 本地验证密钥
     *
     * @return 验证结果
     */
    private boolean validateLocalKey() {
        // 获取远程服务器密钥
        String remoteServerKey = environment.getProperty(Constants.REMOTE_SERVER_KEY);
        if (remoteServerKey == null || remoteServerKey.isEmpty()) {
            return false;
        }

        // 获取密钥过期时间
        String expireTimeStr = environment.getProperty(Constants.REMOTE_SERVER_KEY_EXPIRE);
        if (expireTimeStr == null || expireTimeStr.isEmpty()) {
            return false;
        }

        try {
            // 解析过期时间
            Date expireTime = new Date(Long.parseLong(expireTimeStr));
            // 检查是否过期
            return new Date().before(expireTime);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 远程服务器验证密钥
     *
     * @return 验证结果
     */
    private boolean validateRemoteKey() {
        try {
            // 获取远程服务器密钥
            String remoteServerKey = environment.getProperty(Constants.REMOTE_SERVER_KEY);
            if (remoteServerKey == null || remoteServerKey.isEmpty()) {
                return false;
            }

            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + remoteServerKey);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("key", remoteServerKey);
            requestBody.put("timestamp", System.currentTimeMillis());

            // 构建请求实体
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 发送请求到远程服务器
            // 注意：这里需要替换为实际的远程服务器验证地址
            String remoteAuthUrl = environment.getProperty("REMOTE_AUTH_SERVER_URL", "https://your-auth-server.com/api/validate");
            ResponseEntity<Map> response = restTemplate.exchange(
                    remoteAuthUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // 处理响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && Boolean.TRUE.equals(responseBody.get("valid"))) {
                    // 验证过期时间是否一致
                    Long serverExpireTime = (Long) responseBody.get("expireTime");
                    if (serverExpireTime != null) {
                        String localExpireTimeStr = environment.getProperty(Constants.REMOTE_SERVER_KEY_EXPIRE);
                        if (localExpireTimeStr != null) {
                            Long localExpireTime = Long.parseLong(localExpireTimeStr);
                            // 允许一定的时间误差（例如1分钟）
                            return Math.abs(serverExpireTime - localExpireTime) < 60000;
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            // 网络异常等情况，返回验证失败
            return false;
        }
    }

    /**
     * 获取远程服务器密钥
     *
     * @return 远程服务器密钥
     */
    public String getRemoteServerKey() {
        return environment.getProperty(Constants.REMOTE_SERVER_KEY);
    }

    /**
     * 获取远程服务器密钥过期时间
     *
     * @return 过期时间
     */
    public Date getRemoteServerKeyExpireTime() {
        String expireTimeStr = environment.getProperty(Constants.REMOTE_SERVER_KEY_EXPIRE);
        if (expireTimeStr != null && !expireTimeStr.isEmpty()) {
            try {
                return new Date(Long.parseLong(expireTimeStr));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 检查系统是否已授权
     *
     * @return 是否已授权
     */
    public boolean isAuthorized() {
        return validateRemoteServerKey();
    }
}
