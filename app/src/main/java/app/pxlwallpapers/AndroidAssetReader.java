package app.pxlwallpapers;

import android.content.res.AssetManager;
import java.io.IOException;
import java.io.InputStream;

final class AndroidAssetReader implements AssetShaderSource.AssetReader {
  private final AssetManager assets;

  AndroidAssetReader(AssetManager assets) {
    this.assets = assets;
  }

  @Override
  public String read(String path) throws IOException {
    try (InputStream input = assets.open(path)) {
      return AssetText.read(input);
    }
  }
}
