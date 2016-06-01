package com.hayavadana.nikemanfapp;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

public class NfcWriteData extends AppCompatActivity {


    private NfcAdapter mNfcAdapter;
    private static boolean writeProtect = false;
    private   Context context;
    private JSONObject submitProdInfoObj;
    private static ProductInfo productInfo = null;
    public  static Tag detectedTag;
    private NfcAdapter nfcAdpt;
    private String tagIdString = "tag";
    boolean mTagFound;
    private Handler customHandler;
    private long timeInMilliseconds = 0L;
    private long updatedTime = 0L;
    private long startTime = 0L;
    static final int MAX_TIMEOUT = 15;
    private Intent nfcIntent;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techList;
    private PendingIntent mNfcPendingIntent;
    private IntentFilter[] mWriteTagFilters;

    public String getmTagId() {
        return mTagId;
    }

    public void setmTagId(String mTagId) {
        this.mTagId = mTagId;
    }

    private String mTagId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_write_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String username = getIntent().getStringExtra("Username");

        TextView textView = (TextView) findViewById(R.id.tvUsername);
        textView.setText(username);

        Button wrtButton = (Button) findViewById(R.id.btnWrite);


                wrtButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {
                            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
                        }catch (Exception e){

                        }

                        if (!isNFCEnabled()){
                            showAlert("Info", "Please Enable NFC on your Phone");
                            return;
                        }

