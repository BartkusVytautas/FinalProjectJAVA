package lt.bit.geospy;

import android.app.Activity;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

public class Map implements Runnable {

    private Activity activity;
    private WebView webView;
    private TextView loading;
    private TextView header;

    public Map(Activity activity, WebView webView, TextView loading, TextView header) {
        this.activity = activity;
        this.webView = webView;
        this.loading = loading;
        this.header = header;
    }

    @Override
    public void run() {
        try {
            synchronized (this) {
                wait(10000);
                this.activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            loading.setVisibility(View.GONE);
                            header.setVisibility(View.GONE);
                            webView.setVisibility(View.VISIBLE);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
