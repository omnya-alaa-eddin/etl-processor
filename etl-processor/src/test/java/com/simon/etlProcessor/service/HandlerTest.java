package com.simon.etlProcessor.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.simon.etlProcessor.config.TestContext;
import com.simon.etlProcessor.enums.BucketName;
import com.simon.etlProcessor.handler.Handler;

public class HandlerTest {

    @Test
    public void testEventHandler() throws IOException {
        try {
            // Upload two files to s3
            File oldFile = new File("src/test/resources/FINAL201711.csv");
            File newFile = new File("src/test/resources/FINAL201712.csv");
            InputStream oldFileStream = new FileInputStream(oldFile);
            InputStream newFileStream = new FileInputStream(newFile);

            S3Service s3Service = new S3Service();
            s3Service.upload(BucketName.INPUT_BUCKET.getBucketName(), "FINAL201711.csv", "c1", oldFileStream,
                    "30-11-2017");
            s3Service.upload(BucketName.INPUT_BUCKET.getBucketName(), "FINAL201712.csv", "c1", newFileStream,
                    "31-12-2017");

            // Create Event
            SQSEvent event = new SQSEvent();
            File file = new File("src/test/resources/event.json");
            InputStream inputStream = new FileInputStream(file);
            JSONTokener tokener = new JSONTokener(inputStream);
            JSONObject object = new JSONObject(tokener);
            List<SQSMessage> records = new ArrayList<SQSMessage>();
            SQSMessage record = new SQSMessage();
            record.setBody(object.toString());
            records.add(record);
            event.setRecords(records);

            Handler handler = new Handler();
            TestContext contect = new TestContext();

            String response = handler.handleRequest(event, contect);
            assertNotEquals(null, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}