package com.bintech.rag.service;

import com.bintech.rag.enums.ChannelType;
import com.bintech.rag.repository.entity.MessageChannelEntity;
import com.bintech.rag.repository.mapper.MessageChannelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageChannelService 单元测试")
class MessageChannelServiceTest {

    @Mock
    private MessageChannelMapper messageChannelMapper;

    @InjectMocks
    private MessageChannelService messageChannelService;

    private MessageChannelEntity testEntity;
    private static final String TEST_KB_ID = "test-knowledge-base-001";
    private static final ChannelType TEST_CHANNEL_TYPE = ChannelType.FEISHU;
    private static final String TEST_MODIFIER = "admin";

    @BeforeEach
    void setUp() {
        testEntity = new MessageChannelEntity();
        testEntity.setChannelName("飞书机器人");
        testEntity.setEnabled(true);
        testEntity.setConfigJson("{\"appId\":\"test\",\"appSecret\":\"test\"}");
        testEntity.setRemark("测试备注");
        testEntity.setModifier(TEST_MODIFIER);
    }

    @Nested
    @DisplayName("saveOrUpdateChannel 新增场景测试")
    class SaveOrUpdateChannelInsertTest {

        @Test
        @DisplayName("当渠道不存在时应创建新记录")
        void whenChannelNotExists_shouldInsertNewRecord() {
            when(messageChannelMapper.selectOne(any())).thenReturn(null);
            when(messageChannelMapper.insert(any(MessageChannelEntity.class))).thenReturn(1);

            messageChannelService.saveOrUpdateChannel(TEST_CHANNEL_TYPE, TEST_KB_ID, testEntity);

            ArgumentCaptor<MessageChannelEntity> captor = ArgumentCaptor.forClass(MessageChannelEntity.class);
            verify(messageChannelMapper).insert(captor.capture());

            MessageChannelEntity inserted = captor.getValue();
            assertNotNull(inserted.getId(), "插入时ID不应为空");
            assertEquals(ChannelType.FEISHU, inserted.getChannelType(), "渠道类型应匹配");
            assertEquals(TEST_KB_ID, inserted.getKnowledgeBaseId(), "知识库ID应匹配");
            assertEquals(testEntity.getChannelName(), inserted.getChannelName());
            assertEquals(testEntity.getEnabled(), inserted.getEnabled());
            assertEquals(testEntity.getConfigJson(), inserted.getConfigJson());
            assertNotNull(inserted.getCreateTime(), "创建时间不应为空");
            assertNotNull(inserted.getUpdateTime(), "更新时间不应为空");
            assertEquals(TEST_MODIFIER, inserted.getModifier());
        }

        @Test
        @DisplayName("当渠道不存在时应使用modifier作为creator")
        void whenChannelNotExists_shouldUseModifierAsCreator() {
            when(messageChannelMapper.selectOne(any())).thenReturn(null);
            when(messageChannelMapper.insert(any(MessageChannelEntity.class))).thenReturn(1);

            testEntity.setCreator(null);
            testEntity.setModifier(TEST_MODIFIER);

            messageChannelService.saveOrUpdateChannel(TEST_CHANNEL_TYPE, TEST_KB_ID, testEntity);

            ArgumentCaptor<MessageChannelEntity> captor = ArgumentCaptor.forClass(MessageChannelEntity.class);
            verify(messageChannelMapper).insert(captor.capture());

            MessageChannelEntity inserted = captor.getValue();
            assertEquals(TEST_MODIFIER, inserted.getCreator(), "creator应使用modifier的值");
            assertEquals(TEST_MODIFIER, inserted.getModifier());
        }

        @Test
        @DisplayName("当渠道不存在且modifier为空时应使用system作为creator")
        void whenChannelNotExistsAndModifierNull_shouldUseSystemAsCreator() {
            when(messageChannelMapper.selectOne(any())).thenReturn(null);
            when(messageChannelMapper.insert(any(MessageChannelEntity.class))).thenReturn(1);

            testEntity.setCreator(null);
            testEntity.setModifier(null);

            messageChannelService.saveOrUpdateChannel(TEST_CHANNEL_TYPE, TEST_KB_ID, testEntity);

            ArgumentCaptor<MessageChannelEntity> captor = ArgumentCaptor.forClass(MessageChannelEntity.class);
            verify(messageChannelMapper).insert(captor.capture());

            MessageChannelEntity inserted = captor.getValue();
            assertEquals("system", inserted.getCreator(), "creator应为system");
        }

