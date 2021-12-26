package com.simon.etlProcessor.service;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

public class SQSService {

    public String getS3Key(SQSMessage msg) {

        try {
            JSONObject msgJson;
            msgJson = new JSONObject(msg.getBody());
            JSONObject record = (JSONObject) (msgJson.getJSONArray("Records")).get(0);
            String s3Key = record.getJSONObject("s3").getJSONObject("object").getString("key");
            return s3Key;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
