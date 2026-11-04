package app.pxlwallpapers;

import android.service.wallpaper.WallpaperService;

abstract class GrooveWallpaperService extends WallpaperService {
  private final String configId;

  GrooveWallpaperService(String configId) {
    this.configId = configId;
  }

  @Override
  public Engine onCreateEngine() {
    GrooveConfig config = GrooveConfigs.forId(configId);
    return new GrooveEngine(this, () -> new GrooveRenderer(getAssets(), config));
  }
}
