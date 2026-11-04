package app.pxlwallpapers;

import android.content.res.AssetManager;
import android.opengl.GLES31;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

final class GrooveRenderer implements GlRenderer {
  private static final String FIXED_METHODS = "#define SHOW_AUDIO_BLOCK  0\n#define SHOW_LINES  0\n";
  private static final float[] FULLSCREEN = {
      -1f, -1f, 0f, 0f, 0f,
       1f, -1f, 0f, 1f, 0f,
      -1f,  1f, 0f, 0f, 1f,
       1f,  1f, 0f, 1f, 1f
  };

  private final GrooveConfig config;
  private final AssetShaderSource shaderSource;
  private final GrooveDynamics dynamics;
  private GlProgram backgroundProgram;
  private GlProgram diskProgram;
  private GlProgram highlightsProgram;
  private int fullscreenBuffer;
  private int planeVertexBuffer;
  private int planeIndexBuffer;
  private int planeIndexCount;
  private int highlightTexture;
  private int highlightFramebuffer;
  private int highlightWidth;
  private int highlightHeight;
  private float touchStrength;
  private float previousSeconds;

  GrooveRenderer(AssetManager assets, GrooveConfig config) {
    this.config = config;
    dynamics = new GrooveDynamics(config);
    shaderSource = new AssetShaderSource(new AndroidAssetReader(assets));
  }

