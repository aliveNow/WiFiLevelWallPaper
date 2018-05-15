package ru.skypathway.wifilevelwallpaper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class WifiWallpaperService extends WallpaperService {
    private static final int MAX_RSSI = -55;
    private static final int MIN_RSSI = -100;

    @Override
    public Engine onCreateEngine() {
        return new MyWallpaperEngine();
    }

    public class MyWallpaperEngine extends Engine {
        private final Handler mHandler = new Handler();
        private final Runnable mDrawRunner = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };

        private boolean mVisible = true;
        int mDrawSpeed = 100;

        public MyWallpaperEngine() {
            mHandler.post(mDrawRunner);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            draw();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            if (visible) {
                mHandler.post(mDrawRunner);
            } else {
                mHandler.removeCallbacks(mDrawRunner);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            mVisible = false;
            mHandler.removeCallbacks(mDrawRunner);
            super.onSurfaceDestroyed(holder);
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                drawBackground(canvas);
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
            mHandler.removeCallbacks(mDrawRunner);
            if (mVisible) {
                mHandler.postDelayed(mDrawRunner, mDrawSpeed);
            }
        }

        private void drawBackground(Canvas canvas) {
            if (canvas != null) {
                /*
                Проделывать такое каждые 0,1 сек может быть трудозатратно, не успела проверить
                 */
                WifiManager wifiManager = (WifiManager) getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int rssi = wifiInfo.getRssi();
                float normalizedRssi = calculateNormalizedRssi(rssi);
                int colorBackground = getColor(normalizedRssi);
                canvas.drawColor(colorBackground);
            }
        }

        /*
        Возвращает значение от 0 (нет вай-фая) до 1 (отличный вай-фай)
         */
        private float calculateNormalizedRssi(int rssi) {
            if (rssi <= MIN_RSSI) {
                return 0f;
            } else if (rssi >= MAX_RSSI) {
                return 1f;
            } else {
                float range = MAX_RSSI - MIN_RSSI;
                return Math.abs((float)(MIN_RSSI - rssi)/range);
            }
        }

        /*
        Вычисляет градиент цвета от красного до зелёного.
        В функцию лучше, конечно, добавить параметры minColor и maxColor.
         */
        private int getColor(float power)
        {
            /* от 0 до 120 как раз градиент от красного до зелёного
            Да, лучше было сделать это через константы, с минимальным и максимальным цветом,
            но пока так.
            */
            float hue = power * 120f;
            float saturation = 0.9f;
            float brightness = 0.9f;
            return Color.HSVToColor(new float[]{hue, saturation, brightness});
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mHandler.removeCallbacks(mDrawRunner);
        }
    }
}
