package project.child_safety;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.renderscript.Double2;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import project.child_safety.LocationUtil.LocationHelper;
import project.child_safety.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private static final int SMS_PERMISSION_CODE = 0;
    private GoogleMap mMap;
    private String mUserMobilePhone = "9901365112";
    private String mFakeNumber = "9686630129";
    private String mReplyNumber = "9901365112";

    private Location mLastLocation;

    double latitude;
    double longitude;

    LocationHelper locationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!hasReadSmsPermission()) {
            showRequestPermissionsInfoAlertDialog();
        }

        initViews();

//        locationHelper=new LocationHelper(this);
//        locationHelper.checkpermission();

        SmsBroadcastReceiver.bindListener(new SmsListener() {
            @Override
            public void messageReceived(String messageText) {
                // send location
                // get loction here
                // use lat long var
                if (messageText.equals("@request#")) {
                    latitude = 12.9028257;
                    longitude = 77.5119573;
                    String one = "CHILD LOCATION AT ";
                    String two = ",https://www.google.com/maps/?q=";
                    String message1 = one + two + latitude + ',' + longitude;
                    SmsHelper.sendDebugSms(String.valueOf(mFakeNumber), message1);
                }
                else {
                    // display in map
                    String latlong = messageText.substring(messageText.lastIndexOf("=") + 1);
                    String array[] = latlong.split(",");
                    latitude = Double.parseDouble(array[0]);
                    longitude = Double.parseDouble(array[1]);
                    LatLng newMarker = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(newMarker).title("Christ University"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newMarker, 15));
                }
            }
        });
    }

    private void initViews() {
//        findViewById(R.id.btn_normal_sms).setOnClickListener(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        findViewById(R.id.btn_conditional_sms).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
//        mLastLocation=locationHelper.getLocation();
//
//        if (mLastLocation != null) {
//            latitude = mLastLocation.getLatitude();
//            longitude = mLastLocation.getLongitude();
//            Log.d("yo", latitude + "");
//        } else {
//            Log.d("yo", "ERROR");
//        }

        switch (v.getId()) {
            case R.id.btn_conditional_sms:
                if (!hasValidPreConditions()) return;

//                SmsHelper.sendDebugSms(String.valueOf(mUserMobilePhone), "@request#");
                SmsHelper.sendDebugSms(String.valueOf(mUserMobilePhone), "@request#");
                Toast.makeText(getApplicationContext(), R.string.toast_sending_sms, Toast.LENGTH_SHORT).show();
                break;
        }
    }




    /**
     * Validates if the app has readSmsPermissions and the mobile phone is valid
     *
     * @return boolean validation value
     */
    private boolean hasValidPreConditions() {
        if (!hasReadSmsPermission()) {
            requestReadAndSendSmsPermission();
            return false;
        }

        if (!SmsHelper.isValidPhoneNumber(mUserMobilePhone.toString())) {
            Toast.makeText(getApplicationContext(), R.string.error_invalid_phone_number, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Optional informative alert dialog to explain the user why the app needs the Read/Send SMS permission
     */
    private void showRequestPermissionsInfoAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permission_alert_dialog_title);
        builder.setMessage(R.string.permission_dialog_message);
        builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                requestReadAndSendSmsPermission();
            }
        });
        builder.show();
    }

    /**
     * Runtime permission shenanigans
     */
    private boolean hasReadSmsPermission() {
        return ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestReadAndSendSmsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_SMS)) {
            Log.d(TAG, "shouldShowRequestPermissionRationale(), no permission requested");
            return;
        }
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS},
                SMS_PERMISSION_CODE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney "));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}