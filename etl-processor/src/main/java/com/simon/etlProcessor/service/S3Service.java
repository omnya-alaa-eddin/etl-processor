package com.simon.etlProcessor.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.simon.etlProcessor.config.DatabaseConnection;

public class S3Service {

    public AmazonS3 getAwsS3() {
        try {
            InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("config.properties");
            Properties prop = new Properties();
            prop.load(input);
            String accessKey = prop.getProperty("accessKey");
            String secretKet = prop.getProperty("secretKet");
            String region = prop.getProperty("region");
            AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKet);

            AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard().withRegion(region)
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();
            return amazonS3;

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public S3ObjectInputStream downloadS3File(String key, LambdaLogger logger, String bucketName) {
        try {
            logger.log("---inside download method---");
            S3Object object = getAwsS3().getObject(new GetObjectRequest(bucketName, key));
            if (object != null) {
                logger.log("--file with key :" + key + "is downloaded");

            }
            return object.getObjectContent();

        } catch (Exception e) {
            logger.log("Failed to download the file due to : " + e.toString());
            throw new IllegalStateException("Failed to download the file", e);
        }
    }

    public void upload(String path, String fileName, String tenant, InputStream inputStream, String date) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("x-amz-meta-tenant-id", tenant);
        metadata.put("x-amz-meta-date", date);
        objectMetadata.setUserMetadata(metadata);

        try {
            getAwsS3().putObject(path, tenant + "/" + fileName, inputStream, objectMetadata);
        } catch (AmazonServiceException e) {
            throw new IllegalStateException("Failed to upload the file", e);
        }
    }
}
