package cl.rojas.dulcemar;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (hayconexion((Splash.this))){
                        startActivity(new Intent(Splash.this, MainActivity.class));
                        finish();
                    }else {
                        AlertDialog.Builder miAlert = new AlertDialog.Builder(Splash.this);
                        miAlert.setTitle("No hay acceso a Internet");
                        miAlert.setMessage("Por favor, activa los datos o e Wifi para usar la app");
                    }
                }
            },1000);
    }


    public boolean hayconexion(Context context){
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnectedOrConnecting()){
            NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if ((wifi != null && wifi.isConnectedOrConnecting()) ||
                    (mobile != null && mobile.isConnectedOrConnecting())){
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }
    }
}