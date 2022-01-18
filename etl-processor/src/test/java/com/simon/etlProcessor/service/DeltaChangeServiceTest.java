package com.simon.etlProcessor.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.simon.etlProcessor.config.TestContext;
import com.simon.etlProcessor.enums.BucketName;

class DeltaChangeServiceTest {

    @Test
    void testCalculateDeltaChange() {

        File oldFile = new File("src/test/resources/FINAL201711.csv");
        File newFile = new File("src/test/resources/FINAL201712.csv");
         try {
             InputStream oldFileStream = new FileInputStream(oldFile);
             InputStream newFileStream = new FileInputStream(newFile);
             InputStreamReader oldFileStreamReader = new InputStreamReader(oldFileStream); 
             InputStreamReader newFileStreamReader = new InputStreamReader(newFileStream); 
             
             DeltaChangeService service = new DeltaChangeService();
             TestContext contect = new TestContext();
             LambdaLogger logger = contect.getLogger();
             String outputFileKey = service.calculateDeltaChange(newFileStreamReader, oldFileStreamReader, "c1", logger);

             S3Service s3Service = new S3Service();
             S3ObjectInputStream outputFile = s3Service.downloadS3File(outputFileKey, logger, BucketName.OUTPUT_BUCKET.getBucketName());
             assertNotEquals(outputFile, null);
                 
        } catch (Exception e) { // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


}
