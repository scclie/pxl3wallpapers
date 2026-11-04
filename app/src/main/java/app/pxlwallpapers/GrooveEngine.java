package app.pxlwallpapers;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.os.SystemClock;
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
  private final WallpaperService service;
  private final HandlerThread renderThread = new HandlerThread("GrooveRender");
  private final Handler handler;
  private final PageSwipeDynamics pageSwipe = new PageSwipeDynamics();
  private final TouchDynamics touch = new TouchDynamics();
  private final PresenceDynamics presence = new PresenceDynamics();
  private final BroadcastReceiver presenceReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      handler.post(() -> {
        if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(intent.getAction())) updatePowerSaveMode();
        else updatePresenceFromBroadcast(intent.getAction());
      });
    }
  };
  private final Runnable frame = this::drawFrame;
  private final float density;
  private EglSession egl;
  private GlRenderer renderer;
  private boolean visible;
  private int width;
  private int height;
  private long startedAtNanos;
  private long previousFrameNanos;
  private String rawUserPresence;
  private boolean powerSave;
  private boolean presenceReceiverRegistered;

  GrooveEngine(WallpaperService service, Supplier<GlRenderer> rendererFactory) {
    service.super();
    this.service = service;
    this.rendererFactory = rendererFactory;
    density = service.getResources().getDisplayMetrics().density;
    PowerManager powerManager = service.getSystemService(PowerManager.class);
    powerSave = powerManager != null && powerManager.isPowerSaveMode();
    rawUserPresence = currentPresence();
    presence.setUserPresence(rawUserPresence, false, false);
    renderThread.start();
    handler = new Handler(renderThread.getLooper());
    registerPresenceReceiver();
    pageSwipe.setMinPagesToSwipe(3);
    pageSwipe.setPageSwipeDamping(3f);
    setTouchEventsEnabled(true);
  }

  @Override
  public void onVisibilityChanged(boolean visible) {
    this.visible = visible;
    if (visible) {
      handler.post(() -> {
        if (renderer == null) initializeIfReady();
        else {
          renderer.resume();
          handler.removeCallbacks(frame);
          drawFrame();
        }
      });
    } else {
      handler.removeCallbacks(frame);
    }
  }

  @Override
  public void onTouchEvent(MotionEvent event) {
    long now = SystemClock.elapsedRealtime();
    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_POINTER_DOWN: {
        int index = event.getPointerCount() - 1;
        if (index >= 0 && index < 4) {
          float x = event.getX(index);
          float y = event.getY(index);
          handler.post(() -> touch.touchDown(x, y, index, now, powerSave));
        }
        break;
      }
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_POINTER_UP: {
        int index = event.getPointerCount() - 1;
        if (index >= 0 && index < 4) handler.post(() -> touch.touchUp(index, powerSave));
        break;
      }
      case MotionEvent.ACTION_MOVE:
        if (event.getPointerCount() > 0) {
          float x = event.getX(0);
          float y = event.getY(0);
          handler.post(() -> touch.touchDragged(x, y, 0, density));
        }
        break;
      default:
        break;
    }
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
    handler.removeCallbacks(frame);
    handler.post(this::release);
    super.onSurfaceDestroyed(holder);
  }

  @Override
  public void onDestroy() {
    unregisterPresenceReceiver();
    handler.removeCallbacks(frame);
    handler.post(() -> {
      release();
      renderThread.quitSafely();
    });
    super.onDestroy();
  }

  private void initializeIfReady() {
    if (!visible || width <= 0 || height <= 0 || renderer != null) return;
    try {
      egl = new EglSession();
      egl.create(getSurfaceHolder().getSurface());
      renderer = rendererFactory.get();
      renderer.initialize();
      renderer.setLowSpeed(powerSave);
      renderer.resume();
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
      presence.update(delta);
      renderer.setPageOffset(pageSwipe.getPageOffset());
      renderer.setPresence(presence.aodValue(), presence.unlockedValue());
      touch.update(SystemClock.elapsedRealtime(), delta, pageSwipe.isScrollAnimating(), 0f);
      renderer.setTouchStrength(touch.strength());
      renderer.render(width, height, (now - startedAtNanos) / 1_000_000_000f);
      egl.swapBuffers();
      handler.postDelayed(frame, pageSwipe.isScrollAnimating() ? FRAME_DELAY_60_FPS_MS : FRAME_DELAY_30_FPS_MS);
    } catch (RuntimeException error) {
      Log.e(TAG, "Groove renderer frame failed", error);
      release();
    }
  }

  private String currentPresence() {
    KeyguardManager keyguard = service.getSystemService(KeyguardManager.class);
    return keyguard != null && keyguard.isKeyguardLocked() ? PresenceDynamics.LOCKED : PresenceDynamics.UNLOCKED;
  }

  private void registerPresenceReceiver() {
    IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
    filter.addAction(Intent.ACTION_SCREEN_OFF);
    filter.addAction(Intent.ACTION_SCREEN_ON);
    filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
    if (Build.VERSION.SDK_INT >= 33) service.registerReceiver(presenceReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    else service.registerReceiver(presenceReceiver, filter);
    presenceReceiverRegistered = true;
  }

  private void updatePowerSaveMode() {
    PowerManager powerManager = service.getSystemService(PowerManager.class);
    powerSave = powerManager != null && powerManager.isPowerSaveMode();
    if (renderer != null) renderer.setLowSpeed(powerSave);
  }

  private void updatePresenceFromBroadcast(String action) {
    String nextPresence;
    if (Intent.ACTION_SCREEN_OFF.equals(action)) nextPresence = PresenceDynamics.OFF;
    else if (Intent.ACTION_USER_PRESENT.equals(action)) nextPresence = PresenceDynamics.UNLOCKED;
    else if (Intent.ACTION_SCREEN_ON.equals(action)) nextPresence = currentPresence();
    else return;
    rawUserPresence = nextPresence;
    presence.setUserPresence(nextPresence, true, !visible);
  }

  private void unregisterPresenceReceiver() {
    if (!presenceReceiverRegistered) return;
    service.unregisterReceiver(presenceReceiver);
    presenceReceiverRegistered = false;
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
