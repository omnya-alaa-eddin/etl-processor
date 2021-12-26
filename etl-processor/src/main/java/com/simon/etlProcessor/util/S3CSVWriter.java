package com.simon.etlProcessor.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.simon.etlProcessor.enums.BucketName;
import com.simon.etlProcessor.model.FlowOfFund;
import com.simon.etlProcessor.service.S3Service;

public class S3CSVWriter {
    S3Service s3Service = new S3Service();

    public String writeRecords(List<FlowOfFund> fofLst, String tenant) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter streamWriter = new OutputStreamWriter(stream, StandardCharsets.UTF_8);

        try (CSVWriter writer = buildCSVWriter(streamWriter)) {
            StatefulBeanToCsv<FlowOfFund> beanToCsv = new StatefulBeanToCsvBuilder(writer)
                    .withQuotechar(com.opencsv.CSVWriter.NO_QUOTE_CHARACTER).build();

            beanToCsv.write(fofLst);
            writer.flush();
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(stream.toByteArray().length);
            String outputFileName = generateOutputFileName(fofLst.get(0).getSnapshotDate(), tenant);
            s3Service.getAwsS3().putObject(BucketName.OUTPUT_BUCKET.getBucketName(),
                    outputFileName,
                    new ByteArrayInputStream(stream.toByteArray()),
                    meta);
            return outputFileName;
        } catch (CsvDataTypeMismatchException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CsvRequiredFieldEmptyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }

    private String generateOutputFileName(String dateString, String client) {

        Date date = DateUtil.stringToDate(dateString, "yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return client + "/fof_" + cal.get(Calendar.YEAR) + cal.get(Calendar.MONTH) + ".csv";
    }

    private CSVWriter buildCSVWriter(OutputStreamWriter streamWriter) {
        return new CSVWriter(streamWriter);
    }

}
