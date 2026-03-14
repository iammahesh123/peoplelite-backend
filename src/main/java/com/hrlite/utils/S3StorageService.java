package com.hrlite.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@Service
@Slf4j
public class S3StorageService {

    private final S3Client s3Client;
    private final String bucketName;

    public S3StorageService(S3Client s3Client,
                            @Value("${app.storage.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    /**
     * Upload from InputStream with known content length.
     */
    public String upload(String tenantId, String folder, String fileName,
                         InputStream inputStream, long contentLength, String contentType) {
        String key = tenantId + "/" + folder + "/" + UUID.randomUUID() + "/" + sanitizeFileName(fileName);

        try {
            ensureBucketExists();

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(inputStream, contentLength));

            log.info("Uploaded file to S3: {}", key);
            return key;
        } catch (Exception e) {
            log.error("Failed to upload file to S3", e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    /**
     * Upload from byte array (convenience overload).
     */
    public String upload(String tenantId, String folder, String fileName,
                         byte[] data, String contentType) {
        return upload(tenantId, folder, fileName,
                new ByteArrayInputStream(data), data.length, contentType);
    }

    public byte[] download(String storageKey) {
        try {
            return s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(storageKey)
                            .build()).asByteArray();
        } catch (Exception e) {
            log.error("Failed to download file from S3: {}", storageKey, e);
            throw new RuntimeException("Failed to download file", e);
        }
    }

    public void delete(String storageKey) {
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(storageKey)
                            .build());
            log.info("Deleted file from S3: {}", storageKey);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", storageKey, e);
        }
    }

    /**
     * Check if a key exists in S3.
     */
    public boolean exists(String storageKey) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Failed to check file existence in S3: {}", storageKey, e);
            return false;
        }
    }

    private void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            log.info("Created S3 bucket: {}", bucketName);
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "unnamed-file";
        }
        return fileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
    }
}
