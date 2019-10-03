package lt.bit.geospy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;



public class MainActivity extends AppCompatActivity {

    private String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    public static Double latitude;
    public static Double longitude;




    @Override
    protected void onResume() {
        super.onResume();


    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.d("GPS", "Gautapozicija: " + location.getLatitude() + " " + location.getLongitude());
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        WebView webView =findViewById(R.id.web);
//                        webView.getSettings().setJavaScriptEnabled(true);
//                        webView.addJavascriptInterface(new JavaScriptInterface(getApplicationContext()), "Android");
//                        webView.loadUrl("file:///android_asset/map.html");
//                    }
//                });
                //Todo send to database

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
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);


        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, 15000, 100.0f, mLocationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode== 123){
            if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
            }
        }
    }






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        //Todo generate API key at the first launch
        getCurrentLocation();
        WebView webView =findViewById(R.id.web);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JavaScriptInterface(getApplicationContext()), "Android");
        webView.loadUrl("file:///android_asset/map.html");



        TextView title = findViewById(R.id.titlename);
        TextView loading = findViewById(R.id.loading);
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(150);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        title.startAnimation(anim);

        Thread thread = new Thread(new Map(this, webView, loading, title));
        thread.start();

    }

}