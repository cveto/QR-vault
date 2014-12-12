package csbslovenia.com.paper_vault;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;

import javax.crypto.SecretKey;


public class startEncryptionActivity extends Activity {
    // Limit of characters for EditText
    private static int CHLIMITTITLE = 30;
    private static int CHLIMITTEXT = 2000;      // What about Kanji? ASCII Takes 1 byte, žčćđšp take 2, 空揚 takes 3 each! Nexto timeo.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_encryption);

        /** If Editing - get Extras from previous activity **/
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();

        // Define EditTexts (for setting length and potential contents)
        EditText textToEncrypt = (EditText)findViewById(R.id.et_textToBeEncrypted);
        EditText password = (EditText)findViewById(R.id.et_password);
        EditText title = (EditText)findViewById(R.id.et_title);

        setEditTextMaxLength(title,CHLIMITTITLE);
        setEditTextMaxLength(textToEncrypt,CHLIMITTEXT);

        // If previous activity was startReadingActivity.
       if (extras != null && extras.containsKey("activity") && extras.getString("activity").equals("startReadingActivity")) {
           textToEncrypt = (EditText)findViewById(R.id.et_textToBeEncrypted);
           title = (EditText)findViewById(R.id.et_title);
           textToEncrypt.setText(extras.getString("text"));
           password.setText(extras.getString("password"));
           title.setText(extras.getString("title"));
       }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start_encryption, menu);
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

    public void encryptAndDisplay(View view) {
        /**Get title**/
        // Add unencrypted Title to ciphertext. Transform to base64 first
        EditText et_title = (EditText)findViewById(R.id.et_title);
        String st_title = et_title.getText().toString();

        // Check if Title entered
        if (st_title.matches("")) {
            customToast(getResources().getString(R.string.et_enterTitle));
            return;
        }

        /** Get text ***/
        EditText et_textToBeEncrypted = (EditText) findViewById(R.id.et_textToBeEncrypted);
        String st_textToBeEncrypted = et_textToBeEncrypted.getText().toString();

        // Check if any text entered
        if (st_textToBeEncrypted.matches("")) {
            customToast("Your message?");
            return;
        }

        /** Get password **/
        EditText et_password = (EditText) findViewById(R.id.et_password);
        char[] ch_password = et_password.getText().toString().toCharArray();

        // Check if password entered. Try not to save password to a string.
        if (et_password.getText().toString().matches("")) {
            customToast("No password?");
            return;
        }

        /** ENCRYPT DATA: Using AES-256 **/
        customToast("hehe");

        /// First create key for AES derived from user-password.
            // create salt
        final byte[] S = Crypto.generateSalt();
            // create key from password and salt. Name it DK for Derived Key.
        SecretKey DK = Crypto.deriveKeyPbkdf2(S,ch_password);
            // Encrypt using key, salt and text. You shall receive salt]IV]ciphertext in BASE_64_NoWrap readable form.
        String st_chiperText = Crypto.encrypt(st_textToBeEncrypted,DK,S);

        /** Concat title to the chipertext**/
        st_chiperText = st_chiperText.concat("]"+Crypto.toBase64(st_title.getBytes(Charset.forName("UTF-8"))));

        /** Go to next QRcode creation **/
        Intent intent = new Intent(this, GenerateQRCodeActivity.class);
        // Take chipertext with you and pass it on.
        intent.putExtra("gnoj",st_chiperText);
        intent.putExtra("title",st_title);
        startActivity(intent);
        // Transition
        //overridePendingTransition(R.anim.slideleft,R.anim.slideleft);
        finish();
    }

    public void ShowSchool(View view) {     // this is a stupid name. It just puts up information about what a good password looks like.
        Intent intent = new Intent(this,School.class);
        startActivity(intent);
    }

    // What is the maximum characters user can enter?
    public void setEditTextMaxLength(EditText editText, Integer maxLength) {
            // Create a new InputFilter to define the maximum length
            InputFilter maxLengthFilter = new InputFilter.LengthFilter(maxLength);
            // Apply the filter to the EditText. The array can contain other filters.
            editText.setFilters(new InputFilter[]{ maxLengthFilter });
    }

    public void customToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
}
