package com.simon.etlProcessor.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
        // get products Liquidity
        Map<String, ProductLiquidity> productsLiquidityMap = dao.getProductsLiquidity(client);

        logger.log("1-Will grouping memebers data");
        // group data
        Map<String, List<ProductBalance>> oldMonthDataGrpMap = groupMemeberProducts(oldMonth, productsLiquidityMap);
        Map<String, List<ProductBalance>> newMonthDataGrpMap = groupMemeberProducts(newMonth, productsLiquidityMap);

        logger.log("Data are grouped by members");
        logger.log("2-Will get delta changes");
        // calculate delta change
        Map<String, List<ProductBalance>> delatchangeData = getDeltaChangeData(oldMonthDataGrpMap, newMonthDataGrpMap);
        logger.log("Got Delta changes for each member");

        // create output file
        logger.log("4-creating FOF fiel");
        List<FlowOfFund> fofLst = getFOF(delatchangeData);
        try {
            String outputFileKEy = csvWriter.writeRecords(fofLst, client);
            return outputFileKEy;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * method to group each member with his products
     * 
     * @param productsLiquidityMap2
     * 
     */
    private Map<String, List<ProductBalance>> groupMemeberProducts(InputStreamReader fileStreamReader,
            Map<String, ProductLiquidity> productsLiquidityMap) {
        try {
            List<ProductBalance> productBalances = CSVParser.mapFileToObject(fileStreamReader, ProductBalance.class);

            for (ProductBalance item : productBalances) {
                item.setOrder(productsLiquidityMap.get(item.getProduct()) != null
                        ? productsLiquidityMap.get(item.getProduct()).getProductOrder()
                        : 0);
            }
            Map<String, List<ProductBalance>> balanceMap = productBalances.stream()
                    .collect(Collectors.groupingBy(ProductBalance::getMemberID));
            return balanceMap;
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
    // 5- if product exists in old month but not in the new month ----> ignore
    // it
    private static Map<String, List<ProductBalance>> getDeltaChangeData(Map<String, List<ProductBalance>> oldMonth,
            Map<String, List<ProductBalance>> newMonth) {
        List<String> deletedMembers = new ArrayList<String>();
        for (Map.Entry<String, List<ProductBalance>> entry : newMonth.entrySet()) {
            List<ProductBalance> oldMonthBalance = oldMonth.get(entry.getKey());
            if (oldMonthBalance != null) {
                if (oldMonthBalance.size() <= entry.getValue().size()) {

                    for (ProductBalance newItem : entry.getValue()) {
                        for (ProductBalance oldItem : oldMonthBalance) {
                            // Put delta change = balance by default to consider
                            // the
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
                            } else {
                                deletedMembers.add(entry.getKey());
                            }

                        }
                    }
                } else {
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

    /** This method to create Flow of fund list to be written in excel */
    private List<FlowOfFund> getFOF(Map<String, List<ProductBalance>> delataChanges) {
        List<FlowOfFund> lst = new ArrayList<FlowOfFund>();

        for (Entry<String, List<ProductBalance>> products : delataChanges.entrySet()) {

            List<FlowOfFund> fofForNegativeDeltasLst = calculateFOFForNegativeDeltas(products.getValue());
            if (fofForNegativeDeltasLst.size() > 0) {
                lst.addAll(fofForNegativeDeltasLst);
            }

            List<FlowOfFund> fofForPositiveDeltasLst = calculateFOFForPositiveDeltas(products.getValue());
            if (fofForPositiveDeltasLst.size() > 0) {
                lst.addAll(fofForPositiveDeltasLst);
            }


        }

        return lst;

    }

    /**
     * This method to loop on lst and calculate FOF for all products that have
     * positive delta change value
     */
    private List<FlowOfFund> calculateFOFForPositiveDeltas(List<ProductBalance> products) {
        List<FlowOfFund> lst = new ArrayList<FlowOfFund>();
        // First sort list of user's product by product liquidity order
        // ascending
        Collections.sort(products, new Comparator<ProductBalance>() {
            public int compare(ProductBalance o1, ProductBalance o2) {
                if (o1.getOrder() > o2.getOrder()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        for (ProductBalance item : products) {

            // 3- if delta change was +ve then search for first -ve change
            // with lower liquidity
            if (item.getDeltaChange() > 0.0) {
                FlowOfFund row = new FlowOfFund();
                row.setSnapshotDate(item.getSnapshotDate());
                row.setMemberId(item.getMemberID());
                row.setFlowTo(item.getProduct());
                // Sort items with ascending order

                Optional<ProductBalance> firstnegativeItem = getFirstNegativeDelta(products, item);

                if (firstnegativeItem.isPresent()) {
                    // a- if found then put from = current product and to =
                    // resulted search
                    row.setFlowFrom(firstnegativeItem.get().getProduct());
                    row.setFlowAmount(Math.abs(firstnegativeItem.get().getDeltaChange() + item.getDeltaChange()) + "");
                }
                // b- if not found then put from = current product and to is
                // others
                else {
                    row.setFlowFrom("NA");
                    row.setFlowAmount(Math.abs(item.getDeltaChange()) + "");
                }
                lst.add(row);
            }

        }
        return lst;
    }

    /**
     * This method to loop on lst and calculate FOF for all products that have
     * negative delta change value
     */
    private List<FlowOfFund> calculateFOFForNegativeDeltas(List<ProductBalance> products) {
        // First sort list of user's product by product liquidity order
        // descending
        Collections.sort(products, new Comparator<ProductBalance>() {
            public int compare(ProductBalance o1, ProductBalance o2) {
                if (o1.getOrder() < o2.getOrder()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        List<FlowOfFund> lst = new ArrayList<FlowOfFund>();
        // 1- Loop on user's lst and get first delta change for a product
        for (ProductBalance item : products) {
            // 2- if delta change was -ve then search for first +ve delta
            // change with highest liquidity
            if (item.getDeltaChange() < 0.0) {
                FlowOfFund row = new FlowOfFund();
                row.setSnapshotDate(item.getSnapshotDate());
                row.setMemberId(item.getMemberID());
                // then put flow from is current product
                row.setFlowFrom(item.getProduct());
                // Sort items with descending order
                Optional<ProductBalance> firstPositiveItem = getFirstPositiveDelta(products, item);
                if (firstPositiveItem.isPresent()) {
                    // a- if found then put from = current product and to =
                    // resulted search
                    row.setFlowTo(firstPositiveItem.get().getProduct());
                    row.setFlowAmount(Math.abs(firstPositiveItem.get().getDeltaChange() + item.getDeltaChange()) + "");
                }
                // b- if not found then put from = current product and to is
                // others
                else {
                    row.setFlowTo("NA");
                    row.setFlowAmount(Math.abs(item.getDeltaChange()) + "");
                }
                lst.add(row);
            }

        }
        return lst;
    }
    /**
     * This method returns first positive delta change in the list with highest
     * product liquidity as passed list already sorted descending
     **/
    private Optional<ProductBalance> getFirstPositiveDelta(List<ProductBalance> lst, ProductBalance current) {
        Optional<ProductBalance> firstPositiveItem = lst.stream()
                .filter(x -> !current.getProduct().equals(x.getProduct()) && x.getDeltaChange() > 0.0).findFirst();
        return firstPositiveItem;
    }

    /**
     * This method returns first negative delta change in the list with lowest
     * product liquidity as passed list already sorted ascending
     **/
    private Optional<ProductBalance> getFirstNegativeDelta(List<ProductBalance> lst, ProductBalance current) {
        Optional<ProductBalance> firstPositiveItem = lst.stream()
                .filter(x -> !current.getProduct().equals(x.getProduct()) && x.getDeltaChange() < 0.0).findFirst();
        return firstPositiveItem;
    }



}
