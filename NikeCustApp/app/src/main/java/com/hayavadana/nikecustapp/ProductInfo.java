package com.hayavadana.nikecustapp;

/**
 * Created by Sridhar on 16/05/16.
 */
public class ProductInfo   {
    private String manufacturer = null;
    private String productName = null;
    private String productDesc = null;
    private String serialNum = null;
    private String guiId = null;
    private String tagIdString;
    private String postRequestParams;


    public String getTagIdString() {
        return tagIdString;
    }

    public void setTagIdString(String tagIdString) {
        this.tagIdString = tagIdString;
    }




    public void setPostRequestParams(String postRequestParams) {
        this.postRequestParams = postRequestParams;
    }



    public String getGuiId() {
            return guiId;
        }

        public void setGuiId(String guiId) {
            this.guiId = guiId;
        }
    public ProductInfo(){}


        public ProductInfo(String manufacturer, String productDesc, String serialNum, String productName) {
            this.manufacturer = manufacturer;
            this.productDesc = productDesc;
            this.serialNum = serialNum;
            this.productName = productName;
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