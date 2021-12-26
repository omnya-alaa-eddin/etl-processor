package com.simon.etlProcessor.model;

public class DeltaChangeModel {

    private Double delta;

    private String product;

    private String member;

    public Double getDelta() {
        return delta;
    }

    public void setDelta(Double delta) {
        this.delta = delta;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public DeltaChangeModel() {
        super();
    }

}
