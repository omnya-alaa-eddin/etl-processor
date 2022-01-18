package com.simon.etlProcessor.model;

import com.opencsv.bean.CsvBindByPosition;

public class ProductBalance {

    @CsvBindByPosition(position = 1)
    private String snapshotDate;

    @CsvBindByPosition(position = 2)
    private String memberID;

    @CsvBindByPosition(position = 10)
    private String product;

    @CsvBindByPosition(position = 12)
    private String balance;

    private Double deltaChange;

    private Integer order;

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(String snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public String getMemberID() {
        return memberID;
    }

    public void setMemberID(String memberID) {
        this.memberID = memberID;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public Double getDeltaChange() {
        return deltaChange;
    }

    public void setDeltaChange(Double deltaChange) {
        this.deltaChange = deltaChange;
    }

}
