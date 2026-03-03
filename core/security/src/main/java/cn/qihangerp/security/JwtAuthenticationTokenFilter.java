package cn.qihangerp.security;


import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.enums.HttpStatus;
import com.alibaba.fastjson2.JSON;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * token过滤器 验证token有效性
 * 
 * @author qihang
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    private TokenService tokenService;
    /**
     * 需要拦截的请求头信息
     */
    @Value("${token.header:'Authorization'}")
    public String TOKEN_HEADER = "Authorization";
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String url = request.getRequestURI();
        log.info("intercept " + url);
        if (request.getRequestURI().equals("/login")
                || request.getRequestURI().contains("/login")
                || request.getRequestURI().contains("/captchaImage")
                || request.getRequestURI().equals("/order/get_detail")
                || request.getRequestURI().equals("/refund/get_detail")
                || request.getRequestURI().contains("/system/config")
                || request.getRequestURI().contains("/swagger-ui")
                || request.getRequestURI().contains("/v3/api-docs")
                || request.getRequestURI().contains("/api/sse/")
        ) {
            // 登录页面，放行
            chain.doFilter(request, response);
            return;
        }
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (loginUser != null) {
            tokenService.verifyToken(loginUser);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            chain.doFilter(request, response);
        } else {
            // 对于SSE连接，不要尝试写入错误信息，因为响应已经开始发送
            if (url.contains("/api/sse/connect")) {
                // 直接返回，让SSE连接自然断开
                return;
            } else {
                fallback(" 授权过期！", response);
                return;
            }
        }
    }

    private void fallback(String message, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        PrintWriter writer = null;
        try {
            if (message == null) {
                message = "401 Forbidden";
            }
            AjaxResult res = AjaxResult.error(HttpStatus.UNAUTHORIZED, message);
            writer = response.getWriter();
            writer.append(JSON.toJSONString(res));
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
