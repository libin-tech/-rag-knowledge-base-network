create table document_metadata
(
    id                varchar(64)                         not null
        constraint pk_document_metadata
            primary key,
    filename          varchar(255)                        not null,
    content_type      varchar(100)                        not null,
    file_size         bigint                              not null,
    object_key        varchar(255)                        not null
        constraint uk_object_key
            unique,
    vector_doc_id     varchar(64)                         not null,
    segment_count     integer   default 0                 not null,
    vector_ids        text                                not null,
    upload_time       timestamp                           not null,
    create_time       timestamp default CURRENT_TIMESTAMP not null,
    update_time       timestamp default CURRENT_TIMESTAMP not null,
    creator           varchar(64),
    modifier          varchar(64),
    version           integer   default 0                 not null,
    knowledge_base_id varchar(36)
);

comment on table document_metadata is '文档元数据表，存储已上传文档的基本信息和向量关联数据';

comment on column document_metadata.id is '文档唯一标识符(UUID)，主键';

comment on column document_metadata.filename is '文件名，包含文件扩展名，如：技术文档.pdf';

comment on column document_metadata.content_type is '文件的MIME类型，如：application/pdf';

comment on column document_metadata.file_size is '文件大小，单位为字节';

comment on column document_metadata.object_key is '对象存储键，格式为：documents/{uuid}/{filename}';

comment on column document_metadata.vector_doc_id is '文档在向量数据库（如Milvus）中的文档ID';

comment on column document_metadata.segment_count is '文档被分割成的文本块（chunk）数量';

comment on column document_metadata.vector_ids is '各分块在向量数据库中的ID列表，JSON数组格式';

comment on column document_metadata.upload_time is '文件上传到系统的时间';

comment on column document_metadata.create_time is '记录创建时间，由数据库自动维护';

comment on column document_metadata.update_time is '记录最后更新时间，由数据库自动维护';

comment on column document_metadata.creator is '记录创建者ID';

comment on column document_metadata.modifier is '记录最后修改者ID';

comment on column document_metadata.version is '乐观锁版本号，用于并发控制';

alter table document_metadata
    owner to postgres;

create index idx_upload_time
    on document_metadata (upload_time desc);

create index idx_vector_doc_id
    on document_metadata (vector_doc_id);

create index idx_document_metadata_kb_id
    on document_metadata (knowledge_base_id);

create table llm_config
(
    id           varchar(64)                         not null
        constraint pk_llm_config
            primary key,
    config_type  varchar(32)                         not null,
    config_key   varchar(64)                         not null,
    config_value text,
    remark       varchar(255),
    create_time  timestamp default CURRENT_TIMESTAMP not null,
    update_time  timestamp default CURRENT_TIMESTAMP not null,
    creator      varchar(64),
    modifier     varchar(64),
    version      integer   default 0                 not null,
    enabled      boolean   default false             not null,
    constraint uk_config_type_key
        unique (config_type, config_key)
);

comment on table llm_config is 'LLM和Embedding配置表，存储模型配置信息，支持后台管理和实时生效';

comment on column llm_config.id is '配置唯一标识符(UUID)，主键';

comment on column llm_config.config_type is '配置类型: LLM(大语言模型) 或 EMBEDDING(嵌入模型)';

comment on column llm_config.config_key is '配置键: mode、apiKey、baseUrl、modelName、timeout 等';

comment on column llm_config.config_value is '配置值';

comment on column llm_config.remark is '备注说明';

comment on column llm_config.create_time is '记录创建时间，由数据库自动维护';

comment on column llm_config.update_time is '记录最后更新时间，由数据库自动维护';

comment on column llm_config.creator is '记录创建者ID';

comment on column llm_config.modifier is '记录最后修改者ID';

comment on column llm_config.version is '乐观锁版本号，用于并发控制';

comment on column llm_config.enabled is '启用状态，true-启用，false-停用，同类型只能启用一个';

alter table llm_config
    owner to postgres;

create index idx_llm_config_type
    on llm_config (config_type);

create table message_channel
(
    id                varchar(64)                         not null
        constraint pk_message_channel
            primary key,
    channel_type      varchar(32)                         not null,
    channel_name      varchar(64)                         not null,
    enabled           boolean   default true              not null,
    config_json       text,
    remark            varchar(255),
    create_time       timestamp default CURRENT_TIMESTAMP not null,
    update_time       timestamp default CURRENT_TIMESTAMP not null,
    creator           varchar(64),
    modifier          varchar(64),
    version           integer   default 0                 not null,
    knowledge_base_id varchar(36),
    constraint uk_channel_type_kb
        unique (channel_type, knowledge_base_id)
);

comment on table message_channel is '消息渠道配置表，存储各平台机器人配置';

comment on column message_channel.id is '渠道唯一标识符(UUID)，主键';

comment on column message_channel.channel_type is '渠道类型: FEISHU(飞书)、DINGTALK(钉钉)、WECHAT_WORK(企业微信)';

comment on column message_channel.channel_name is '渠道名称';

comment on column message_channel.enabled is '是否启用';

comment on column message_channel.config_json is '渠道配置JSON';

comment on column message_channel.remark is '备注说明';

comment on column message_channel.create_time is '记录创建时间';

comment on column message_channel.update_time is '记录最后更新时间';

comment on column message_channel.creator is '记录创建者ID';

comment on column message_channel.modifier is '记录最后修改者ID';

comment on column message_channel.version is '乐观锁版本号';

comment on column message_channel.knowledge_base_id is '关联的知识库ID，每个知识库可以有独立的渠道配置';

alter table message_channel
    owner to postgres;

create index idx_message_channel_type
    on message_channel (channel_type);

create index idx_message_channel_enabled
    on message_channel (enabled);

create index idx_message_channel_kb_id
    on message_channel (knowledge_base_id);

create index idx_message_channel_kb
    on message_channel (knowledge_base_id);

create table knowledge_base
(
    id          varchar(36)  not null
        primary key,
    name        varchar(100) not null,
    description varchar(500),
    enabled     boolean   default true,
    create_time timestamp default CURRENT_TIMESTAMP,
    update_time timestamp default CURRENT_TIMESTAMP,
    creator     varchar(50),
    modifier    varchar(50),
    version     integer   default 0
);

alter table knowledge_base
    owner to postgres;

create index idx_knowledge_base_enabled
    on knowledge_base (enabled);

INSERT INTO "knowledge_base" (id,name,description,enabled,create_time,update_time,creator,modifier,version) VALUES ('default','默认知识库','系统默认知识库',true,'2026-04-30 02:36:35.123051','2026-04-30 16:40:14.332301','admin','admin','0');