        @Test
        @DisplayName("当渠道不存在时应生成唯一ID")
        void whenChannelNotExists_shouldGenerateUniqueId() {
            when(messageChannelMapper.selectOne(any())).thenReturn(null);
            when(messageChannelMapper.insert(any(MessageChannelEntity.class))).thenReturn(1);

            messageChannelService.saveOrUpdateChannel(TEST_CHANNEL_TYPE, TEST_KB_ID, testEntity);

            ArgumentCaptor<MessageChannelEntity> captor = ArgumentCaptor.forClass(MessageChannelEntity.class);
            verify(messageChannelMapper).insert(captor.capture());

            assertNotNull(captor.getValue().getId());
            assertFalse(captor.getValue().getId().isEmpty());
        }
    }

    @Nested
    @DisplayName("saveOrUpdateChannel 更新场景测试")
    class SaveOrUpdateChannelUpdateTest {

        @Test
        @DisplayName("当渠道已存在时应更新记录")
        void whenChannelExists_shouldUpdateRecord() {
            MessageChannelEntity existingEntity = new MessageChannelEntity();
            existingEntity.setId("existing-id-123");
            existingEntity.setChannelType(ChannelType.FEISHU);
            existingEntity.setKnowledgeBaseId(TEST_KB_ID);
            existingEntity.setChannelName("旧名称");
            existingEntity.setEnabled(false);
            existingEntity.setCreateTime(LocalDateTime.now().minusDays(1));
            existingEntity.setCreator("original-creator");

            when(messageChannelMapper.selectOne(any())).thenReturn(existingEntity);
            when(messageChannelMapper.updateById(any(MessageChannelEntity.class))).thenReturn(1);

            messageChannelService.saveOrUpdateChannel(TEST_CHANNEL_TYPE, TEST_KB_ID, testEntity);

            ArgumentCaptor<MessageChannelEntity> captor = ArgumentCaptor.forClass(MessageChannelEntity.class);
            verify(messageChannelMapper).updateById(captor.capture());

            MessageChannelEntity updated = captor.getValue();
            assertEquals("existing-id-123", updated.getId(), "应保留原有ID");
            assertEquals(ChannelType.FEISHU, updated.getChannelType(), "渠道类型应匹配");
            assertEquals(testEntity.getChannelName(), updated.getChannelName(), "名称应更新");
            assertEquals(testEntity.getEnabled(), updated.getEnabled(), "启用状态应更新");
            assertEquals(testEntity.getConfigJson(), updated.getConfigJson(), "配置应更新");
            assertNotNull(updated.getUpdateTime(), "更新时间不应为空");
        }

        @Test
        @DisplayName("当渠道已存在时应保留原创建时间")
        void whenChannelExists_shouldPreserveCreateTime() {
            LocalDateTime originalCreateTime = LocalDateTime.now().minusDays(1);
            MessageChannelEntity existingEntity = new MessageChannelEntity();
            existingEntity.setId("existing-id-123");
            existingEntity.setChannelType(ChannelType.FEISHU);
            existingEntity.setKnowledgeBaseId(TEST_KB_ID);
            existingEntity.setCreateTime(originalCreateTime);
            existingEntity.setCreator("original-creator");

            when(messageChannelMapper.selectOne(any())).thenReturn(existingEntity);
            when(messageChannelMapper.updateById(any(MessageChannelEntity.class))).thenReturn(1);

            messageChannelService.saveOrUpdateChannel(TEST_CHANNEL_TYPE, TEST_KB_ID, testEntity);

            ArgumentCaptor<MessageChannelEntity> captor = ArgumentCaptor.forClass(MessageChannelEntity.class);
            verify(messageChannelMapper).updateById(captor.capture());

            assertEquals(originalCreateTime, captor.getValue().getCreateTime(), "创建时间应保持不变");
            assertEquals("original-creator", captor.getValue().getCreator(), "创建人应保持不变");
        }
    }

    @Nested
    @DisplayName("saveOrUpdateChannel 参数校验测试")
    class SaveOrUpdateChannelValidationTest {

