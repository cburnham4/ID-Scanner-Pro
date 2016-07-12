package letshangllc.idscanner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.idscan.android.dlparser.DLParser;
import net.idscan.android.pdf417scanner.PDF417ScanActivity;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final static int SCAN_ACTIVITY_CODE = 0x001;
    private DLParser parser = null;

    /* Views */
    private TextView tvAge;
    private TextView tvName;
    private TextView tvBirthday;
    private TextView tvExpires;
    private TextView tvAddress;
    private TextView tvCity;
    private LinearLayout cvLinLayout;
    private TextView tvNoData;
    private EditText etAge;

    /* Required Age */
    private int requiredAge = 21;
    private int scannedAge = 0;

    /**
     * License key for scanner and parser.
     */

    //private static final String SCANNER_KEY;
    //private static final String PARSER_KEY = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAge = (TextView) findViewById(R.id.tvAge);
        tvName = (TextView) findViewById(R.id.tvName);
        tvBirthday= (TextView) findViewById(R.id.tvBirthday);
        tvExpires= (TextView) findViewById(R.id.tvExpires);
        tvAddress= (TextView) findViewById(R.id.tvAddress);
        tvCity= (TextView) findViewById(R.id.tvCity);
        etAge = (EditText) findViewById(R.id.etAge);
        cvLinLayout = (LinearLayout) findViewById(R.id.cvLinLayout);
        tvNoData =(TextView) findViewById(R.id.tvNoData);


        Button _btn_scan = (Button) findViewById(R.id.btn_scan);
        _btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, PDF417ScanActivity.class);
                i.putExtra(PDF417ScanActivity.EXTRA_LICENSE_KEY, getString(R.string.scanner_key));

                startActivityForResult(i, SCAN_ACTIVITY_CODE);
            }
        });

        this.setupToolbar();

        this.runAds();
    }

    private void parseData(){
        parser = new DLParser();
        cvLinLayout.setVisibility(View.VISIBLE);
        tvNoData.setVisibility(View.GONE);
        try {
            //setup parser.
            parser.setup(getApplicationContext(), getString(R.string.parser_key));
            //make parse the data.
            DLParser.DLResult res = parser.parse(tvName.getText().toString().getBytes("UTF8"));

//            res.firstName = "Bobby";
//            res.middleName = "Richard";
//            res.lastName = "Richardson";
//
//            res.birthdate = "04/16/1996";
//            res.expirationDate = "07/10/2020";
//            res.address1 = "716 McMaster Dr.";
//            res.city = "Portland";
//            res.jurisdictionCode = "OR";

            tvName.setText(String.format(Locale.getDefault(), "%s %s %s",res.firstName,res.middleName,res.lastName));

            if(!isLegal(res.birthdate)){
                Toast.makeText(this, "Not of Age", Toast.LENGTH_SHORT).show();
                tvBirthday.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                tvAge.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }else{
                tvAge.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                tvBirthday.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            }

            if(scannedAge ==0){
                tvAge.setText("N/A");
            }else{
                tvAge.setText(String.format(Locale.getDefault(), "%d", scannedAge));
            }

            if(isExpired(res.expirationDate)){
                Toast.makeText(this, "Expired", Toast.LENGTH_SHORT).show();
                tvExpires.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }else {
                tvExpires.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            }

            tvBirthday.setText(res.birthdate);
            tvExpires.setText(res.expirationDate);
            tvAddress.setText(res.address1);
            tvCity.setText( res.city + ", " + res.jurisdictionCode);

        } catch (DLParser.DLParserException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private boolean isLegal(String birthdate){
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

        try{
            Date date = formatter.parse(birthdate);
            Date dateNow = new Date();

            this.scannedAge = getDiffYears(date, dateNow);
            if(scannedAge >= requiredAge){
                Log.i(TAG, "LEGAL");
                return true;
            }else{
                return false;
            }
        }catch (ParseException e){
            e.printStackTrace();
            Toast.makeText(this, "The age could not be determined", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    private boolean isExpired(String expirationDate){
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

        try{
            Date date = formatter.parse(expirationDate);
            Date dateNow = new Date();
            if(dateNow.after(date)){
                Log.i(TAG, "Expired");
                return true;
            }else{
                return false;
            }
        }catch (ParseException e){
            e.printStackTrace();
            Toast.makeText(this, "The expiration could not be determined", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    public static int getDiffYears(Date first, Date last) {
        Calendar a = Calendar.getInstance();
        a.setTime(first);
        Calendar b = Calendar.getInstance();
        b.setTime(last);
        int diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR);
        if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) ||
                (a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE))) {
            diff--;
        }
        return diff;
    }

    public void subOneOnClick(View view){
        String ageString = etAge.getText().toString().trim();
        if(ageString.isEmpty()){
            Toast.makeText(this, "The required age is blank", Toast.LENGTH_SHORT).show();
        }else {
            try{
                int age = Integer.parseInt(ageString);
                etAge.setText(String.format(Locale.getDefault(), "%d", --age));
                this.requiredAge = age;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void addOneOnClick(View view){
        String ageString = etAge.getText().toString().trim();
        if(ageString.isEmpty()){
            Toast.makeText(this, "The required requiredAge is blank", Toast.LENGTH_SHORT).show();
        }else {
            try{
                int age = Integer.parseInt(ageString);
                etAge.setText(String.format(Locale.getDefault(), "%d", ++age));
                this.requiredAge = age;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCAN_ACTIVITY_CODE) {
            if (resultCode == PDF417ScanActivity.RESULT_OK) {
                if (data != null) {
                    tvName.setText(data.getStringExtra(PDF417ScanActivity.BARCODE_DATA));
                    parseData();
                }
            } else if (resultCode == PDF417ScanActivity.ERROR_INVALID_CAMERA_NUMBER) {
                tvName.setText("Invalid camera number.");
            } else if (resultCode == PDF417ScanActivity.ERROR_CAMERA_NOT_AVAILABLE) {
                tvName.setText("Camera not available.");
            } else if (resultCode == PDF417ScanActivity.ERROR_INVALID_CAMERA_ACCESS) {
                tvName.setText("Invalid camera access.");
            } else if (resultCode == PDF417ScanActivity.ERROR_INVALID_LICENSE_KEY) {
                tvName.setText("Invalid license key.");
            }
        }
    }

    public void setupToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Id Scanner");
    }

    private AdsHelper adsHelper;
    public void runAds(){
        adsHelper =  new AdsHelper(getWindow().getDecorView(), getResources().getString(R.string.admob_id), this);

        adsHelper.setUpAds();
        int delay = 1000; // delay for 1 sec.
        int period = getResources().getInteger(R.integer.ad_refresh_rate);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                adsHelper.refreshAd();  // display the data
            }
        }, delay, period);
    }
}
