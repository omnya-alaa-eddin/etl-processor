package com.simon.etlProcessor.handler;

import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.simon.etlProcessor.enums.BucketName;
import com.simon.etlProcessor.service.DeltaChangeService;
import com.simon.etlProcessor.service.S3Service;
import com.simon.etlProcessor.service.SQSService;
import com.simon.etlProcessor.util.DateUtil;

public class Handler implements RequestHandler<SQSEvent, String> {

    static String client = null;
    S3Service s3Service = new S3Service();
    SQSService sqsService = new SQSService();

    @Override
    public String handleRequest(SQSEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        String response = "success";
        try {
            for (SQSMessage msg : event.getRecords()) {

                // get s3 input info then get metadata then get date
                String s3Key = sqsService.getS3Key(msg.getBody());
                logger.log("New file has been uploaded with s3 key : " + s3Key);
                logger.log("will check if previuos month exists");
                // search during s3 files by previous month
                if (isLastMonthExists(s3Key)) {
                    logger.log("Previuos month exists and will download files");
                    // Process two files to get delta changes
                    S3ObjectInputStream newMonth = s3Service.downloadS3File(s3Key, logger,
                            BucketName.INPUT_BUCKET.getBucketName());
                    S3ObjectInputStream oldMonth = s3Service.downloadS3File(getLastMonthKey(s3Key), logger,
                            BucketName.INPUT_BUCKET.getBucketName());
                    DeltaChangeService service = new DeltaChangeService();
                    InputStreamReader newFileStreamReader = new InputStreamReader(newMonth);
                    InputStreamReader oldFileStreamReader = new InputStreamReader(oldMonth);
                    service.calculateDeltaChange(newFileStreamReader, oldFileStreamReader, client, logger);

                }
                // if month not found
                else {
                    logger.log("Previuos month doesn't exist and will reminate");
                    response = "Previuos month doesn't exist and will terminate";
                    return response;
                }
            }
        } catch (Exception ex) {
            response = "Unexpected error due to : " + ex.toString();
            ex.printStackTrace();
        }
        return response;
    }

    private String getLastMonthKey(String key) {
        client = key.split("/")[0];
        String fileDate = key.split("/")[1];
        Date date = DateUtil.stringToDate(fileDate, "MM-dd-yyyy");
        String lastMonthDate = DateUtil.getlastMonthDate(date);
        System.out.println("--Last month key should be " + lastMonthDate);
        return client + "/" + lastMonthDate;
    }

    private boolean isLastMonthExists(String key) {
        String lastMonthKey = getLastMonthKey(key);

        ListObjectsV2Result result = s3Service.getAwsS3().listObjectsV2(BucketName.INPUT_BUCKET.getBucketName());
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        for (S3ObjectSummary os : objects) {
            if (os.getKey().equals(lastMonthKey)) {
                return true;
            }
        }
        return false;
    }

}