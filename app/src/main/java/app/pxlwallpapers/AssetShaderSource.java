package app.pxlwallpapers;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AssetShaderSource {
  @FunctionalInterface
  public interface AssetReader {
    String read(String path) throws IOException;
  }

  private static final Pattern ASSET_INCLUDE = Pattern.compile(
      "(?m)^\\s*#include\\s+<([^>]+)>\\s*$");
  private final AssetReader reader;

  public AssetShaderSource(AssetReader reader) {
    this.reader = reader;
  }

  public String load(String assetPath, String generatedMethods) throws IOException {
    return expand(assetPath, generatedMethods, new LinkedHashSet<>());
  }

  private String expand(String path, String generatedMethods, LinkedHashSet<String> stack)
      throws IOException {
    if (!stack.add(path)) {
      throw new IOException("Cyclic GLSL include: " + stack);
    }
    String source = reader.read(path);
    if (source == null) {
      throw new IOException("Missing GLSL asset: " + path);
    }
    source = source.replace("#include SHADER_METHODS", generatedMethods);
    Matcher matcher = ASSET_INCLUDE.matcher(source);
    StringBuffer expanded = new StringBuffer();
    while (matcher.find()) {
      String replacement = expand(matcher.group(1), generatedMethods, stack);
      matcher.appendReplacement(expanded, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(expanded);
    stack.remove(path);
    return expanded.toString();
  }
}
