package app.pxlwallpapers;

interface GlRenderer {
  void initialize();
  void render(int width, int height, float elapsedSeconds);
  void setPageOffset(float value);
  void setTouchStrength(float value);
  void release();
}
