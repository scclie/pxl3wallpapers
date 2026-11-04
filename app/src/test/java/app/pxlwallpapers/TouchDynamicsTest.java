package app.pxlwallpapers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class TouchDynamicsTest {
  @Test
  public void startsInteractionOnlyAfterOriginalSeventyMillisecondDelay() {
    TouchDynamics touch = new TouchDynamics();
    touch.touchDown(10f, 20f, 0, 1_000L, false);

    touch.update(1_070L, .1f, false, 0f);
    assertEquals(0f, touch.strength(), 0f);

    touch.update(1_071L, .1f, false, 0f);
    assertEquals(.2f, touch.strength(), .00001f);
  }

  @Test
  public void dragBeyondOriginalDensityThresholdCancelsTouch() {
    TouchDynamics touch = new TouchDynamics();
    touch.touchDown(0f, 0f, 0, 0L, false);
    touch.touchDragged(4f, 0f, 0, 1f);

    touch.update(100L, .1f, false, 0f);

    assertEquals(0f, touch.strength(), 0f);
  }
}
