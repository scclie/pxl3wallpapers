package app.pxlwallpapers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class GrooveDynamicsPowerSaveTest {
  @Test
  public void powerSaveUsesOriginalPointOneTimeScale() {
    GrooveDynamics dynamics = new GrooveDynamics(GrooveConfigs.forId("chill"));

    dynamics.setLowSpeed(true);
    dynamics.update(1f, .5f);

    assertEquals(106.1f, dynamics.time(), .00001f);
  }
}
