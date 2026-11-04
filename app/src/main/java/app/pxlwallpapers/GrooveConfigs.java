package app.pxlwallpapers;

public final class GrooveConfigs {
  private static final GrooveConfig CHILL = config(
      "chill", "Groove, Chill", 106f, 40f, "#ffffff",
      new String[] {"#354ECC", "#96a7fe", "#96a7fe", "#c8cde9", "#c8cde9", "#99A2CC"},
      new String[] {"#3C58E6", "#8395F2", "#8395F2", "#BDC4EF", "#BDC4EF", "#99A2CC"},
      "#f9e0c4", "#ffdab2", 0.5f, 0.4f, 0.15f, 2.064f, 2.064f, 1.264f, 1.264f, 0.9f, 1.01f, 1.01f, 0.5f, 0.5f);

  private static final GrooveConfig PARTY = config(
      "party", "Groove, Party", 562f, 24f, "#ff6e71",
      new String[] {"#4539ED", "#F06C6C", "#F06C6C", "#F99491", "#F99491", "#464BE0"},
      new String[] {"#4539ED", "#F06C6C", "#F06C6C", "#F99491", "#F99491", "#464BE0"},
      "#464BE0", "#F99491", 0.55f, 0.43f, 0.8f, 1.15f, 1.15f, 1.264f, 1.264f, 1f, 0.75f, 1.15f, 0.5f, 0.5f);

  private static final GrooveConfig AFTER_DARK = config(
      "after_dark", "Groove, After Dark", 59f, 13f, "#6734EF",
      new String[] {"#141220", "#101437", "#12153b", "#5A71ED", "#5A71ED", "#CE98E1"},
      new String[] {"#141220", "#101437", "#12153b", "#5A71ED", "#5A71ED", "#CE98E1"},
      "#000000", "#CE98E1", 0.425f, 0.43f, 0.812f, 2.064f, 2.064f, 1.264f, 1.264f, 0.7f, 0.75f, 1.5f, 0.5f, 0.35f);

  private GrooveConfigs() {}

  public static GrooveConfig forId(String id) {
    return switch (id) {
      case "chill" -> CHILL;
      case "party" -> PARTY;
      case "after_dark" -> AFTER_DARK;
      default -> throw new IllegalArgumentException("Unknown Groove configuration: " + id);
    };
  }

  private static GrooveConfig config(
      String id, String title, float initialTime, float initialSeed, String clearColor,
      String[] colorsA, String[] colorsB, String vignetteColor, String highlightColor,
      float oscCenter, float oscCenterAod, float highlightNoisePeriod,
      float highlightNoiseScaleX, float highlightNoiseScaleY,
      float highlightNoiseScaleLandscapeX, float highlightNoiseScaleLandscapeY,
      float highlightIntensity, float vignetteScaleX, float vignetteScaleY, float vignetteCenterX, float vignetteCenterY) {
    return new GrooveConfig(
        id, title, initialTime, initialSeed, rgb(clearColor), rgbs(colorsA), rgbs(colorsB),
        rgb(vignetteColor), rgb(highlightColor), oscCenter, oscCenterAod, highlightNoisePeriod,
        highlightNoiseScaleX, highlightNoiseScaleY, highlightNoiseScaleLandscapeX, highlightNoiseScaleLandscapeY,
        highlightIntensity, vignetteScaleX, vignetteScaleY, vignetteCenterX, vignetteCenterY);
  }

  private static float[] rgb(String value) {
    int color = Integer.parseInt(value.substring(1), 16);
    return new float[] {((color >> 16) & 0xff) / 255f, ((color >> 8) & 0xff) / 255f, (color & 0xff) / 255f};
  }

  private static float[] rgbs(String[] values) {
    float[] result = new float[values.length * 3];
    for (int index = 0; index < values.length; index++) {
      System.arraycopy(rgb(values[index]), 0, result, index * 3, 3);
    }
    return result;
  }
}
