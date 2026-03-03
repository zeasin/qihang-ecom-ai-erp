//package cn.qihangerp.security.task;
//
//import cn.qihangerp.security.service.RemoteServerKeyService;
//import lombok.AllArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
///**
// * 远程服务器密钥定时验证任务
// *
// * @author qihang
// */
//@Component
//@AllArgsConstructor
//public class RemoteServerKeyValidationTask {
//
//    private static final Logger logger = LoggerFactory.getLogger(RemoteServerKeyValidationTask.class);
//    private final RemoteServerKeyService remoteServerKeyService;
//
//    /**
//     * 定时验证远程服务器密钥
//     * 每小时验证一次
//     */
//    @Scheduled(fixedRate = 3600000) // 1小时 = 3600000毫秒
//    public void validateRemoteServerKey() {
//        if (!remoteServerKeyService.isAuthorized()) {
//            logger.error("远程服务器密钥验证失败，系统将关闭！");
//            // 关闭程序
//            System.exit(1);
//        }
//
//        logger.info("远程服务器密钥验证成功");
//    }
//}
