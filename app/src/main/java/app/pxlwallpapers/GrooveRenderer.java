package app.pxlwallpapers;

import android.content.res.AssetManager;
import android.opengl.GLES31;
import java.io.IOException;

final class GrooveRenderer implements GlRenderer {
  private static final String FIXED_METHODS = "#define SHOW_AUDIO_BLOCK  0\n#define SHOW_LINES  0\n";

  private final GrooveConfig config;
  private final AssetShaderSource shaderSource;
  private GlProgram backgroundProgram;
  private GlProgram diskProgram;

  GrooveRenderer(AssetManager assets, GrooveConfig config) {
    this.config = config;
    this.shaderSource = new AssetShaderSource(new AndroidAssetReader(assets));
  }

  @Override
  public void initialize() {
    try {
      backgroundProgram = GlProgram.create(
          shaderSource.load("soundviz/bgd.vert", FIXED_METHODS),
          shaderSource.load("soundviz/bgd.frag", FIXED_METHODS),
          "soundviz background");
      diskProgram = GlProgram.create(
          shaderSource.load("soundviz/disk.vert", FIXED_METHODS),
          shaderSource.load("soundviz/disk.frag", FIXED_METHODS),
          "soundviz disk");
    } catch (IOException error) {
      throw new IllegalStateException("Unable to load SoundViz shaders", error);
    }
  }

  @Override
  public void render(int width, int height, float elapsedSeconds) {
    float[] color = config.clearColor();
    GLES31.glViewport(0, 0, width, height);
    GLES31.glClearColor(color[0], color[1], color[2], 1f);
    GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT);
  }

  @Override
  public void release() {
    if (backgroundProgram != null) backgroundProgram.release();
    if (diskProgram != null) diskProgram.release();
  }
}
