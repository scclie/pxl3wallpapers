package app.pxlwallpapers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;
import org.junit.Test;

public final class AssetShaderSourceTest {
  @Test
  public void expandsAssetIncludesAndGeneratedMethods() throws IOException {
    Map<String, String> assets = Map.of(
        "soundviz/disk.frag", "#version 310 es\n#include SHADER_METHODS\n#include <glsl/noise.glsl>\nvoid main() {}",
        "glsl/noise.glsl", "float noise() { return 0.0; }");

    String source = new AssetShaderSource(assets::get).load(
        "soundviz/disk.frag", "#define SHOW_AUDIO_BLOCK  0\n#define SHOW_LINES  0\n");

    assertTrue(source.startsWith("#version 310 es"));
    assertTrue(source.contains("#define SHOW_AUDIO_BLOCK  0"));
    assertTrue(source.contains("float noise()"));
    assertFalse(source.contains("#include"));
  }
}
