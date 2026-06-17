package com.bintech.rag.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PgVectorConfig {

    private final PgVectorProperties pgVectorProperties;

    private final DataSource dataSource;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table(pgVectorProperties.getTableName())
                .dimension(pgVectorProperties.getDimension())
                .createTable(pgVectorProperties.isCreateTable())
                .useIndex(pgVectorProperties.isUseIndex())
                .indexListSize(pgVectorProperties.getIndexListSize())
                .build();
    }
}
