package app.pxlwallpapers;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.view.Surface;

public final class EglSession {
  private static final int EGL_OPENGL_ES3_BIT_KHR = 0x40;
  private EGLDisplay display = EGL14.EGL_NO_DISPLAY;
  private EGLContext context = EGL14.EGL_NO_CONTEXT;
  private EGLSurface surface = EGL14.EGL_NO_SURFACE;

  public void create(Surface windowSurface) {
    display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
    require(display != EGL14.EGL_NO_DISPLAY, "eglGetDisplay");

    int[] version = new int[2];
    require(EGL14.eglInitialize(display, version, 0, version, 1), "eglInitialize");
    require(EGL14.eglBindAPI(EGL14.EGL_OPENGL_ES_API), "eglBindAPI");

    int[] attributes = {
        EGL14.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT_KHR,
        EGL14.EGL_RED_SIZE, 8,
        EGL14.EGL_GREEN_SIZE, 8,
        EGL14.EGL_BLUE_SIZE, 8,
        EGL14.EGL_ALPHA_SIZE, 8,
        EGL14.EGL_NONE
    };
    EGLConfig[] configs = new EGLConfig[1];
    int[] count = new int[1];
    require(EGL14.eglChooseConfig(display, attributes, 0, configs, 0, 1, count, 0) && count[0] == 1,
        "eglChooseConfig");

    int[] contextAttributes = {EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL14.EGL_NONE};
    context = EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, contextAttributes, 0);
    require(context != EGL14.EGL_NO_CONTEXT, "eglCreateContext");
    surface = EGL14.eglCreateWindowSurface(display, configs[0], windowSurface, new int[] {EGL14.EGL_NONE}, 0);
    require(surface != EGL14.EGL_NO_SURFACE, "eglCreateWindowSurface");
    makeCurrent();
  }

  public void makeCurrent() {
    require(EGL14.eglMakeCurrent(display, surface, surface, context), "eglMakeCurrent");
  }

  public void swapBuffers() {
    require(EGL14.eglSwapBuffers(display, surface), "eglSwapBuffers");
  }

  public void release() {
    if (display == EGL14.EGL_NO_DISPLAY) return;
    EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
    if (surface != EGL14.EGL_NO_SURFACE) EGL14.eglDestroySurface(display, surface);
    if (context != EGL14.EGL_NO_CONTEXT) EGL14.eglDestroyContext(display, context);
    EGL14.eglTerminate(display);
    display = EGL14.EGL_NO_DISPLAY;
    context = EGL14.EGL_NO_CONTEXT;
    surface = EGL14.EGL_NO_SURFACE;
  }

  private static void require(boolean value, String operation) {
    if (!value) throw new IllegalStateException(operation + " failed: 0x" + Integer.toHexString(EGL14.eglGetError()));
  }
}
