package app.pxlwallpapers;

interface GlRenderer {
  void initialize();
  void render(int width, int height, float elapsedSeconds);
  void release();
}
