package com.bin.ragknowledge.service;

import com.bin.ragknowledge.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class ObjectStorageService {

    private final MinioClient minioClient;

    private final MinioProperties minioProperties;

    @PostConstruct
    public void initBucket() throws Exception {
        boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build()
        );
        if (!bucketExists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build()
            );
        }
    }

    public void uploadFile(String objectKey, MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectKey)
                            .contentType(file.getContentType())
                            .stream(inputStream, file.getSize(), -1L)
                            .build()
            );
        }
    }

    public InputStream getFileStream(String objectKey) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .object(objectKey)
                        .build()
        );
    }

    public String getContentType(String objectKey) throws Exception {
        StatObjectResponse objectResponse = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .object(objectKey)
                        .build()
        );
        return objectResponse.contentType();
    }

    public void deleteFile(String objectKey) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .object(objectKey)
                        .build()
        );
    }
}
