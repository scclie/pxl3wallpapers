package app.pxlwallpapers;

public record GrooveConfig(
    String id,
    String title,
    float initialTime,
    float initialSeed,
    float[] clearColor,
    float[] colorsA,
    float[] colorsB,
    float[] vignetteColor,
    float[] highlightColor,
    float oscCenter,
    float oscCenterAod,
    float highlightNoisePeriod,
    float highlightIntensity,
    float vignetteScaleX,
    float vignetteScaleY,
    float vignetteCenterX,
    float vignetteCenterY
) {}
