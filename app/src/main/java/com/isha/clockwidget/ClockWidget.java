package com.isha.clockwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class ClockWidget extends AppWidgetProvider {

    static final String ACTION_SHUFFLE = "com.isha.clockwidget.SHUFFLE";

    // {left, top, rotation, size} from the original HTML design (220 x 192 card)
    static final String[] VALS = {"12","1","2","3","4","5","11","10","9","8","7","6",
            "10","12","1","2","3","4","5","11","10","9","8","7","6"};
    static final int[][] POS = {
        {42,12,-22,25},{80,15,12,24},{106,11,-10,25},{133,15,8,24},{158,18,-12,23},{181,6,6,25},
        {24,30,-34,25},{52,38,-14,24},{90,39,-4,25},{124,41,10,24},{150,36,-12,25},{172,32,16,25},
        {14,58,-30,25},{49,68,-24,25},{84,72,6,25},{110,66,-18,25},{134,70,14,24},{158,72,-8,24},{182,58,4,25},
        {25,90,-40,25},{55,99,10,25},{94,101,-6,25},{126,100,12,25},{151,97,-14,25},{173,88,-16,26}
    };

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) render(ctx, mgr, id, false);
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        super.onReceive(ctx, intent);
        if (ACTION_SHUFFLE.equals(intent.getAction())) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
            int[] ids = mgr.getAppWidgetIds(new ComponentName(ctx, ClockWidget.class));
            for (int id : ids) render(ctx, mgr, id, true);
        }
    }

    static void render(Context ctx, AppWidgetManager mgr, int id, boolean shuffle) {
        RemoteViews rv = new RemoteViews(ctx.getPackageName(), R.layout.widget_clock);
        rv.setImageViewBitmap(R.id.pile, drawCard(shuffle));
        Intent i = new Intent(ctx, ClockWidget.class).setAction(ACTION_SHUFFLE);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        rv.setOnClickPendingIntent(R.id.root, pi);
        mgr.updateAppWidget(id, rv);
    }

    static Bitmap drawCard(boolean shuffle) {
        final float S = 2f;
        int w = (int) (220 * S), h = (int) (192 * S);
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);

        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setColor(0xFF1C1916);
        RectF rect = new RectF(0, 0, w, h);
        c.drawRoundRect(rect, 34 * S, 34 * S, bg);

        Path clip = new Path();
        clip.addRoundRect(rect, 34 * S, 34 * S, Path.Direction.CW);
        c.save();
        c.clipPath(clip);

        ArrayList<String> vals = new ArrayList<>();
        for (String v : VALS) vals.add(v);
        Random rnd = new Random();
        if (shuffle) Collections.shuffle(vals, rnd);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0xFFFFF1D6);
        p.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));

        for (int k = 0; k < POS.length; k++) {
            int[] d = POS[k];
            float size = d[3] * S;
            p.setTextSize(size);
            String t = vals.get(k);
            float tw = p.measureText(t);
            float cx = d[0] * S + tw / 2f;
            float cy = (d[1] + 50) * S + size / 2f;
            float rot = d[2] + (shuffle ? rnd.nextInt(31) - 15 : 0);
            float baseline = cy - (p.ascent() + p.descent()) / 2f;
            c.save();
            c.rotate(rot, cx, cy);
            c.drawText(t, cx - tw / 2f, baseline, p);
            c.restore();
        }
        c.restore();

        Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(3f);
        border.setColor(0xFF4A3B2A);
        c.drawRoundRect(new RectF(1.5f, 1.5f, w - 1.5f, h - 1.5f), 34 * S, 34 * S, border);
        return bmp;
    }
}
