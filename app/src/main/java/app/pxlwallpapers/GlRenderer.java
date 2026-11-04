package app.pxlwallpapers;

interface GlRenderer {
  void initialize();
  void render(int width, int height, float elapsedSeconds);
  void setPageOffset(float value);
  void setPresence(float aodValue, float unlockedValue);
  void setLowSpeed(boolean lowSpeed);
  void resume();
  void setTouchStrength(float value);
  void release();
}
