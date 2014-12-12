package csbslovenia.com.paper_vault;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class DrawView extends View {

    Paint paint = new Paint();

    public DrawView(Context context) {
        super(context);
    }
    @Override
    public void onDraw(Canvas canvas) {
    // This is where the actual painting happens:
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(100,100,20, paint);
        canvas.drawCircle(180,100,20, paint);
        canvas.drawCircle(260,100,20, paint);

        paint.setColor(Color.CYAN);
        canvas.drawCircle(140,150,20, paint);
        canvas.drawCircle(220,150,20, paint);

        paint.setColor(Color.RED);
        canvas.drawCircle(100,200,20, paint);
        canvas.drawCircle(180,200,20, paint);
        canvas.drawCircle(260,200,20, paint);
    }

}