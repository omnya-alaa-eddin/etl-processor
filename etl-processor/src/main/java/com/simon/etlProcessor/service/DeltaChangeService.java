package com.simon.etlProcessor.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.simon.etlProcessor.domain.ProductDao;
import com.simon.etlProcessor.model.FlowOfFund;
import com.simon.etlProcessor.model.ProductBalance;
import com.simon.etlProcessor.model.ProductLiquidity;
import com.simon.etlProcessor.util.CSVParser;
import com.simon.etlProcessor.util.S3CSVWriter;

public class DeltaChangeService {
    ProductDao dao = new ProductDao();
    
    static S3CSVWriter csvWriter = new S3CSVWriter();

    public String calculateDeltaChange(InputStreamReader newMonth, InputStreamReader oldMonth, String client,
            LambdaLogger logger) {

        logger.log("Calculating delta changes ..");
        logger.log("1-Will grouping memebers data");
        // group data
        Map<String, List<ProductBalance>> oldMonthDataGrpMap = groupMemeberProducts(oldMonth);
        Map<String, List<ProductBalance>> newMonthDataGrpMap = groupMemeberProducts(newMonth);

        logger.log("Data are grouped by members");
        logger.log("2-Will get delta changes");
        // calculate delta change
        Map<String, List<ProductBalance>> delatchangeData = getDataChangeData(oldMonthDataGrpMap, newMonthDataGrpMap);
        logger.log("Got Delta changes for each member");

        // get products Liquidity
        Map<String, ProductLiquidity> productsLiquidityMap = null;
        productsLiquidityMap = dao.getProductsLiquidity(client);

        // Missed step get foflst

        // create output file

        logger.log("4-creating FOF fiel");
        List<FlowOfFund> fofLst = createFOfList(delatchangeData);
        try {
            String outputFileKEy = csvWriter.writeRecords(fofLst, client);
            return outputFileKEy;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private static List<FlowOfFund> createFOfList(Map<String, List<ProductBalance>> deltaChanges) {
        List<FlowOfFund> fofLst = new ArrayList<FlowOfFund>();
        for (Entry<String, List<ProductBalance>> lst : deltaChanges.entrySet())
        {
            for (ProductBalance item : lst.getValue()) {
            FlowOfFund fofItem = new FlowOfFund();

                fofItem.setFlowAmount(item.getDeltaChange().toString());
                fofItem.setFlowFrom(item.getProduct());
                fofItem.setFlowTo(item.getProduct());
                fofItem.setSnapshotDate(item.getSnapshotDate());
                fofItem.setMemberId(item.getMemberID());
                fofLst.add(fofItem);
            }
        }
        return fofLst;
    }
    // method to group each member with his products
    private Map<String, List<ProductBalance>> groupMemeberProducts(InputStreamReader fileStreamReader) {
        try {
            List<ProductBalance> oldMonthBalance = CSVParser.mapFileToObject(fileStreamReader, ProductBalance.class);
            Map<String, List<ProductBalance>> oldMonthBalanceMap = oldMonthBalance.stream()
                    .collect(Collectors.groupingBy(ProductBalance::getMemberID));
            return oldMonthBalanceMap;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    // We have cases for delta calulation must be considered
    // 1- if product and member exist in both months
    // 2 if member not exists in previous month >> Ignore it
    // 3- if members exists but product not exists in previous month >> put
    // delta change = balance
    // 4- if delta change = 0 >> exclude item
    private static Map<String, List<ProductBalance>> getDataChangeData(Map<String, List<ProductBalance>> oldMonth,
            Map<String, List<ProductBalance>> newMonth) {
        List<String> deletedMembers = new ArrayList<String>();
        for (Map.Entry<String, List<ProductBalance>> entry : newMonth.entrySet()) {
            List<ProductBalance> oldMonthBalance = oldMonth.get(entry.getKey());
            if (oldMonthBalance != null) {
                if (oldMonthBalance.size() <= entry.getValue().size()) {

                
            for (ProductBalance newItem : entry.getValue()) {
                for (ProductBalance oldItem : oldMonthBalance) {
                        // Put delta change = balance by default to consider the
                        // case of product not exists in old month
                        newItem.setDeltaChange(Double.parseDouble(newItem.getBalance()));
                        if (Double.parseDouble(newItem.getBalance()) != 0.0) {
                            if (newItem.getProduct().equals(oldItem.getProduct())) {
                                Double delta = Double.parseDouble(newItem.getBalance())
                                        - Double.parseDouble(oldItem.getBalance());
                                if (delta == 0.0) {
                                    deletedMembers.add(entry.getKey());
                                    break;
                                }
                                newItem.setDeltaChange(delta);
                                break;
                            }
                        }
                        else {
                            deletedMembers.add(entry.getKey());
                        }

                }
                }
            }
                else {
                    deletedMembers.add(entry.getKey());
                }
            }
            // member id is not existed in old month then delete from map

            else {
                deletedMembers.add(entry.getKey());
            }

        }

        for (String member : deletedMembers) {
            newMonth.remove(member);
        }
        return newMonth;

    }


    private void getFOF(Map<String, ProductLiquidity> liquidity, Map<String, List<ProductBalance>> delataChanges) {


        for (Entry<String, List<ProductBalance>> products : delataChanges.entrySet()) {

            
            // if delta is -ve and no other items in list
            // then put fof from = product and fof-to = other
            for (ProductBalance item : products.getValue()) {
                FlowOfFund fof = new FlowOfFund();
                if (item.getDeltaChange() < 0.0) {
                    ProductBalance positiveProduct = getFirstPositiveDelata(products.getValue(), item);
                    if (positiveProduct != null) {
                        positiveProduct.setDeltaChange(0.0);


                    }
                }
                // see if there is another product with higher liquidity in his list
                else {
                    fof.setFlowTo(item.getProduct());
                    ProductBalance negativeProduct = getFirstNegativeDelata(products.getValue(), item);
                    if (negativeProduct != null) {
                        negativeProduct.setDeltaChange(0.0);
                        fof.setFlowFrom(negativeProduct.getProduct());
                        fof.setFlowAmount(Math.abs(negativeProduct.getDeltaChange()) + "");
                        item.setDeltaChange(0.0);
                    }

                }
            }
        }
    }

    private ProductBalance getFirstPositiveDelata(List<ProductBalance> lst, ProductBalance searched) {
        for (ProductBalance item : lst) {
            if (!item.getProduct().equals(searched.getProduct()) && item.getDeltaChange() > 0.0) {
                return item;
            }
        }
        return null;
    }

    private ProductBalance getFirstNegativeDelata(List<ProductBalance> lst, ProductBalance searched) {
        for (ProductBalance item : lst) {
            if (!item.getProduct().equals(searched.getProduct()) && item.getDeltaChange() > 0.0) {
                return item;
            }
        }
        return null;
    }
    private ProductBalance getLowerProductLiquidity(List<ProductBalance> lst,
            Map<String, ProductLiquidity> liquidity, ProductBalance searched) {
        for (ProductBalance item : lst) {
            if (!item.getProduct().equals(searched.getProduct())) {
                if (liquidity.get(searched).getProductOrder() < liquidity.get(item.getProduct()).getProductOrder()) {
                    return item;
                }

            }
        }
        return null;
    }/*
      * //* public static void main(String[] args) {
      * 
      * File oldFile = new
      * File("C:\\Users\\oali5\\Desktop\\Input\\FINAL201711.csv"); File newFile
      * = new File("C:\\Users\\oali5\\Desktop\\Input\\FINAL201712.csv");
      * 
      * try { InputStream oldFileStream = new FileInputStream(oldFile);
      * InputStream newFileStream = new FileInputStream(newFile);
      * InputStreamReader oldFileStreamReader = new
      * InputStreamReader(oldFileStream); InputStreamReader newFileStreamReader
      * = new InputStreamReader(newFileStream); List<ProductBalance>
      * oldMonthBalance = CSVParser.mapFileToObject(oldFileStreamReader,
      * ProductBalance.class); List<ProductBalance> newMonthBalance =
      * CSVParser.mapFileToObject(newFileStreamReader, ProductBalance.class);
      * Map<String, List<ProductBalance>> oldMonthBalanceMap =
      * oldMonthBalance.stream()
      * .collect(Collectors.groupingBy(ProductBalance::getMemberID));
      * Map<String, List<ProductBalance>> newMonthBalanceMap =
      * newMonthBalance.stream()
      * .collect(Collectors.groupingBy(ProductBalance::getMemberID));
      * 
      * 
      * System.out.println(newMonthBalanceMap.size()); Map<String,
      * List<ProductBalance>> deltaChanges =
      * getDataChangeData(oldMonthBalanceMap, newMonthBalanceMap);
      * System.out.println(deltaChanges.size());
      * 
      * // calculate liquidity List<FlowOfFund> fofLst =
      * createFOfList(deltaChanges);
      * 
      * try { csvWriter.writeRecords(fofLst, "c1"); } catch (IOException e) { //
      * TODO Auto-generated catch block e.printStackTrace(); }
      * 
      * 
      * } catch (IOException e) { // TODO Auto-generated catch block
      * e.printStackTrace(); }
      * 
      * }
      */

}
