package app.pxlwallpapers;

import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;
import java.util.function.Supplier;

final class GrooveEngine extends WallpaperService.Engine {
  private static final String TAG = "PxlWallpapers";
  private static final long FRAME_DELAY_MS = 16L;

  private final Supplier<GlRenderer> rendererFactory;
  private final Handler handler = new Handler(Looper.getMainLooper());
  private final Runnable frame = this::drawFrame;
  private EglSession egl;
  private GlRenderer renderer;
  private boolean visible;
  private int width;
  private int height;
  private long startedAtNanos;

  GrooveEngine(WallpaperService service, Supplier<GlRenderer> rendererFactory) {
    service.super();
    this.rendererFactory = rendererFactory;
  }

  @Override
  public void onVisibilityChanged(boolean visible) {
    this.visible = visible;
    if (visible) initializeIfReady();
    else handler.removeCallbacks(frame);
  }

  @Override
  public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    this.width = width;
    this.height = height;
    initializeIfReady();
  }

  @Override
  public void onSurfaceDestroyed(SurfaceHolder holder) {
    release();
    super.onSurfaceDestroyed(holder);
  }

  @Override
  public void onDestroy() {
    release();
    super.onDestroy();
  }

  private void initializeIfReady() {
    if (!visible || width <= 0 || height <= 0 || renderer != null) return;
    try {
      egl = new EglSession();
      egl.create(getSurfaceHolder().getSurface());
      renderer = rendererFactory.get();
      renderer.initialize();
      startedAtNanos = System.nanoTime();
      handler.post(frame);
    } catch (RuntimeException error) {
      Log.e(TAG, "Groove renderer initialization failed", error);
      release();
    }
  }

  private void drawFrame() {
    if (!visible || renderer == null || egl == null) return;
    try {
      egl.makeCurrent();
      renderer.render(width, height, (System.nanoTime() - startedAtNanos) / 1_000_000_000f);
      egl.swapBuffers();
      handler.postDelayed(frame, FRAME_DELAY_MS);
    } catch (RuntimeException error) {
      Log.e(TAG, "Groove renderer frame failed", error);
      release();
    }
  }

  private void release() {
    handler.removeCallbacks(frame);
    if (renderer != null) {
      renderer.release();
      renderer = null;
    }
    if (egl != null) {
      egl.release();
      egl = null;
    }
  }
}
