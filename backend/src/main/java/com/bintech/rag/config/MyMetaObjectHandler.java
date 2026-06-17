package com.bintech.rag.config;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    private static final String UNKNOWN = "UNKNOWN";

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String userId = resolveUserId();

        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "creator", String.class, userId);
        this.strictInsertFill(metaObject, "modifier", String.class, userId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        String userId = resolveUserId();

        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "modifier", String.class, userId);
    }

    private String resolveUserId() {
        try {
            if (StpUtil.isLogin()) {
                return String.valueOf(StpUtil.getLoginIdAsLong());
            }
        } catch (Exception e) {
            log.warn("无法从会话上下文中获取用户ID，使用默认值: {}", UNKNOWN);
        }
        return UNKNOWN;
    }
}