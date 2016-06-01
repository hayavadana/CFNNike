package com.hayavadana.nikecustapp;

/**
 * Created by Sridhar on 16/05/16.
 */
public class WebServiceHost {

    private String hostname = "183.83.32.48";
   // private String hostname = "192.168.1.13";
    private int  port = 8080;
    private String webappname = "RateOn";
    //private String webappname = "Rating";
    private static WebServiceHost webServiceHost;
    public String getWebserviceURL(){
        String str;
        str = "http://"+hostname+":"+port+"/"+webappname;
        System.out.println(" getWebserviceURL is returning :" +str);
        return str;
    }
    private WebServiceHost(){

    }

    public static WebServiceHost getInstance(){
        if (webServiceHost == null){
            webServiceHost = new WebServiceHost();
        }
        return webServiceHost;
    }
}