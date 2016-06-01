package com.hayavadana.nikemanfapp;

/**
 * Created by Sridhar on 14/05/16.
 */
 public class ProductInfo {

    private String manufacturer = null;
    private String productName = null;
    private String productDesc = null;
    private String serialNum = null;
    private String guiId = null;
    private String tagId;

    public String getTagIdArr() {
        return tagIdArr;
    }

    public void setTagIdArr(String mTagId) {
        this.tagIdArr = mTagId;
    }

    private String tagIdArr;

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getGuiId() {
        return guiId;
    }

    public void setGuiId(String guiId) {
        this.guiId = guiId;
    }


    public ProductInfo(String manufacturer, String productDesc, String serialNum, String productName) {
        this.manufacturer = manufacturer;
        this.productDesc = productDesc;
        this.serialNum = serialNum;
        this.productName = productName;
        this.tagId = tagId;

    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getProductDesc() {
        return productDesc;
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc;
    }

    public String getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(String serialNum) {
        this.serialNum = serialNum;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

}
