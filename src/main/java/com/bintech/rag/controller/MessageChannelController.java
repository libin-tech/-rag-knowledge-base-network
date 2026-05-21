package com.bintech.rag.controller;

import java.util.List;
import java.util.Map;

import cn.hutool.core.util.EnumUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bintech.rag.enums.ChannelType;
import com.bintech.rag.repository.entity.MessageChannelEntity;
import com.bintech.rag.service.MessageChannelService;
import com.bintech.rag.service.dingtalk.DingtalkEventListener;
import com.bintech.rag.service.dingtalk.DingtalkMessageSender;
import com.bintech.rag.service.feishu.FeishuEventListener;
import com.bintech.rag.service.feishu.FeishuMessageSender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/admin/api/channel")
@RequiredArgsConstructor
public class MessageChannelController {

    private final MessageChannelService messageChannelService;
    private final FeishuEventListener feishuEventListener;
    private final DingtalkEventListener dingtalkEventListener;
    private final FeishuMessageSender feishuMessageSender;
    private final DingtalkMessageSender dingtalkMessageSender;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(@RequestParam(required = false) String knowledgeBaseId) {
        List<MessageChannelEntity> list;
        if (knowledgeBaseId != null && !knowledgeBaseId.isEmpty()) {
            list = messageChannelService.listByKnowledgeBaseId(knowledgeBaseId);
        } else {
            list = messageChannelService.listAll();
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", list
        ));
    }

    @GetMapping("/{channelType}")
    public ResponseEntity<Map<String, Object>> getByType(@PathVariable ChannelType channelType) {
        var channel = messageChannelService.getByType(channelType);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", channel
        ));
    }

    @PutMapping("/{channelType}")
    public ResponseEntity<Map<String, Object>> updateChannel(
            @PathVariable ChannelType channelType,
            @RequestBody ChannelUpdateRequest request) {
        try {
            String knowledgeBaseId = request.getKnowledgeBaseId();



            var entity = new MessageChannelEntity();
            entity.setChannelName(request.getChannelName());
            entity.setEnabled(request.getEnabled());
            entity.setConfigJson(request.getConfigJson());
            entity.setRemark(request.getRemark());
            entity.setModifier(request.getModifier());



            entity.setChannelType(channelType);
            entity.setChannelName(channelType.getDesc());

            messageChannelService.saveOrUpdateChannel(channelType, knowledgeBaseId, entity);

            if (ChannelType.FEISHU.equals(channelType)) {
                feishuMessageSender.refreshConfig(knowledgeBaseId);
                feishuEventListener.restartForKnowledgeBase(knowledgeBaseId);
            } else if (ChannelType.DINGTALK.equals(channelType)) {
                dingtalkMessageSender.refreshConfig(knowledgeBaseId);
                dingtalkEventListener.restartForKnowledgeBase(knowledgeBaseId);
            }


            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "渠道配置保存成功，监听器已重启"
            ));
        } catch (IllegalArgumentException e) {
            log.warn("保存消息渠道配置失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("保存消息渠道配置失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "保存失败: " + e.getMessage()
            ));
        }
    }

    public static class ChannelUpdateRequest {
        private String knowledgeBaseId;
        private String channelName;
        private Boolean enabled;
        private String configJson;
        private String remark;
        private String modifier;

        public String getKnowledgeBaseId() { return knowledgeBaseId; }
        public void setKnowledgeBaseId(String knowledgeBaseId) { this.knowledgeBaseId = knowledgeBaseId; }
        public String getChannelName() { return channelName; }
        public void setChannelName(String channelName) { this.channelName = channelName; }
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        public String getConfigJson() { return configJson; }
        public void setConfigJson(String configJson) { this.configJson = configJson; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public String getModifier() { return modifier; }
        public void setModifier(String modifier) { this.modifier = modifier; }
    }
}
