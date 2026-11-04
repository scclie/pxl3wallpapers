package app.pxlwallpapers;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public final class AssetTextTest {
  @Test
  public void decodesUtf8FromAnInputStream() throws Exception {
    byte[] bytes = "лустмленстгенЬЬЬЖЖЖпшэрстгпгрсг".getBytes(StandardCharsets.UTF_8);

    assertEquals("лустмленстгенЬЬЬЖЖЖпшэрстгпгрсг", AssetText.read(new ByteArrayInputStream(bytes)));
  }
}
