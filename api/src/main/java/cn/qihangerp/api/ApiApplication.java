package cn.qihangerp.api;

import cn.qihangerp.security.service.RemoteServerKeyService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@ComponentScan(basePackages={"cn.qihangerp"})
//@MapperScan("cn.qihangerp.sys.api.mapper")
@SpringBootApplication(scanBasePackages = {"cn.qihangerp.api"})
public class ApiApplication
{
    public static void main( String[] args )
    {
        System.out.println( "Hello qihangerp!" );
        SpringApplication.run(ApiApplication.class, args);
        // 启动前验证远程服务器密钥
//        SpringApplication app = new SpringApplication(ApiApplication.class);
//        var context = app.run(args);
//        RemoteServerKeyService remoteServerKeyService = context.getBean(RemoteServerKeyService.class);
//        if (!remoteServerKeyService.isAuthorized()) {
//            System.err.println("远程服务器密钥验证失败或已过期，系统将关闭！");
//            System.exit(1);
//        }
//        System.out.println("远程服务器密钥验证成功，系统启动正常！");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
