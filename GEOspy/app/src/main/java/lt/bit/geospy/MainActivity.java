package lt.bit.geospy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import com.loopj.android.http
.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {

    public static Double latitude;
    public static Double longitude;
    public static Timestamp lastUpdated;

    LocationListener mLocationListener=null;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getCurrentLocation();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        SharedPreferences sharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean firstStart = sharedPreferences.getBoolean("firstStart", true);
        if(firstStart) {
            sendApi();
        }
        getCurrentLocation();
        final WebView webView = findViewById(R.id.web);
        final TextView title = findViewById(R.id.titlename);
        final TextView loading = findViewById(R.id.loading);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Animation anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(150);
                anim.setStartOffset(20);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
                title.startAnimation(anim);
            }
        });
        Thread thread = new Thread(new Map(this, webView, loading, title));
        thread.start();
    }


    public void start() {
        final WebView webView = findViewById(R.id.web);

        if (MainActivity.longitude != null && MainActivity.latitude != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.addJavascriptInterface(new JavaScriptInterface(getApplicationContext()), "Android");
                    webView.loadUrl("file:///android_asset/map.html");

                }
            });

        }

    }

    @SuppressLint("MissingPermission")
        private void getCurrentLocation () {

            String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;
            LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (mLocationListener==null) {
                mLocationListener = new LocationListener() {

                    @Override
                    public void onLocationChanged(Location location) {
                        Timestamp currentTime = new Timestamp(new Date().getTime());
                        if (lastUpdated == null) {
                            lastUpdated = currentTime;
                        }
                        if ((currentTime.getTime() - lastUpdated.getTime()) >= 10000) {

                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Log.d("GPS", "Gautapozicija: " + location.getLatitude() + " " + location.getLongitude());
                            sendCoordinatesToServer(latitude.toString(), longitude.toString());
                        }
                        lastUpdated = currentTime;

                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        Log.d("GPS", "GPS isjungtas");
                    }
                };
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, 10000, 20.0f, mLocationListener);
        }

    public void sendApi(){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams rp=new RequestParams();
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String api = "emulator_api";
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        try {
            api = telephonyManager.getDeviceId();
            Toast.makeText(getApplicationContext(),api,
                    Toast.LENGTH_LONG).show();
        }catch (SecurityException e){
            Toast.makeText(getApplicationContext(),"Couldn't connect your phone to server, please contact support.",
                    Toast.LENGTH_LONG).show();


        }
        editor.putString("api", api);
        editor.apply();

        rp.add("key", "" + api);

        client.post("http://pabegeliai.lt:8080/geospy/api/add_new_api",
                rp, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            System.out.println(response.toString());
                            if(response.getString("Success:").equals("Already exists") || response.getString("Success").equals("New device added successfully")) {
                                Toast.makeText(getApplicationContext(), "Your phone connected to server successfully",
                                        Toast.LENGTH_LONG).show();
                                SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean("firstStart", false);
                                editor.putString("api", response.getString("api"));
                                editor.apply();
                            }else{
                                Toast.makeText(getApplicationContext(),"Couldn't connect your phone to server, please contact support.",
                                        Toast.LENGTH_LONG).show();
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                });
    }

    public void sendCoordinatesToServer(String latitude, String longitude){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams rp=new RequestParams();
        SharedPreferences sharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE);
        String api = sharedPreferences.getString("api", "emulator_api");
        rp.add("latitude", "" + latitude);
        rp.add("longitude", "" + longitude);
        rp.add("key", api);
        System.out.println(api);
        client.post("http://pabegeliai.lt:8080/geospy/api/add_coordinates",
                rp, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            if(response.getString("success").equals("true")) {
                                Toast.makeText(getApplicationContext(), "Coordinates sent",
                                        Toast.LENGTH_SHORT).show();

                            }else{
                                Toast.makeText(getApplicationContext(),"Couldn't send coordinates",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(),"Couldn't send coordinates",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
}
