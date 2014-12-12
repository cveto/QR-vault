package csbslovenia.com.paper_vault;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;

public class startReadingActivity extends Activity {

static String st_qrData;
private static final String TAG = "startReadingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_reading);

        /** get chipertext from previous Activity. It was passed on, grab it! **/
        Intent myIntent = getIntent();
        st_qrData = myIntent.getExtras().getString("gnoj");

        /**Get title out of the chipertext. Do the whole Base64 decoding utf encoding thingy**/
        String st_title=null;
        // IF QR CODE IS NOT A VALID PAPERVAULT CODE - This should probably be improved. It only checks if there are 4 concatenated  datas inside.
        try {
            st_title = getTitle(st_qrData);
        } catch (Exception e) {
            Log.d(TAG,"Couldent get title out of the QR. Not a valid qr code?");
            customToast(getResources().getString(R.string.invalidQRcodeTitle));
            finish();
            return;
        }

        st_title = new String(Crypto.fromBase64(st_title),Charset.forName("UTF-8"));
        // Display title of the document
        TextView tv_title = (TextView)findViewById(R.id.tv_title);
        tv_title.setText(st_title);

        /**Make textviews Scrollable**/
        TextView tv_decrypted = (TextView) findViewById(R.id.tv_decrypted);
        tv_decrypted.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start_reading, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void decryptAndDisplay(View view) {

        /**Get password**/
        EditText etPassword = (EditText) findViewById(R.id.etPassword);

        /**Get chipertext**/
        // is a static variable set on onCreate.

        /**Empty results**/
        TextView tv_decrypted = (TextView) findViewById(R.id.tv_decrypted);
        tv_decrypted.setHint("Decrypted text will be shown here.");

        /**Decrypt**/
        try {
            /**Remove Title from chiper text. Title is not encrypted, just base64*/
            // It is put in try catch, so the app does not crash if the number of data is not 4.

            // Probably there should be a better way of checking if this code is a valid Paper-Vault code. (Signature?)
            String st_chiperText = removeTitleFromChipertext(st_qrData);

            /**Decrypt and return in char array.**/
            String stDecrypted = Crypto.decryptPbkdf2(st_chiperText, etPassword.getText().toString().toCharArray());

            /**Show decrypted message**/
            tv_decrypted = (TextView) findViewById(R.id.tv_decrypted);
            tv_decrypted.setText(stDecrypted);

            /**Make password input uneditable after successful decryption**/
            //etPassword.setText("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");       //This fucked up the Edit section.
            etPassword.setEnabled(false);

            /**Make button Decrypt unclickable after successful decryption**/
            Button but_decrypt = (Button) findViewById(R.id.but_decrypt);
            but_decrypt.setEnabled(false);

            /**Make button Edit clickable after successful decryption**/
            Button but_enable = (Button) findViewById(R.id.but_edit);
            but_enable.setEnabled(true);

            customToast("Decryption successful!");

        } catch (Exception e) {
            tv_decrypted.setHint(getResources().getString(R.string.incorrectPassword));
            Toast.makeText(getApplicationContext(), "Password incorrect", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void editMessage(View view) {
        /** Find password and text **/
        EditText password = (EditText)findViewById(R.id.etPassword);
        TextView text = (TextView)findViewById(R.id.tv_decrypted);
        TextView title = (TextView)findViewById(R.id.tv_title);

        /** Take password and text and activity name with you to next intent.**/
        Intent intent = new Intent(this, startEncryptionActivity.class);
        intent.putExtra("activity","startReadingActivity");
        intent.putExtra("text",text.getText().toString());
        intent.putExtra("password",password.getText().toString());
        intent.putExtra("title",title.getText().toString());

        startActivity(intent);
        finish();
    }

    public String getTitle(String st_qrData) {
        String[] fields = st_qrData.split("]");
        if (fields.length != 4) {
            throw new IllegalArgumentException("Number of datas not 4. getTitle method.");
        }
        return fields[3];
    }

    public String removeTitleFromChipertext(String st_qrData) {
        String[] fields = st_qrData.split("]");
        if (fields.length != 4) {
            throw new IllegalArgumentException("Number of datas not 4");
        }

        // concat
        return String.format("%s%s%s%s%s", fields[0], "]",fields[1],"]",fields[2]);
    }

    public void customToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
}
