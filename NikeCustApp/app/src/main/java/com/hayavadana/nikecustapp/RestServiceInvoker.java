package com.hayavadana.nikecustapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class RestServiceInvoker extends AsyncTask<String, Void, Void> {


private String ContentFrmServer;
private String ErrorMsg;
private Activity curActivity;
private java.lang.Class nextActivityClass;
private ProgressDialog progress;
private String operationType;
private String jsonRespStr = "";
private String svcURL;
private String httpMethod;
private String postRequestParams;
private Context curContext;
private boolean connIssue = false;
private ProductInfo productInfoRef;
    EditText editText ;
    EditText editText1 ;
    EditText editText2 ;
    EditText editText3;
    EditText editText4;

    public EditText getEditText2() {
        return editText2;
    }

    public void setEditText2(EditText editText2) {
        this.editText2 = editText2;
    }

    public EditText getEditText1() {
        return editText1;
    }

    public void setEditText1(EditText editText1) {
        this.editText1 = editText1;
    }

    public EditText getEditText3() {
        return editText3;
    }

    public void setEditText3(EditText editText3) {
        this.editText3 = editText3;
    }

    public EditText getEditText() {
        return editText;
    }

    public void setEditText(EditText editText) {
        this.editText = editText;
    }



    public EditText getEditText4() {
        return editText4;
    }

    public void setEditText4(EditText editText4) {
        this.editText4 = editText4;
    }


    public ProductInfo getProductInfoRef() {
        return productInfoRef;
    }


public void setProductInfoRef(ProductInfo productInfoRef) {
        this.productInfoRef = productInfoRef;
        }


public void setPostRequestParams(String postRequestParams) {
        this.postRequestParams = postRequestParams;
        }


public void setCurrentContext(Context curCntxt) {
        this.curContext = curCntxt;
        }


public void setHttpMethod(String meth) {
        httpMethod = meth;

        }

public void setCurActivity(Activity curAct) {

        curActivity = curAct;
        }

public void setNextActivity(Class nextActClass) {

        nextActivityClass = nextActClass;
        }

public void setOperationType(String opType) {
        operationType = opType;

        }

public void setURL(String urlStr) {
        svcURL = urlStr;

        }

protected void onPreExecute() {

        progress = new ProgressDialog(curActivity);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.show();

        }

// Call after onPreExecute method
protected Void doInBackground(String... urls) {

        System.out.println("nampelli inside the do in background");

        System.out.println("inside background task");

        jsonRespStr = "";
        try {

        URL url = new URL(svcURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(httpMethod);

        if (httpMethod == "POST") {
        System.out.println("before writing the json object string");
        conn.setRequestProperty("content-type", "application/json; charset=utf-8");
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(postRequestParams);
        System.out.println("after writing the json object string");

        }

        if (conn.getResponseCode() != 200) {
        System.out.println("Problem getting response from server..");
        connIssue = true;
        } else {

        BufferedReader br = new BufferedReader(new InputStreamReader(
        (conn.getInputStream())));

        String output;
        System.out.println("Output from Server .... \n");

        while ((output = br.readLine()) != null) {
        jsonRespStr += output;
        System.out.println(output);
        }
       System.out.println("SREENI received json string is :" + jsonRespStr);
        }
        conn.disconnect();

        } catch (MalformedURLException e) {

        e.printStackTrace();

        } catch (IOException e) {
        //showDialog("Unable to connect to server");
        connIssue = true;
        e.printStackTrace();

        }

        return null;


        }

protected void onPostExecute(Void unused) {
        // NOTE: You can call UI Element here.

        System.out.println("INSIDE ONPOST EXECUTE");
        progress.dismiss();

        if (connIssue) {
        showDialog("Connectivity Issue", "Problem getting response from server..");
        connIssue = false;
        return;
        }
        if (jsonRespStr != "") try {
        processResponse();
        } catch (JSONException e) {
        e.printStackTrace();
        }


        }

public void processResponse() throws JSONException {
        System.out.println("Nampelli: Inside process Response");
        if (operationType == "RETRIEVE_PRODUCT_DETAILS") {
        System.out.println("Nampelli: Inside RETRIEVE_PRODUCT_DETAILS");

        try {

        JSONObject jsonResp = new JSONObject(jsonRespStr);
        String prodID = jsonResp.optString("proId");
        String prodDesc = jsonResp.optString("proDesc");
        String prodName = jsonResp.optString("productName");
        String prodManf = jsonResp.optString("manufactureName");
        String datentime = jsonResp.optString("date");
        String error = jsonResp.optString("error");

            if(error != "" && error != null)
            {

            }


            productInfoRef.setSerialNum(prodID);
            productInfoRef.setProductDesc(prodDesc);
            productInfoRef.setProductName(prodName);
            productInfoRef.setManufacturer(prodManf);




            System.out.println("prodId" + prodID);
            System.out.println("prodDesc" + prodDesc);
            System.out.println("prodName" + prodName);
            System.out.println("prodManf" + prodManf);


            EditText etManf = (EditText)this.curActivity.findViewById(R.id.etManf);
            etManf.setText(prodManf);

            EditText etPName = (EditText)this.curActivity.findViewById(R.id.etPName);
            etPName.setText(prodName);

            EditText etSnum = (EditText)this.curActivity.findViewById(R.id.etSNum);
            etSnum.setText(prodID);

            EditText etPDesc = (EditText)this.curActivity.findViewById(R.id.etPDesc);
            etPDesc.setText(prodDesc);

            EditText etDnT = (EditText)this.curActivity.findViewById(R.id.etDnT);
            etDnT.setText(datentime);


        } catch (Exception e) {
        e.printStackTrace();
        }
        }
        jsonRespStr = "";

        }

private void showDialog(String alertTitle, String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(curContext).create();

        // Setting Dialog Title
        alertDialog.setTitle(alertTitle);

        // Setting Dialog Message
        alertDialog.setMessage(msg);

        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
public void onClick(DialogInterface dialog, int which) {
        // Write your code here to execute after dialog closed
        dialog.dismiss();
        }
        });

        // Showing Alert Message
        alertDialog.show();

        }


    public String byteArrayToString(byte[] arr){
        String str="";
        for(byte i:arr){
            str = str+i;

        }
        return  str;
    }
}
