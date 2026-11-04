package app.pxlwallpapers;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public final class GrooveConfigsTest {
  @Test
  public void chillMatchesTheRecoveredBlueConfiguration() {
    GrooveConfig config = GrooveConfigs.forId("chill");

    assertEquals("Groove, Chill", config.title());
    assertEquals(106f, config.initialTime(), 0f);
    assertEquals(40f, config.initialSeed(), 0f);
    assertArrayEquals(new float[] {1f, 1f, 1f}, config.clearColor(), 0f);
  }

  @Test
  public void partyMatchesTheRecoveredPinkConfiguration() {
    GrooveConfig config = GrooveConfigs.forId("party");

    assertEquals("Groove, Party", config.title());
    assertEquals(562f, config.initialTime(), 0f);
    assertEquals(24f, config.initialSeed(), 0f);
    assertArrayEquals(new float[] {1f, 110f / 255f, 113f / 255f}, config.clearColor(), 0f);
  }

  @Test
  public void afterDarkMatchesTheRecoveredNeonConfiguration() {
    GrooveConfig config = GrooveConfigs.forId("after_dark");

    assertEquals("Groove, After Dark", config.title());
    assertEquals(59f, config.initialTime(), 0f);
    assertEquals(13f, config.initialSeed(), 0f);
    assertArrayEquals(new float[] {103f / 255f, 52f / 255f, 239f / 255f}, config.clearColor(), 0f);
  }

  @Test
  public void rejectsUnverifiedAudioConfiguration() {
    assertThrows(IllegalArgumentException.class, () -> GrooveConfigs.forId("audio"));
  }
}
