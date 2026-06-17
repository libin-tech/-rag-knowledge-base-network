package com.bintech.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "pgvector")
public class PgVectorProperties {

    private String tableName = "vector_embeddings";

    private int dimension = 1024;

    private boolean createTable = true;

    private boolean useIndex = true;

    private int indexListSize = 100;
}
