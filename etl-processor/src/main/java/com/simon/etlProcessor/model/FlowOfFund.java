package com.simon.etlProcessor.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvIgnore;


public class FlowOfFund {
    public FlowOfFund(String memberId, String flowFrom, String flowTo, String flowAmount,
            String snapshotDate) {
        super();
        this.memberId = memberId;
        this.flowFrom = flowFrom;
        this.flowTo = flowTo;
        this.flowAmount = flowAmount;
        this.snapshotDate = snapshotDate;
    }

    public FlowOfFund() {
        super();
    }

    @CsvBindByName(column = "")
    @CsvBindByPosition(position = 0)
    @CsvIgnore
    private String id;

    @CsvBindByName(column = "Member_ID")
    @CsvBindByPosition(position = 1)
    private String memberId;

    @CsvBindByPosition(position = 3)
    @CsvBindByName(column = "Flow_From")
    private String flowFrom;

    @CsvBindByPosition(position = 4)
    @CsvBindByName(column = "Flow_To")
    private String flowTo;

    @CsvBindByPosition(position = 5)
    @CsvBindByName(column = "Flow_Amount")
    private String flowAmount;

    @CsvBindByPosition(position = 6)
    @CsvBindByName(column = "Snapshot_Date")
    private String snapshotDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
    public String getFlowFrom() {
        return flowFrom;
    }

    public void setFlowFrom(String flowFrom) {
        this.flowFrom = flowFrom;
    }

    public String getFlowTo() {
        return flowTo;
    }

    public void setFlowTo(String flowTo) {
        this.flowTo = flowTo;
    }

    public String getFlowAmount() {
        return flowAmount;
    }

    public void setFlowAmount(String flowAmount) {
        this.flowAmount = flowAmount;
    }

    public String getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(String snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

}
