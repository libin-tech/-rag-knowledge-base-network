package com.bin.ragknowledge.controller;

import com.bin.ragknowledge.enums.ChannelType;
import com.bin.ragknowledge.repository.entity.MessageChannelEntity;
import com.bin.ragknowledge.service.MessageChannelService;
import com.bin.ragknowledge.service.feishu.FeishuEventListener;
import com.bin.ragknowledge.service.dingtalk.DingtalkEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/api/channel")
@RequiredArgsConstructor
public class MessageChannelController {

    private final MessageChannelService messageChannelService;
    private final FeishuEventListener feishuEventListener;
    private final DingtalkEventListener dingtalkEventListener;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list() {
        List<MessageChannelEntity> list = messageChannelService.listAll();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", list
        ));
    }

    @GetMapping("/{channelType}")
    public ResponseEntity<Map<String, Object>> getByType(@PathVariable String channelType) {
        var channel = messageChannelService.getByType(channelType);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", channel
        ));
    }

    @PutMapping("/{channelType}")
    public ResponseEntity<Map<String, Object>> updateChannel(
            @PathVariable String channelType,
            @RequestBody ChannelUpdateRequest request) {
        var entity = new MessageChannelEntity();
        entity.setChannelName(request.getChannelName());
        entity.setEnabled(request.getEnabled());
        entity.setConfigJson(request.getConfigJson());
        entity.setRemark(request.getRemark());
        entity.setModifier(request.getModifier());

        messageChannelService.updateChannel(channelType, entity);

        if (ChannelType.FEISHU.equals(channelType)) {
            feishuEventListener.restart();
        } else if (ChannelType.DINGTALK.equals(channelType)) {
            dingtalkEventListener.restart();
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "渠道配置更新成功，监听器已重启"
        ));
    }

    @lombok.Data
    public static class ChannelUpdateRequest {
        private String channelName;
        private Boolean enabled;
        private String configJson;
        private String remark;
        private String modifier;
    }
}