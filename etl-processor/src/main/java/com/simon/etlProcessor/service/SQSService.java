package com.simon.etlProcessor.service;

import org.json.JSONException;
import org.json.JSONObject;

public class SQSService {

    public String getS3Key(String body) {

        try {
            JSONObject msgJson;
            msgJson = new JSONObject(body);
            JSONObject record = (JSONObject) (msgJson.getJSONArray("Records")).get(0);
            String s3Key = record.getJSONObject("s3").getJSONObject("object").getString("key");
            return s3Key;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
