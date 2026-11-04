package app.pxlwallpapers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class HighlightNoiseLayoutTest {
  @Test
  public void portraitUsesOriginalFixedFboAndAspectCorrectedUniforms() {
    HighlightNoiseLayout layout = HighlightNoiseLayout.forAspect(.5f, 2.064f, 2.064f, 1.264f, 1.264f);

    assertEquals(160, layout.textureWidth());
    assertEquals(160, layout.textureHeight());
    assertEquals(160f, layout.resolutionWidth(), 0f);
    assertEquals(320f, layout.resolutionHeight(), 0f);
    assertEquals(2.064f, layout.scaleX(), 0f);
    assertEquals(4.128f, layout.scaleY(), 0.0001f);
  }

  @Test
  public void landscapeUsesOriginalLandscapeScaleAndAspectCorrection() {
    HighlightNoiseLayout layout = HighlightNoiseLayout.forAspect(2f, 2.064f, 2.064f, 1.264f, 1.264f);

    assertEquals(320f, layout.resolutionWidth(), 0f);
    assertEquals(160f, layout.resolutionHeight(), 0f);
    assertEquals(2.528f, layout.scaleX(), 0.0001f);
    assertEquals(1.264f, layout.scaleY(), 0f);
  }
}