  @Override public void initialize() {
    try {
      backgroundProgram = GlProgram.create(shaderSource.load("soundviz/bgd.vert", FIXED_METHODS), shaderSource.load("soundviz/bgd.frag", FIXED_METHODS), "soundviz background");
      diskProgram = GlProgram.create(shaderSource.load("soundviz/disk.vert", FIXED_METHODS), shaderSource.load("soundviz/disk.frag", FIXED_METHODS), "soundviz disk");
      highlightsProgram = GlProgram.create(shaderSource.load("soundviz/bgd.vert", FIXED_METHODS), shaderSource.load("soundviz/highlights.frag", FIXED_METHODS), "soundviz highlights");
      int[] buffers = new int[1];
      GLES31.glGenBuffers(1, buffers, 0);
      fullscreenBuffer = buffers[0];
      GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, fullscreenBuffer);
      FloatBuffer data = ByteBuffer.allocateDirect(FULLSCREEN.length * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
      data.put(FULLSCREEN).position(0);
      GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER, FULLSCREEN.length * Float.BYTES, data, GLES31.GL_STATIC_DRAW);
      GroovePlaneMesh plane = new GroovePlaneMesh();
      GLES31.glGenBuffers(1, buffers, 0);
      planeVertexBuffer = buffers[0];
      GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, planeVertexBuffer);
      GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER, plane.vertices.capacity() * Float.BYTES, plane.vertices, GLES31.GL_STATIC_DRAW);
      GLES31.glGenBuffers(1, buffers, 0);
      planeIndexBuffer = buffers[0];
      planeIndexCount = plane.indices.capacity();
      GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, planeIndexBuffer);
      GLES31.glBufferData(GLES31.GL_ELEMENT_ARRAY_BUFFER, plane.indices.capacity() * Short.BYTES, plane.indices, GLES31.GL_STATIC_DRAW);
      GLES31.glGenTextures(1, buffers, 0);
      highlightTexture = buffers[0];
      GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, highlightTexture);
      GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_LINEAR);
      GLES31.glTexParameteri(GLES31.GL_TEXTURE_2D, GLES31.GL_TEXTURE_MAG_FILTER, GLES31.GL_LINEAR);
      ByteBuffer white = ByteBuffer.allocateDirect(4).put(new byte[] {(byte) 255, (byte) 255, (byte) 255, (byte) 255});
      white.position(0);
      GLES31.glTexImage2D(GLES31.GL_TEXTURE_2D, 0, GLES31.GL_RGBA, 1, 1, 0, GLES31.GL_RGBA, GLES31.GL_UNSIGNED_BYTE, white);
    } catch (IOException error) { throw new IllegalStateException("Unable to load SoundViz shaders", error); }
  }

  @Override public void render(int width, int height, float elapsedSeconds) {
    float delta = Math.max(0f, elapsedSeconds - previousSeconds);
    previousSeconds = elapsedSeconds;
    dynamics.update(delta, width / (float) height);
    float[] color = config.clearColor();
    GLES31.glViewport(0, 0, width, height);
    GLES31.glClearColor(color[0], color[1], color[2], 1f);
    GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT);
    backgroundProgram.use();
    GLES31.glUniform4f(backgroundProgram.uniform("u_color"), color[0], color[1], color[2], 1f);
    GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, fullscreenBuffer);
    int position = backgroundProgram.attribute("a_position");
    int texCoord = backgroundProgram.attribute("a_texCoord0");
    GLES31.glEnableVertexAttribArray(position);
    GLES31.glVertexAttribPointer(position, 3, GLES31.GL_FLOAT, false, 20, 0);
    GLES31.glEnableVertexAttribArray(texCoord);
    GLES31.glVertexAttribPointer(texCoord, 2, GLES31.GL_FLOAT, false, 20, 12);
    GLES31.glDrawArrays(GLES31.GL_TRIANGLE_STRIP, 0, 4);
    GLES31.glDisableVertexAttribArray(position);
    GLES31.glDisableVertexAttribArray(texCoord);

    renderHighlights(width, height);
    GLES31.glViewport(0, 0, width, height);
    diskProgram.use();
    GLES31.glActiveTexture(GLES31.GL_TEXTURE0);
    GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, highlightTexture);
    GLES31.glUniform1i(diskProgram.uniform("u_highlights"), 0);
    float aspect = width / (float) height;
    float planeHeight = 4.9f * 4f / (float) Math.sqrt(aspect * aspect + 1f);
    float planeWidth = planeHeight * aspect;
    float frustumScale = width < height ? 0.937f : 0.7f;
    float[] projection = Matrix4f.ortho(
        -planeWidth * frustumScale / 2f, planeWidth * frustumScale / 2f,
        -planeHeight * frustumScale / 2f, planeHeight * frustumScale / 2f,
        -1f, 1f);
    GLES31.glUniformMatrix4fv(diskProgram.uniform("u_projTrans"), 1, false, projection, 0);
    GLES31.glUniformMatrix4fv(diskProgram.uniform("u_transform"), 1, false,
        Matrix4f.transform(0f, 0f, 0f, dynamics.rotationRadians(), 3.5f, 4.9f, 1f), 0);
    GLES31.glUniform1f(diskProgram.uniform("u_time"), dynamics.time());
    GLES31.glUniform1f(diskProgram.uniform("u_seed"), dynamics.seed());
    GLES31.glUniform2f(diskProgram.uniform("u_resolution"), width, height);
    GLES31.glUniform1f(diskProgram.uniform("u_aod"), dynamics.aodValue());
    GLES31.glUniform1f(diskProgram.uniform("u_locked"), dynamics.lockedValue());
    GLES31.glUniform1f(diskProgram.uniform("u_aod_vignette"), dynamics.aodVignette());
    setColor("u_color_a_outer", config.colorsA(), 0);
    setColor("u_color_a_cp4", config.colorsA(), 3);
    setColor("u_color_a_cp3", config.colorsA(), 6);
    setColor("u_color_a_cp2", config.colorsA(), 9);
    setColor("u_color_a_cp1", config.colorsA(), 12);
    setColor("u_color_a_inner", config.colorsA(), 15);
    setColor("u_color_b_outer", config.colorsB(), 0);
    setColor("u_color_b_cp4", config.colorsB(), 3);
    setColor("u_color_b_cp3", config.colorsB(), 6);
    setColor("u_color_b_cp2", config.colorsB(), 9);
    setColor("u_color_b_cp1", config.colorsB(), 12);
    setColor("u_color_b_inner", config.colorsB(), 15);
    float[] vignette = config.vignetteColor();
    float[] highlight = config.highlightColor();
    GLES31.glUniform4f(diskProgram.uniform("u_vignette_color"), vignette[0], vignette[1], vignette[2], 1f);
    GLES31.glUniform4f(diskProgram.uniform("u_highlight_color"), highlight[0], highlight[1], highlight[2], 1f);
    GLES31.glUniform2f(diskProgram.uniform("u_vignette_scale"), config.vignetteScaleX(), config.vignetteScaleY());
    GLES31.glUniform2f(diskProgram.uniform("u_vignette_center"), config.vignetteCenterX(), config.vignetteCenterY());
    GLES31.glUniform1f(diskProgram.uniform("u_center"), dynamics.oscCenter());
    GLES31.glUniform1f(diskProgram.uniform("u_amp"), dynamics.oscAmplitude());
    GLES31.glUniform1f(diskProgram.uniform("aod_amp"), dynamics.aodAmplitude());
    GLES31.glUniform1f(diskProgram.uniform("u_phase"), dynamics.phase());
    GLES31.glUniform1f(diskProgram.uniform("u_pull_noise_amp"), dynamics.pullNoiseAmplitude());
    GLES31.glUniform1f(diskProgram.uniform("u_outer_noise_amp"), dynamics.outerNoiseAmplitude());
    GLES31.glUniform1f(diskProgram.uniform("u_inner_noise_amp"), dynamics.innerNoiseAmplitude());
    GLES31.glUniform1f(diskProgram.uniform("u_inner_noise_variation"), dynamics.innerNoiseVariation());
    GLES31.glUniform1f(diskProgram.uniform("u_freq"), 1.45f);
    GLES31.glUniform1f(diskProgram.uniform("u_pull_noise_speed"), 0.15f);
    GLES31.glUniform1f(diskProgram.uniform("u_pull_noise_freq"), 1f);
    GLES31.glUniform1f(diskProgram.uniform("u_outer_noise_speed"), 0.057f);
    GLES31.glUniform1f(diskProgram.uniform("u_outer_noise_freq"), 2.743f);
    GLES31.glUniform1f(diskProgram.uniform("u_inner_noise_speed"), 0.08f);
    GLES31.glUniform1f(diskProgram.uniform("u_inner_noise_freq"), 2.8f);
    GLES31.glUniform1f(diskProgram.uniform("u_inner_noise_freq_2"), 0.15f);
    GLES31.glUniform1f(diskProgram.uniform("u_highlight_intensity"), config.highlightIntensity());
    GLES31.glUniform1f(diskProgram.uniform("u_color_var_speed"), 0.08f);
    GLES31.glUniform1f(diskProgram.uniform("u_color_var_freq"), 0.25f);
    GLES31.glUniform1f(diskProgram.uniform("u_opacity_max"), 1f);
    GLES31.glUniform1f(diskProgram.uniform("u_opacity_min"), 0.3f);
    GLES31.glUniform1f(diskProgram.uniform("u_opacity_thr_min"), 0f);
    GLES31.glUniform1f(diskProgram.uniform("u_opacity_thr_max"), 0.5f);
    GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, planeVertexBuffer);
    int diskPosition = diskProgram.attribute("a_position");
    int diskTexCoord = diskProgram.attribute("a_texCoord0");
    GLES31.glEnableVertexAttribArray(diskPosition);
    GLES31.glVertexAttribPointer(diskPosition, 3, GLES31.GL_FLOAT, false, 20, 0);
    GLES31.glEnableVertexAttribArray(diskTexCoord);
    GLES31.glVertexAttribPointer(diskTexCoord, 2, GLES31.GL_FLOAT, false, 20, 12);
    GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, planeIndexBuffer);
    GLES31.glDrawElements(GLES31.GL_TRIANGLES, planeIndexCount, GLES31.GL_UNSIGNED_SHORT, 0);
    GLES31.glDisableVertexAttribArray(diskPosition);
    GLES31.glDisableVertexAttribArray(diskTexCoord);
  }

  private void renderHighlights(int width, int height) {
    HighlightNoiseLayout layout = HighlightNoiseLayout.forAspect(
        width / (float) height,
        config.highlightNoiseScaleX(), config.highlightNoiseScaleY(),
        config.highlightNoiseScaleLandscapeX(), config.highlightNoiseScaleLandscapeY());
    if (highlightWidth != layout.textureWidth() || highlightHeight != layout.textureHeight()) {
      highlightWidth = layout.textureWidth();
      highlightHeight = layout.textureHeight();
      GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, highlightTexture);
      GLES31.glTexImage2D(GLES31.GL_TEXTURE_2D, 0, GLES31.GL_RGBA, highlightWidth, highlightHeight, 0,
          GLES31.GL_RGBA, GLES31.GL_UNSIGNED_BYTE, null);
      if (highlightFramebuffer == 0) {
        int[] framebuffers = new int[1];
        GLES31.glGenFramebuffers(1, framebuffers, 0);
        highlightFramebuffer = framebuffers[0];
      }
      GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, highlightFramebuffer);
      GLES31.glFramebufferTexture2D(GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT0,
          GLES31.GL_TEXTURE_2D, highlightTexture, 0);
    }
    GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, highlightFramebuffer);
    GLES31.glViewport(0, 0, highlightWidth, highlightHeight);
    GLES31.glClearColor(0f, 0f, 0f, 0f);
    GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT);
    GLES31.glEnable(GLES31.GL_DITHER);
    GLES31.glEnable(GLES31.GL_BLEND);
    GLES31.glBlendFunc(GLES31.GL_SRC_ALPHA, GLES31.GL_ONE_MINUS_SRC_ALPHA);
    highlightsProgram.use();
    float[] identity = Matrix4f.identity();
    GLES31.glUniformMatrix4fv(highlightsProgram.uniform("u_viewTrans"), 1, false, identity, 0);
    GLES31.glUniformMatrix4fv(highlightsProgram.uniform("u_projTrans"), 1, false, identity, 0);
    GLES31.glUniform1f(highlightsProgram.uniform("u_time"), dynamics.time());
    GLES31.glUniform1f(highlightsProgram.uniform("u_highlight_noise_period"), config.highlightNoisePeriod());
    GLES31.glUniform2f(highlightsProgram.uniform("u_highlight_noise_scale"), layout.scaleX(), layout.scaleY());
    GLES31.glUniform3f(highlightsProgram.uniform("u_highlight_noise_speed"), 0.1f, 0.05f, 0.01f);
    GLES31.glUniform2f(highlightsProgram.uniform("u_resolution"), layout.resolutionWidth(), layout.resolutionHeight());
    GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, fullscreenBuffer);
    int position = highlightsProgram.attribute("a_position");
    int texCoord = highlightsProgram.attribute("a_texCoord0");
    GLES31.glEnableVertexAttribArray(position);
    GLES31.glVertexAttribPointer(position, 3, GLES31.GL_FLOAT, false, 20, 0);
    GLES31.glEnableVertexAttribArray(texCoord);
    GLES31.glVertexAttribPointer(texCoord, 2, GLES31.GL_FLOAT, false, 20, 12);
    GLES31.glDrawArrays(GLES31.GL_TRIANGLE_STRIP, 0, 4);
    GLES31.glDisableVertexAttribArray(position);
    GLES31.glDisableVertexAttribArray(texCoord);
    GLES31.glDisable(GLES31.GL_BLEND);
    GLES31.glDisable(GLES31.GL_DITHER);
    GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, 0);
  }

  private void setColor(String uniform, float[] colors, int offset) {
    GLES31.glUniform4f(
        diskProgram.uniform(uniform),
        colors[offset], colors[offset + 1], colors[offset + 2], 1f);
  }

  @Override public void setPageOffset(float value) {
    dynamics.setPageOffset(value);
  }

  @Override public void setPresence(float aodValue, float unlockedValue) {
    dynamics.setAodValue(aodValue);
    dynamics.setUnlockedValue(unlockedValue);
  }

  @Override public void setLowSpeed(boolean lowSpeed) {
    dynamics.setLowSpeed(lowSpeed);
  }

  @Override public void resume() {
    dynamics.resume();
  }

  @Override public void setTouchStrength(float value) {
    touchStrength = Math.max(0f, Math.min(1f, value));
    dynamics.setTouchStrength(touchStrength);
  }

  @Override public void release() {
    if (fullscreenBuffer != 0) GLES31.glDeleteBuffers(1, new int[] {fullscreenBuffer}, 0);
    if (planeVertexBuffer != 0) GLES31.glDeleteBuffers(1, new int[] {planeVertexBuffer}, 0);
    if (planeIndexBuffer != 0) GLES31.glDeleteBuffers(1, new int[] {planeIndexBuffer}, 0);
    if (highlightTexture != 0) GLES31.glDeleteTextures(1, new int[] {highlightTexture}, 0);
    if (highlightFramebuffer != 0) GLES31.glDeleteFramebuffers(1, new int[] {highlightFramebuffer}, 0);
    if (backgroundProgram != null) backgroundProgram.release();
    if (diskProgram != null) diskProgram.release();
    if (highlightsProgram != null) highlightsProgram.release();
  }
}
