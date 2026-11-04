package app.pxlwallpapers;

import android.content.res.AssetManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

final class AndroidAssetReader implements AssetShaderSource.AssetReader {
  private final AssetManager assets;

  AndroidAssetReader(AssetManager assets) {
    this.assets = assets;
  }

  @Override
  public String read(String path) throws IOException {
    try (InputStream input = assets.open(path); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      input.transferTo(output);
      return output.toString(StandardCharsets.UTF_8);
    }
  }
}
