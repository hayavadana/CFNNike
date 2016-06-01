package com.hayavadana.nikecustapp;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class NfcReadData extends AppCompatActivity {


    private NfcAdapter mNfcAdapter;
    private IntentFilter[] mNdefExchangeFilters;
    private PendingIntent mNfcPendingIntent;
    public String GuiId;
    private JSONObject submitProdInfoObj;
    public JSONObject retrieveProdInfoObj;
    private JSONObject tagDetailsJson = null;
    ProductInfo productInfo;
    RestServiceInvoker rInvoker;
    private String tagIdString;
    private String mTagId;
    private String mGuiId;

    Intent nfcIntent;
    PendingIntent pendingIntent;
    IntentFilter[] intentFiltersArray;
    String[][] techList;
    private NfcAdapter nfcAdpt;
    private Context context;
    boolean mTagFound = false;
    private Handler customHandler;
    long timeInMilliseconds = 0L;
    long updatedTime = 0L;
    long startTime = 0L;
    static final int MAX_TIMEOUT = 15;

    public ProductInfo getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_read_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String username = getIntent().getStringExtra("Username");

        TextView textView = (TextView) findViewById(R.id.tvUsername);
        textView.setText(username);

        context = getApplicationContext();

        Button scnButton = (Button) findViewById(R.id.btnScan);

        if (scnButton != null) {
            scnButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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

            }

        });

            Button clrButton = (Button) findViewById(R.id.bClear);
            clrButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearDetails();
                }
            });

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);

            // Intent filters for exchanging over p2p.

        IntentFilter GuiId = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            GuiId.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
        }

        mNdefExchangeFilters = new IntentFilter[]{GuiId};
    }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nfc_read, menu);
        return true;
    }

    public void onLogoutBtnClick(View view) {

        if (view.getId() == R.id.btnLogout) ;
        {

            Intent intent = new Intent(NfcReadData.this, Login.class);
            startActivity(intent);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        nfcIntent = new Intent(this, getClass());
        nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
        intentFiltersArray = new IntentFilter[]{};
        techList = new String[][]{{android.nfc.tech.Ndef.class.getName()}, {android.nfc.tech.NdefFormatable.class.getName()}};

        nfcAdpt = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdpt != null) nfcAdpt.disableForegroundDispatch(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (customHandler!=null) {
            customHandler.removeCallbacks(updateTimerThread);
            toggleProgressBarVisibility(false);
        }

        if (mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);


        if (mTagFound){
            return;
        }
        mTagFound = true;

        if ((NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()))
                || (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()))) {

            //get tag id (Tag manufacturer's ID)
            mTagId = "";
            byte[] uid = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            for (int b = 0; b < uid.length; b++) { // skip SOH
                mTagId +=  uid[b];
            }
            //Get the content that was written in the tag
            NdefMessage[] messages = null;
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                messages = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    messages[i] = (NdefMessage) rawMsgs[i];
                }
            }
            if (messages[0] != null) {
                String result = "";
                byte[] payload = messages[0].getRecords()[0].getPayload();
                // this assumes that we get back am SOH followed by host/code
                mGuiId = "";
                for (int b = 3; b < payload.length; b++) { // skip SOH
                    mGuiId +=  (char) payload[b];
                }

                System.out.println("mGuiId is :" + mGuiId);
                System.out.println("mTagId is :" + mTagId);

                //Send the Tag ID and Gui ID to server
                if ((mTagId.length()>0) && (mGuiId.length()>0)) {

                    GetDetailsFromServer();
                }

            }

        }

    }


    public void GetDetailsFromServer(){

        RestServiceInvoker rInvoker = new RestServiceInvoker();
        WebServiceHost webSrvUrlObj = WebServiceHost.getInstance();
        String svcUrlStr = webSrvUrlObj.getWebserviceURL();

        productInfo = new ProductInfo();
        rInvoker.setProductInfoRef(productInfo);
        rInvoker.setOperationType("RETRIEVE_PRODUCT_DETAILS");
        rInvoker.setHttpMethod("POST");


        if (tagDetailsJson == null) {
            tagDetailsJson = new JSONObject();
        }

        try {
            tagDetailsJson.put("tagId",mTagId);
            tagDetailsJson.put("guiId", mGuiId);

        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("Exception while preparing the JSON object for submitting the Tag Info");
        }

        rInvoker.setPostRequestParams(tagDetailsJson.toString());
        rInvoker.setCurActivity(NfcReadData.this);
        rInvoker.setCurrentContext(NfcReadData.this);

        String completeSvcStateUrl = svcUrlStr + "/rest/nfcService/getDetails";
        System.out.println("Sreeni : completeSvcStateUrl is " + completeSvcStateUrl);
        rInvoker.setURL(completeSvcStateUrl);
        rInvoker.execute();
    }

    private Boolean isValidInputs(){

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
            tv.setText("Waiting for the NFC Tag"+"\n"+timeLeft.toString());
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
            nfcAdpt.enableForegroundDispatch(NfcReadData.this, pendingIntent, intentFiltersArray, techList);
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
        AlertDialog alertDialog = new AlertDialog.Builder(NfcReadData.this).create();

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
    public void clearDetails(){

        EditText editText = (EditText) findViewById(R.id.etManf);
        EditText editText1 = (EditText) findViewById(R.id.etPName);
        EditText editText2 = (EditText) findViewById(R.id.etSNum);
        EditText editText3 = (EditText) findViewById(R.id.etPDesc);
        EditText editText4 = (EditText) findViewById(R.id.etDnT);

        editText.setText("");
        editText1.setText("");
        editText2.setText("");
        editText3.setText("");
        editText4.setText("");
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
        EditText editText4 = (EditText) findViewById(R.id.etDnT);

        editText.setVisibility(visibilityVal);
        editText1.setVisibility(visibilityVal);
        editText2.setVisibility(visibilityVal);
        editText3.setVisibility(visibilityVal);
        editText4.setVisibility(visibilityVal);


        Button btn1 = (Button)findViewById(R.id.btnScan);
        btn1.setVisibility(visibilityVal);

        Button btn2 = (Button) findViewById(R.id.bClear);
        btn2.setVisibility(visibilityVal);

        Button btn3 = (Button) findViewById(R.id.btnLogout);
        btn3.setVisibility(visibilityVal);

        TextView txtView1 = (TextView) findViewById(R.id.tvText);
        TextView txtView2 = (TextView) findViewById(R.id.tvPInfo);
        TextView txtView3 = (TextView) findViewById(R.id.tvManf);
        TextView txtView4 = (TextView) findViewById(R.id.tvPName);
        TextView txtView5 = (TextView) findViewById(R.id.tvPDesc);
        TextView txtView6 = (TextView) findViewById(R.id.tvSNum);
        TextView txtView7 = (TextView) findViewById(R.id.tvDnT);
        TextView txtView8 = (TextView) findViewById(R.id.tvUsername);
        TextView txtView9 = (TextView) findViewById(R.id.textView);



        txtView1.setVisibility(visibilityVal);
        txtView2.setVisibility(visibilityVal);
        txtView3.setVisibility(visibilityVal);
        txtView4.setVisibility(visibilityVal);
        txtView5.setVisibility(visibilityVal);
        txtView6.setVisibility(visibilityVal);
        txtView7.setVisibility(visibilityVal);
        txtView8.setVisibility(visibilityVal);
        txtView9.setVisibility(visibilityVal);

    }
}
