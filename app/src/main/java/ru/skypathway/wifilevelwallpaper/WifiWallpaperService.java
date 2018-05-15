package ru.skypathway.wifilevelwallpaper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class WifiWallpaperService extends WallpaperService {

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
        int mDrawSpeed = 20;

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
            //int colorBackground = ResourcesCompat.getColor(getResources(),
            //        R.color.colorBackgroundMinWifi, null);
                canvas.drawColor(Color.RED);
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mHandler.removeCallbacks(mDrawRunner);
        }
    }
}
