package app.pxlwallpapers;

record HighlightNoiseLayout(
    int textureWidth,
    int textureHeight,
    float resolutionWidth,
    float resolutionHeight,
    float scaleX,
    float scaleY) {
  private static final int SIZE = 160;

  static HighlightNoiseLayout forAspect(
      float aspectRatio,
      float portraitScaleX,
      float portraitScaleY,
      float landscapeScaleX,
      float landscapeScaleY) {
    float width = SIZE;
    float height = SIZE;
    if (aspectRatio > 1f) width *= aspectRatio;
    else height /= aspectRatio;
    float scaleX = (aspectRatio > 1f ? landscapeScaleX : portraitScaleX) * (aspectRatio > 1f ? aspectRatio : 1f);
    float scaleY = (aspectRatio > 1f ? landscapeScaleY : portraitScaleY) / (aspectRatio < 1f ? aspectRatio : 1f);
    return new HighlightNoiseLayout(SIZE, SIZE, width, height, scaleX, scaleY);
  }
}
