package app.pxlwallpapers;

import android.opengl.GLES31;

final class GrooveRenderer implements GlRenderer {
  private final GrooveConfig config;

  GrooveRenderer(GrooveConfig config) {
    this.config = config;
  }

  @Override
  public void initialize() {}

  @Override
  public void render(int width, int height, float elapsedSeconds) {
    float[] color = config.clearColor();
    GLES31.glViewport(0, 0, width, height);
    GLES31.glClearColor(color[0], color[1], color[2], 1f);
    GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT);
  }

  @Override
  public void release() {}
}