        @Test
        @DisplayName("当渠道类型为空时应抛出IllegalArgumentException")
        void whenChannelTypeIsNull_shouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> messageChannelService.saveOrUpdateChannel(null, TEST_KB_ID, testEntity)
            );
            assertEquals("渠道类型不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("当知识库ID为空时应抛出IllegalArgumentException")
        void whenKnowledgeBaseIdIsNull_shouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> messageChannelService.saveOrUpdateChannel(TEST_CHANNEL_TYPE, null, testEntity)
            );
            assertEquals("知识库ID不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("当知识库ID为空字符串时应抛出IllegalArgumentException")
        void whenKnowledgeBaseIdIsEmpty_shouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> messageChannelService.saveOrUpdateChannel(TEST_CHANNEL_TYPE, "", testEntity)
            );
            assertEquals("知识库ID不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("当实体为空时应抛出IllegalArgumentException")
        void whenEntityIsNull_shouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> messageChannelService.saveOrUpdateChannel(TEST_CHANNEL_TYPE, TEST_KB_ID, null)
            );
            assertEquals("渠道实体不能为空", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("updateChannel 原有方法测试")
    class UpdateChannelOriginalMethodTest {

        @Test
        @DisplayName("当记录存在时应更新成功")
        void whenRecordExists_shouldUpdateSuccessfully() {
            when(messageChannelMapper.update(any(MessageChannelEntity.class), any())).thenReturn(1);

            assertDoesNotThrow(() ->
                    messageChannelService.updateChannel(TEST_CHANNEL_TYPE, TEST_KB_ID, testEntity)
            );

            verify(messageChannelMapper).update(any(MessageChannelEntity.class), any());
        }

        @Test
        @DisplayName("当记录不存在时应记录警告日志")
        void whenRecordNotExists_shouldLogWarning() {
            when(messageChannelMapper.update(any(MessageChannelEntity.class), any())).thenReturn(0);

            assertDoesNotThrow(() ->
                    messageChannelService.updateChannel(TEST_CHANNEL_TYPE, TEST_KB_ID, testEntity)
            );
        }

        @Test
        @DisplayName("当数据库操作失败时应抛出RuntimeException")
        void whenDatabaseError_shouldThrowRuntimeException() {
            when(messageChannelMapper.update(any(MessageChannelEntity.class), any()))
                    .thenThrow(new RuntimeException("数据库连接失败"));

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> messageChannelService.updateChannel(TEST_CHANNEL_TYPE, TEST_KB_ID, testEntity)
            );
            assertTrue(exception.getMessage().contains("数据库连接失败"));
        }
    }

    @Nested
    @DisplayName("initChannelsForKnowledgeBase 测试")
    class InitChannelsForKnowledgeBaseTest {

        @Test
        @DisplayName("初始化时应为知识库创建三种渠道")
        void shouldCreateThreeChannelTypes() {
            when(messageChannelMapper.delete(any())).thenReturn(0);
            when(messageChannelMapper.insert(any(MessageChannelEntity.class))).thenReturn(1);

            messageChannelService.initChannelsForKnowledgeBase(TEST_KB_ID, TEST_MODIFIER);

            verify(messageChannelMapper, times(3)).insert(any(MessageChannelEntity.class));
        }

        @Test
        @DisplayName("初始化时渠道应默认禁用")
        void createdChannelsShouldBeDisabledByDefault() {
            when(messageChannelMapper.delete(any())).thenReturn(0);
            ArgumentCaptor<MessageChannelEntity> captor = ArgumentCaptor.forClass(MessageChannelEntity.class);
            when(messageChannelMapper.insert(captor.capture())).thenReturn(1);

            messageChannelService.initChannelsForKnowledgeBase(TEST_KB_ID, TEST_MODIFIER);

            for (MessageChannelEntity inserted : captor.getAllValues()) {
                assertFalse(inserted.getEnabled(), "渠道默认应该禁用");
            }
        }

        @Test
        @DisplayName("当知识库ID为空时应抛出异常")
        void whenKnowledgeBaseIdIsNull_shouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> messageChannelService.initChannelsForKnowledgeBase(null, TEST_MODIFIER)
            );
            assertEquals("知识库ID不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("当创建人为空时应抛出异常")
        void whenCreatorIsNull_shouldThrowException() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> messageChannelService.initChannelsForKnowledgeBase(TEST_KB_ID, null)
            );
            assertEquals("创建人不能为空", exception.getMessage());
        }
    }
}
