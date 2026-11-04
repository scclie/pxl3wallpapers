package app.pxlwallpapers;

final class PageSwipeDynamics {
  private int minStepsToSwipe = 3;
  private float pageSwipeDamping = 3f;
  private float lastOffset;
  private float easedOffset;

  float getPageOffset() {
    return easedOffset;
  }

  boolean update(float delta) {
    float ease = (lastOffset - easedOffset) * delta * pageSwipeDamping;
    easedOffset += ease;
    return Math.abs(ease) > .0001f;
  }

  boolean isScrollAnimating() {
    return Math.abs(lastOffset - easedOffset) > .0001f;
  }

  void setPageSwipe(float xOffset, float xOffsetStep) {
    int numSteps = Math.round(1f / xOffsetStep);
    float offsetStretch = Math.min(numSteps / (float) minStepsToSwipe, 1f);
    lastOffset = xOffset * offsetStretch;
  }

  void setMinPagesToSwipe(int minPagesToSwipe) {
    minStepsToSwipe = minPagesToSwipe < 2 ? 1 : minPagesToSwipe - 1;
  }

  void setPageSwipeDamping(float value) {
    pageSwipeDamping = value;
  }
}
