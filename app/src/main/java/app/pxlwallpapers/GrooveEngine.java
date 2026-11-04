package app.pxlwallpapers;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import java.util.function.Supplier;

final class GrooveEngine extends WallpaperService.Engine {
  private static final String TAG = "PxlWallpapers";
  private static final long FRAME_DELAY_30_FPS_MS = 1000L / 30L;
  private static final long FRAME_DELAY_60_FPS_MS = 1000L / 60L;

  private final Supplier<GlRenderer> rendererFactory;
  private final HandlerThread renderThread = new HandlerThread("GrooveRender");
  private final Handler handler;
  private final PageSwipeDynamics pageSwipe = new PageSwipeDynamics();
  private final Runnable frame = this::drawFrame;
  private EglSession egl;
  private GlRenderer renderer;
  private boolean visible;
  private int width;
  private int height;
  private long startedAtNanos;
  private long previousFrameNanos;
  private long touchDownAtNanos;
  private float touchStrength;

  GrooveEngine(WallpaperService service, Supplier<GlRenderer> rendererFactory) {
    service.super();
    this.rendererFactory = rendererFactory;
    renderThread.start();
    handler = new Handler(renderThread.getLooper());
    pageSwipe.setMinPagesToSwipe(3);
    pageSwipe.setPageSwipeDamping(3f);
    setTouchEventsEnabled(true);
  }

  @Override
  public void onVisibilityChanged(boolean visible) {
    this.visible = visible;
    if (visible) handler.post(this::initializeIfReady);
    else handler.removeCallbacks(frame);
  }

  @Override
  public void onTouchEvent(MotionEvent event) {
    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
      touchDownAtNanos = System.nanoTime();
      Log.i(TAG, "touch down");
    } else if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
      touchDownAtNanos = 0L;
      Log.i(TAG, "touch up");
    }
    super.onTouchEvent(event);
  }

  @Override
  public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
    if (xOffsetStep > 0f) handler.post(() -> pageSwipe.setPageSwipe(xOffset, xOffsetStep));
  }

  @Override
  public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    this.width = width;
    this.height = height;
    handler.post(this::initializeIfReady);
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
      previousFrameNanos = startedAtNanos;
      drawFrame();
    } catch (RuntimeException error) {
      Log.e(TAG, "Groove renderer initialization failed", error);
      release();
    }
  }

  private void drawFrame() {
    if (!visible || renderer == null || egl == null) return;
    try {
      egl.makeCurrent();
      long now = System.nanoTime();
      float delta = (now - previousFrameNanos) / 1_000_000_000f;
      previousFrameNanos = now;
      pageSwipe.update(delta);
      renderer.setPageOffset(pageSwipe.getPageOffset());
      float targetTouchStrength = touchDownAtNanos != 0L && now - touchDownAtNanos > 70_000_000L ? 1f : 0f;
      float easing = targetTouchStrength == 0f ? 4f : 2f;
      touchStrength += (targetTouchStrength - touchStrength) * easing * delta;
      renderer.setTouchStrength(touchStrength);
      renderer.render(width, height, (now - startedAtNanos) / 1_000_000_000f);
      egl.swapBuffers();
      handler.postDelayed(frame, pageSwipe.isScrollAnimating() ? FRAME_DELAY_60_FPS_MS : FRAME_DELAY_30_FPS_MS);
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
