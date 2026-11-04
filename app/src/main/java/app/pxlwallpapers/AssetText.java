package app.pxlwallpapers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

final class AssetText {
  private AssetText() {}

  static String read(InputStream input) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    int count;
    while ((count = input.read(buffer)) != -1) {
      output.write(buffer, 0, count);
    }
    return new String(output.toByteArray(), StandardCharsets.UTF_8);
  }
}
