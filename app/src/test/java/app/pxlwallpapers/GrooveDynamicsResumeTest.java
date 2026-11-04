package app.pxlwallpapers;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class GrooveDynamicsResumeTest {
  @Test
  public void resumeRandomizesOriginalTimeAndSeedAfterFourHours() {
    GrooveDynamics dynamics = new GrooveDynamics(GrooveConfigs.forId("chill"));
    dynamics.update(36_000f, .5f);

    dynamics.resume();

    assertTrue(dynamics.time() >= .1f && dynamics.time() <= 7200.1f);
    assertTrue(dynamics.seed() >= .1f && dynamics.seed() <= 513.44006f);
  }
}
