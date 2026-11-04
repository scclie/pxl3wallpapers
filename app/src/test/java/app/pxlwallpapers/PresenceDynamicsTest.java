package app.pxlwallpapers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class PresenceDynamicsTest {
  @Test
  public void unlockUsesOriginalTwoSecondExpoOutTransition() {
    PresenceDynamics presence = new PresenceDynamics();
    presence.setUserPresence(PresenceDynamics.LOCKED, false, false);
    presence.setUserPresence(PresenceDynamics.UNLOCKED, true, false);

    presence.update(1f);

    assertEquals(0f, presence.aodValue(), 0f);
    assertEquals(.96875f, presence.unlockedValue(), .00001f);
  }

  @Test
  public void screenOffDelaysOriginalTransitionByPointTwoSeconds() {
    PresenceDynamics presence = new PresenceDynamics();
    presence.setUserPresence(PresenceDynamics.UNLOCKED, false, false);
    presence.setUserPresence(PresenceDynamics.OFF, true, false);

    presence.update(.1f);
    assertEquals(0f, presence.aodValue(), 0f);
    presence.update(.2f);
    assertEquals(1f - (float) Math.pow(2d, -.5d), presence.aodValue(), .00001f);
  }
}
