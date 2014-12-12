package csbslovenia.com.paper_vault;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.print.PrintHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;


public class GenerateQRCodeActivity extends Activity {
    // for file deletion
    static File file;

    // For saving to Flash
    static String st_title = null;

    // A4 document: 300DPI
    static int bitmapWidth = 2481;
    static int bitmapHeight = 3510;

    // Create bitmap
    static Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qrcode);

        /** get chipertext and more from previous Activity. It was passed on, grab it! **/
        Intent myIntent = getIntent();
        String st_chiperText = myIntent.getExtras().getString("gnoj");
        st_title = myIntent.getExtras().getString("title");
        String st_bitmapBottom = this.getString(R.string.bitmap_bottom);
        String st_pencilLines = this.getString(R.string.pencil_lines);
        String st_notes = this.getString(R.string.notes);

        /** Create a document **/
        // Create Paint canvas on the bitmap, so you can draw on it.
        Canvas canvas = new Canvas(bitmap);
        // Create paing, a way to draw on the canvas
        Paint paint = new Paint();

        /** Drawing **/
        // Document Background
        paint.setARGB(250, 249, 249, 249);  // light gray, no transparency
        canvas.drawRect(0, 0, bitmapWidth, bitmapHeight, paint);

        // Document title
        int titlePosTop = 350;
        int titlePosLeft = 200;
        int titleTextSize = 100;

        paint.setColor(Color.BLACK);
        paint.setTextSize(titleTextSize);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/URANIA_CZECH.ttf");   //Typeface tf = Typeface.create("monospace",Typeface.BOLD);
        paint.setTypeface(tf);
        canvas.drawText(st_title, titlePosLeft, titlePosTop, paint);

        // Notes:
        int y = titlePosTop+200;
        paint.setColor(Color.LTGRAY);
        paint.setTextSize(40);
        canvas.drawText(st_notes, titlePosLeft,y, paint);

        // Lines for pencil
        int numberOfLines= 8;
        y-=(paint.ascent()+paint.descent())*25/10;
        for(Integer i = 0; i<numberOfLines; i++) {     //delimiter is \n with a space after it. This is to remove an added space on every new line.
            canvas.drawText(st_pencilLines, titlePosLeft,y, paint);
            y-=(paint.ascent()+paint.descent())*25/10;
        }

        /** Text on the bitmap **/
        // Where text should be?
        int xLeft = 250;
        int xRight = bitmapWidth-xLeft;
        int yTop = 2550;

        // Text Details
        int textSize = 50;
        float textSpacing = (float) 1.2;

        paint.setTextSize(textSize);
        paint.setColor(Color.BLACK);
        paint.setSubpixelText(true);

        String text = st_bitmapBottom;
        float textHeight = drawJustifiedText(text,textSize,textSpacing, xLeft, xRight, yTop, paint,canvas, false);


        /*
        // Document description
        paint.setTextSize(50);
        int x = 200;
        y = 2750;
        Integer cutText = 40;
        Integer missingChars = cutText - st_bitmapBottom.length() % cutText;
        // make char length dividible by 70, concat blank chars at the end
        for (int i=0;i<missingChars;i++){
            st_bitmapBottom = st_bitmapBottom.concat(" ");
        }
        // New lines with \n
        for(String line: st_bitmapBottom.split("\n ")){     //delimiter is \n with a space after it. This is to remove an added space on every new line.
            canvas.drawText(line, x, y, paint);
            y-=(paint.ascent()+paint.descent())*14/10;
        }
        */

        // Green line around QR code. Need to know the QR size before making the line around it.
        int qrWidth = 1000;
        int qrHeight = qrWidth;
        int qrLineWidth = 25;

        int qrPosTop = bitmapHeight / 2 - qrHeight / 2;
        int qrPosBottom = bitmapHeight / 2 + qrHeight / 2;
        int qrPosLeft = bitmapWidth / 2 - qrWidth / 2;
        int qrPosRight = bitmapWidth / 2 + qrWidth / 2;
        paint.setColor(Color.GREEN);
        canvas.drawRect(qrPosLeft - qrLineWidth, qrPosTop - qrLineWidth, qrPosRight + qrLineWidth, qrPosBottom + qrLineWidth, paint);

        // QR CODE
        int[] pixels = getQRpixels(st_chiperText, qrWidth, qrHeight, "UTF-8");
        bitmap.setPixels(pixels, 0, qrWidth, qrPosLeft, qrPosTop, qrWidth, qrHeight);

        //Hanko - place close to text
        Bitmap hanko = BitmapFactory.decodeResource(this.getResources(),R.drawable.hanko300dp1cm);
        int hankoWidth = hanko.getWidth();
        int hankoX = xRight-hankoWidth/2;
        float hankoY = textHeight;

        canvas.drawBitmap(hanko,hankoX,hankoY,null);

        // Compression
        //ByteArrayOutputStream compressedImage = new ByteArrayOutputStream();
        //bitmap.compress(Bitmap.CompressFormat.PNG, 100, compressedImage);
        //bitmap.compress(Bitmap.CompressFormat.JPEG, 1, compressedImage);

        // Decompression
        //byte[] byteArray = compressedImage.toByteArray();
        //Bitmap createdPNG = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);

        ImageView myImage = (ImageView) findViewById(R.id.imageView1);
        myImage.setImageBitmap(bitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        if (Build.VERSION.SDK_INT > 15) {
            getMenuInflater().inflate(R.menu.generate_qrcode, menu);
        } else {
            MenuInflater inflater = new MenuInflater(this);
            inflater.inflate(R.menu.generate_qrcode, menu);
        }
        return true;
    }

    /**ZXING magic**/
    //*** ZXing library for creating a QR code. Not yet in bitmap form.
    public int[] getQRpixels(String input, int width, int height, String encoding) {
        //Size of pixels array
        int[] pixels = new int[width * height];

        // set QR background and pixel color
        final int WHITE = 0xFFFFFFFF;
        final int BLACK = 0xFF000000;

        Map<EncodeHintType, Object> hints = null;

        hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, encoding);

        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix result = writer.encode(input, BarcodeFormat.QR_CODE, width, width, hints);

            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
                }
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return pixels;
    }

    /** ®Code for Justifying Text® **/
    //Example how to use the code
    private void showBitmap() {
        int bitHeight = 400;
        int bitWidth = 800;

        Bitmap bitmap = Bitmap.createBitmap(bitWidth,bitHeight,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        // Where text should be?
        int xLeft = 150;
        int xRight = 650;
        int yTop = 50;

        // Background for text
        paint.setColor(Color.YELLOW);
        canvas.drawRect(xLeft, yTop, xRight, bitHeight, paint);

        // Text Details
        int textSize = 15;
        float textSpacing = (float) 1.2;

        Typeface tf =Typeface.createFromAsset(getAssets(),"fonts/URANIA_CZECH.ttf");
        paint.setTypeface(tf);

        paint.setTextSize(textSize);
        paint.setColor(Color.BLACK);
        paint.setSubpixelText(true);      // because breakText counts wrong otherwise.

        // What Text
        String text = "Včeraj je bi   l en lep sončen dan. Zelo je škoda, da ni tako tu    ostalo. Namreč, pri v življenju takemu je obvezno, da se za žgance da precej še storiti. Kdor meni jamo koplje seveda" +
                "ta tudi sam vanjo    pade. Ni pa moč reči, da je          s tem kaj narobe. Zdeleč od tega. Trololo Tralala hopsas    a in če tako mi pravimo potem bo tudi tako res. kaj ne mislita tako? Nekoč se si domišljal. da je vse pravzaprav le ena" +
                " velika nadležna laž.   Hi hi Ho ho. Predam predam se vam, ki ste bolan.";
        //text = "a b c";

        // Put text on canvas
        drawJustifiedText(text,textSize,textSpacing, xLeft, xRight, yTop, paint,canvas, false);

        // Show bitmap on Android
        //ImageView imageView = (ImageView)findViewById(R.id.image);
        //imageView.setImageBitmap(bitmap);


    }

    private float drawJustifiedText(
            String text,
            int textSize,
            float textSpacing,
            int xLeft,
            int xRight,
            int yTop,
            Paint paint,
            Canvas canvas,
            Boolean justifyShortLine) {


        // Starting conditions
        String text_line = "";
        String text_rest = text;

        // Top part of text starts there, not bottom.
        yTop = yTop+textSize*3/4;

        //  How many fit?
        int maxTextOnLine = paint.breakText(text_rest, 0, text_rest.length(), true, xRight - xLeft, null);

        // Counter for y position
        int i = 0;

        /**For text that doesn't fit in one line**/
      if (text_rest.length() > maxTextOnLine+1) {         //why + 1? It was putting half lines on new line. needs testing
                do {
                //How many characters fit on the line?
                maxTextOnLine = paint.breakText(text_rest, 0, text_rest.length(), true, xRight - xLeft, null);

                // Find where to remove partial word.
                while (text_rest.charAt(maxTextOnLine-1) != " ".charAt(0)) {
                    maxTextOnLine--;
                }

                // Implement delimiter for new line /n
                    // Yet to be implemented

                // Split string on two strings - Remove partial word.
                text_line = text_rest.substring(0, maxTextOnLine);
                text_rest = text_rest.substring(maxTextOnLine, text_rest.length());

                // Draw jutified Text
                justifyCalculation(text_line,xLeft,xRight,yTop,textSize,textSpacing,paint,canvas,i);

                // increment for next line
                i++;
            }  while (text_rest.length() > text_line.length());
        }

        /** For Last or Only line. **/
        text_line = text_rest;
        if (justifyShortLine) {
            justifyCalculation(text_line,xLeft,xRight,yTop,textSize,textSpacing,paint,canvas,i);
        } else {
            // No justification
            canvas.drawText(text_line, 0, text_line.length(), xLeft, yTop + i * textSize * textSpacing, paint);
        }
        return yTop + i * textSize * textSpacing;
    }

    private void justifyCalculation(String text_line,int xLeft,int xRight,int yTop,int textSize,float textSpacing,Paint paint, Canvas canvas, int i) {
        // Create array of words
        String[] words = createArrayOfWords(text_line);
        int numberOfWords = words.length;
        int numberOfSpaces = numberOfWords-1;

        // Length of the words without spaces;
        float lenWordsNoSpaces = measureLenOfStringsInStringArray(words,paint);

        // How much whitespace is there to fill?
        float emtpySpaceInLine = xRight-xLeft-lenWordsNoSpaces;

        // How many per space
        float emptySpacePerSpace = emtpySpaceInLine/numberOfSpaces;

        // Display data
        float cumulativeLength = 0;
        for (int j=0;j<numberOfWords;j++) {
            canvas.drawText(words[j],xLeft + cumulativeLength,yTop + i * textSize * textSpacing,paint);
            cumulativeLength += paint.measureText(words[j]) + emptySpacePerSpace;
        }

    }

    private String[] createArrayOfWords(String string) {
        String[] words = string.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].replaceAll(" ", "");
        }
        return words;
    }

    private float measureLenOfStringsInStringArray(String[] stringArray, Paint paint) {
        float num = 0;
        for (int i = 0; i < stringArray.length; i++) {
            num += paint.measureText(stringArray[i]);
        }
        return num;
    }


    /**PRINT**/
    // Print the bitmap. Works great for printing with Photosmart, but not on saving to PDF or drive.
    public void printBitmap(MenuItem mi) {
        if (!PrintHelper.systemSupportsPrint()) {
            //Toast.makeText(this, "Printing from this phone not supporeted.", Toast.LENGTH_SHORT).show();
            customToast(getResources().getString(R.string.invalidQRcode));
            return;
        }
        PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        photoPrinter.setOrientation(PrintHelper.ORIENTATION_PORTRAIT);
        photoPrinter.setColorMode(PrintHelper.COLOR_MODE_COLOR);
        photoPrinter.printBitmap("PaperVault", bitmap);
    }

    /**SAVE PNG. Preferably to SD card**/
    public void saveBitmap(MenuItem mi) {
        customToast(getResources().getString(R.string.justAMoment));

        String folder = "/PaperVault/temp";
        if (mi.getItemId() == R.id.save) {
            folder = "/PaperVault";
        }

        // compress
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);

        // store
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File (sdCard.getAbsolutePath() + "/PaperVault");
        dir.mkdirs();

        //This file is local
        File file = new File(dir, "PapVa_"+st_title.replace(" ", "") +"_"+System.currentTimeMillis()+".png");

        try {
            file.createNewFile();
            FileOutputStream fo = new FileOutputStream(file);
            fo.write(bytes.toByteArray());
            fo.flush();
            fo.close();

            // Putting to gallery doesnt work
            // MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(), file.getName(), file.getName());

            customToast(getResources().getString(R.string.imageStoredTo)+"\n"+file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**SHARE to other apps. Due to the large size of the Bitmap, the file must be saved first. Not okay for security...**/
    public void shareBitmap(MenuItem mi) {
        // Toasts will have to be put in a new Tread, since they appear too late.
        customToast(getResources().getString(R.string.justAMoment));

        // I intend to send a PNG image
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/png");

        // Compress the bitmap to PNG
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);

        // Temporarily store the image to Flash
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File (sdCard.getAbsolutePath() + "/PaperVault/temp");
        dir.mkdirs();

        // This file is static - so I can delete it in the next method
        file = new File(dir, "PapVa_"+System.currentTimeMillis()+".png");

        try {
            file.createNewFile();
            FileOutputStream fo = new FileOutputStream(file);
            fo.write(bytes.toByteArray());
            fo.flush();
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("Florjan: dir and file getname", dir.getAbsolutePath() + "/" + file.getName());
        Log.d("Florjan: Current", file.getPath());
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///"+file.getPath()));

        //startActivity(Intent.createChooser(share, "Share Image"));
        startActivityForResult(Intent.createChooser(share,"Share Image"),1);

        /**Delete Temporary file:**/
        // I need a better solution for this. I tried not saving it to flash in the first place, but that made the app crash.
        // file.delete();        // deletes it too fast
        file.deleteOnExit();     // sometimes works???

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* // This does not work very good. It deleted the file before it is attached to some devices. So it it disabled for now.
        customToast("Deleting temporary file");

        try {
            // This gives me an error Hangouts. Maybe file was deleted twice.
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            Log.d("Deleting file","hopefully will not crash now");
        }
        */

    }

    public void customToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
}