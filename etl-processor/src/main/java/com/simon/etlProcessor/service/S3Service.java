package com.simon.etlProcessor.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public class S3Service {

    public AmazonS3 getAwsS3() {
        AWSCredentials awsCredentials = new BasicAWSCredentials("", "");

        AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard().withRegion("ca-central-1")
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();
        return amazonS3;

    }

    public S3ObjectInputStream downloadS3File(String key, LambdaLogger logger, String bucketName) {
        try {
            logger.log("---inside download method---");
            S3Object object = getAwsS3()
                    .getObject(new GetObjectRequest(bucketName, key));
            if (object != null) {
                logger.log("--file with key :" + key + "is downloaded");

            }
            return object.getObjectContent();

        } catch (Exception e) {
            logger.log("Failed to download the file due to : " + e.toString());
            throw new IllegalStateException("Failed to download the file", e);
        }
    }
}
