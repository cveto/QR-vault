package csbslovenia.com.paper_vault;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;


public class MainMenu extends Activity {
    private static final String TAG = "MainMenu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);



        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
       /*
        if (id == R.id.action_settings) {
            return true;
        }
        */
        return super.onOptionsItemSelected(item);
    }

    public void startEncryptionActivity(View view) {
        Intent intent = new Intent(this, startEncryptionActivity.class);
        startActivity(intent);
    }

    /**Lanuch Zxing QR scanner**/
    // Launches QR Scanner

    public void launchQRScanner(View view) {
        IntentIntegrator integrator;
        integrator = new IntentIntegrator(this);
        integrator.setScanningMessage(getResources().getString(R.string.scanCiphertext));
        integrator.initiateScan();
    }

    /** This is what happens on Zxing activity result **/
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        // if scan result successful and message at least 30 characters long (Salt + IV length). With BIG QR messages it sometimes reads some small number by accident.
        if (scanResult.getContents() != null && scanResult.getContents().length() > 30) {
            // Inteding to go to next activity
            Intent startReadingIntent = new Intent(MainMenu.this, startReadingActivity.class);
            // Taking chipertext with me
            startReadingIntent.putExtra("gnoj", scanResult.getContents());
            // Ready, set, go!
            startActivity(startReadingIntent);
        } else if (scanResult.getContents() != null) {
            customToast(getResources().getString(R.string.invalidQRcode));
            return;
        } else {
            return;
        }
    }

    public void manuallySetLocale(MenuItem mi) {
        finish();

        // What is current locale?
        String lang = "jp";
        if (Locale.getDefault().getDisplayLanguage().equals("English")) {
            lang = "jp";
        } else if (Locale.getDefault().getDisplayLanguage().equals("jp")) {
            lang = "English";
        }
        Log.d(lang,Locale.getDefault().getDisplayLanguage());
         // SetLocale
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        // End of set locale
        startActivity(new Intent(MainMenu.this, MainMenu.class));
    }

    public void customToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

}
