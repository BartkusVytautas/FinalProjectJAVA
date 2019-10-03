package lt.bit.geospy;

import android.app.Activity;
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class JavaScriptInterface {

    protected Context context;


    public JavaScriptInterface(Context context)  {
        this.context = context;


    }

    @JavascriptInterface
    public void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

    }

    @JavascriptInterface
    public double getLatitude() {
        return MainActivity.latitude;
    }

    @JavascriptInterface
    public double getLongitude() {
        return MainActivity.longitude;
    }
}