                        if (isValidInputs()){
                          

                            mTagFound = false;
                            toggleProgressBarVisibility(true);
                            
                            startTime = SystemClock.uptimeMillis();

                            customHandler = new Handler();
                            customHandler.postDelayed(updateTimerThread, 0);

                        }
                        else {
                            AlertDialog alertDialog = new AlertDialog.Builder(NfcWriteData.this).create();

                            // Setting Dialog Title
                            alertDialog.setTitle("Error");
                            // Setting Dialog Message
                            alertDialog.setMessage("Plz Enter Valid Inputs");
                            // Setting OK Button
                            alertDialog.setButton("OK", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Write your code here to execute after dialog closed
                                    dialog.dismiss();
                                }
                            });

                            // Showing Alert Message
                            alertDialog.show();

                        }
                        
                    }

                });

        context = getApplicationContext();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);


        // Intent filters for writing to a tag
        IntentFilter discovery=new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

        mWriteTagFilters = new IntentFilter[] { discovery };


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nfc_write, menu);
        return true;
    }

    public void onLogoutBtnClick(View view){

        if (view.getId() == R.id.btnLogout);
        {

            Intent intent = new Intent(NfcWriteData.this, Login.class);
            startActivity(intent);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();

         nfcIntent = new Intent(this, getClass());
        nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
         pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
         intentFiltersArray = new IntentFilter[] {};
        techList = new String[][] { { android.nfc.tech.Ndef.class.getName() }, { android.nfc.tech.NdefFormatable.class.getName() } };

        nfcAdpt = NfcAdapter.getDefaultAdapter(this);

        if(nfcAdpt != null) nfcAdpt.disableForegroundDispatch(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (customHandler!=null) {
            customHandler.removeCallbacks(updateTimerThread);
            toggleProgressBarVisibility(false);
        }
        if(mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);

        if (mTagFound){
            return;
        }
        mTagFound = true;
        nfcAdpt = NfcAdapter.getDefaultAdapter(this);

        String mTagId = "";
        byte[] uid = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        for (int b = 1; b < uid.length; b++) { // skip SOH
            mTagId += (char) uid[b];
        }

        RestServiceInvoker rInvoker = new RestServiceInvoker();
        setmTagId(rInvoker.byteArrayToString(uid));

        System.out.println("Nf" + tagIdString.toString());
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())  ) {
            // validate that this tag can be written....
            detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            System.out.println("Nfctag" + mTagId);
            if(supportedTechs(detectedTag.getTechList())) {
                // check if tag is writable (to the extent that we can
                if (writableTag(detectedTag)) {


                    SendDetailsToServer();

                } else {
                    Toast.makeText(context,"This tag is not writeable",Toast.LENGTH_SHORT).show();

                }
            }else {
                Toast.makeText(context,"This tag type is not supported",Toast.LENGTH_SHORT).show();

            }
        }
    }


    public  static String writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
        String mess = "";

        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();

                if (!ndef.isWritable()) {
                    String ret_mess = "Tag is read-only";
                    return ret_mess;
                }
                if (ndef.getMaxSize() < size) {
                    mess = "Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size
                            + " bytes.";
                    String ret_mess = mess;
                    return ret_mess;

                }

                ndef.writeNdefMessage(message);
                if(writeProtect)  ndef.makeReadOnly();
                mess = "SUCCESS";
                String ret_mess = mess;
                return ret_mess;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        mess = "SUCCESS";
                        String ret_mess = mess;
                        return ret_mess;
                    } catch (IOException e) {
                        mess = "Failed to format tag.";
                        String ret_mess = mess;
                        return ret_mess;
                    }
                } else {
                    mess = "Tag doesn't support NDEF.";

                    String ret_mess = mess;
                    return ret_mess;
                }
            }
        } catch (Exception e) {
            mess = "Failed to write tag";
            WriteResponse aresp;
            String ret_mess = mess;
            return ret_mess;
        }
    }

    private class WriteResponse {
        int status;
        String message;
        WriteResponse(int Status, String Message) {
            this.status = Status;
            this.message = Message;
        }
        public int getStatus() {
            return status;
        }
        public String getMessage() {
            return message;
        }
    }

    public static boolean supportedTechs(String[] techs) {
        boolean ultralight=false;
        boolean nfcA=false;
        boolean ndef=false;
        for(String tech:techs) {
            if(tech.equals("android.nfc.tech.MifareUltralight")) {
                ultralight=true;
            }else if(tech.equals("android.nfc.tech.NfcA")) {
                nfcA=true;
            } else if(tech.equals("android.nfc.tech.Ndef") || tech.equals("android.nfc.tech.NdefFormatable")) {
                ndef=true;

            }
        }
        if(ultralight && nfcA && ndef) {
            return true;
        } else {
            return false;
        }
    }

    private boolean writableTag(Tag tag) {

        try {
            Ndef ndef = (Ndef.get(tag));
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    Toast.makeText(context,"Tag is read-only.",Toast.LENGTH_SHORT).show();
                    ndef.close();
                    return false;
                }
                ndef.close();
                return true;
            }
        } catch (Exception e) {
            Toast.makeText(context,"Failed to read tag",Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    public static NdefMessage createTextMessage(String content) {
        try {
// Get UTF-8 byte
            byte[] lang = Locale.getDefault().getLanguage().getBytes("UTF-8");
            byte[] text = content.getBytes("UTF-8"); // Content in UTF-8


            int langSize = lang.length;
            int textLength = text.length;

            ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + langSize + textLength);
            payload.write((byte) (langSize & 0x1F));
            payload.write(lang, 0, langSize);
            payload.write(text, 0, textLength);
            NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                    NdefRecord.RTD_TEXT, new byte[0],
                    payload.toByteArray());
            return new NdefMessage(new NdefRecord[]{record});
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getTagID(){

        return tagIdString;
    }

    public void SendDetailsToServer(){

        EditText editText = (EditText) findViewById(R.id.etManf);
        EditText editText1 = (EditText) findViewById(R.id.etPDesc);
        EditText editText2 = (EditText) findViewById(R.id.etSNum);
        EditText editText3 = (EditText) findViewById(R.id.etPName);


        productInfo = new ProductInfo(editText.getText().toString(),
                editText1.getText().toString(),
                editText2.getText().toString(), editText3.getText().toString());

        productInfo.setTagId(getmTagId());
        productInfo.setTagIdArr(mTagId);



        RestServiceInvoker rInvoker = new RestServiceInvoker();
        WebServiceHost webSrvUrlObj = WebServiceHost.getInstance();
        String svcUrlStr = webSrvUrlObj.getWebserviceURL();

        rInvoker.setProductInfoRef(productInfo);
        rInvoker.setOperationType("Save_Details");
        rInvoker.setHttpMethod("POST");


        if (submitProdInfoObj == null) {
            submitProdInfoObj = new JSONObject();
        }

        //sample data
        try {

            submitProdInfoObj.put("tagId",productInfo.getTagId());
            submitProdInfoObj.put("tagIdArr",productInfo.getTagIdArr());
            submitProdInfoObj.put("proId", productInfo.getSerialNum());
            submitProdInfoObj.put("productName", productInfo.getProductName());
            submitProdInfoObj.put("proDesc", productInfo.getProductDesc());
            submitProdInfoObj.put("manufactureName", productInfo.getManufacturer());

        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("Exception while preparing the JSON object for submit ProdInfo");
        }

        rInvoker.setPostRequestParams(submitProdInfoObj.toString());
        rInvoker.setCurActivity(NfcWriteData.this);
        rInvoker.setCurrentContext(NfcWriteData.this);

        String completeSvcStateUrl = svcUrlStr + "/rest/nfcService/send";
        System.out.println("Sreeni : completeSvcStateUrl is " + completeSvcStateUrl);
        rInvoker.setURL(completeSvcStateUrl);
        rInvoker.execute();
    }

    public static String writeGuiIDIntoTag(){

        //writeTag here
        NdefMessage ndefmesg = createTextMessage(productInfo.getGuiId());

        String wr_status = writeTag(ndefmesg, detectedTag);
        return wr_status;

    }

    private Boolean isValidInputs(){


        EditText editText = (EditText) findViewById(R.id.etManf);
        EditText editText1 = (EditText) findViewById(R.id.etPDesc);
        EditText editText2 = (EditText) findViewById(R.id.etSNum);
        EditText editText3 = (EditText) findViewById(R.id.etPName);

        if(     (editText.getText().toString().length()  == 0) ||
                (editText1.getText().toString().length()  == 0)||
                (editText2.getText().toString().length()  == 0) ||
                (editText3.getText().toString().length()  == 0))
        {

            return false;
        }

        return true;
    }

    private boolean isNFCEnabled(){
        NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            // adapter exists and is enabled.
            return true;
        }
        return false;
    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            updatedTime = timeInMilliseconds;

            Integer secs = (int) (updatedTime / 1000);
            Integer timeLeft = MAX_TIMEOUT - secs;
            TextView tv = (TextView)findViewById(R.id.tvProgress);
            tv.setText("Waiting For NFC Tag"+"\n" + timeLeft.toString());
            if (mTagFound){
                toggleProgressBarVisibility(false);
                customHandler.removeCallbacks(updateTimerThread);
                return;
            }
            if (secs < MAX_TIMEOUT){
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.circularProgressbar);
                progressBar.setProgress(timeLeft*100/15);
                customHandler.postDelayed(this, 0);
            }
            else {
                toggleProgressBarVisibility(false);
                showAlert("No Tag found","Timedout... Waiting for the Tag");
                customHandler.removeCallbacks(updateTimerThread);
            }

        }

    };

    private void toggleProgressBarVisibility(boolean flag) {
        int visib;
        if (flag){
            visib = View.VISIBLE;
            setVisisbilityForAllLayoutElements(false);
            nfcAdpt.enableForegroundDispatch(NfcWriteData.this, pendingIntent, intentFiltersArray, techList);
        }
        else {
            visib = View.INVISIBLE;
            setVisisbilityForAllLayoutElements(true);
            nfcAdpt = NfcAdapter.getDefaultAdapter(this);
            if(nfcAdpt != null) nfcAdpt.disableForegroundDispatch(this);
        }
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.circularProgressbar);
        progressBar.setVisibility(visib);
        TextView tv = (TextView)findViewById(R.id.tvProgress) ;
        tv.setVisibility(visib);
    }

    private void showAlert(String tit, String mesg){
        AlertDialog alertDialog = new AlertDialog.Builder(NfcWriteData.this).create();

        // Setting Dialog Title
        alertDialog.setTitle(tit);
        // Setting Dialog Message
        alertDialog.setMessage(mesg);
        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to execute after dialog closed
                dialog.dismiss();
            }
        });

        // Showing Alert Message
        alertDialog.show();

    }

    private void setVisisbilityForAllLayoutElements(boolean vis){

        int visibilityVal;
        if (vis){
        visibilityVal = View.VISIBLE;
        }
        else {
        visibilityVal = View.INVISIBLE;
        }
        EditText editText = (EditText) findViewById(R.id.etManf);
        EditText editText1 = (EditText) findViewById(R.id.etPName);
        EditText editText2 = (EditText) findViewById(R.id.etSNum);
        EditText editText3 = (EditText) findViewById(R.id.etPDesc);


        editText.setVisibility(visibilityVal);
        editText1.setVisibility(visibilityVal);
        editText2.setVisibility(visibilityVal);
        editText3.setVisibility(visibilityVal);

        Button btn1 = (Button)findViewById(R.id.btnWrite);
        btn1.setVisibility(visibilityVal);

        Button btn3 = (Button) findViewById(R.id.btnLogout);
        btn3.setVisibility(visibilityVal);

        TextView txtView1 = (TextView) findViewById(R.id.tvText);
        TextView txtView2 = (TextView) findViewById(R.id.tvManf);
        TextView txtView3 = (TextView) findViewById(R.id.tvPName);
        TextView txtView4 = (TextView) findViewById(R.id.tvPDesc);
        TextView txtView5 = (TextView) findViewById(R.id.tvSNum);
        TextView txtView6 = (TextView) findViewById(R.id.tvUsername);
        TextView txtView7 = (TextView) findViewById(R.id.textViewWar);

        txtView1.setVisibility(visibilityVal);
        txtView2.setVisibility(visibilityVal);
        txtView3.setVisibility(visibilityVal);
        txtView4.setVisibility(visibilityVal);
        txtView5.setVisibility(visibilityVal);
        txtView6.setVisibility(visibilityVal);
        txtView7.setVisibility(visibilityVal);

    }
}
