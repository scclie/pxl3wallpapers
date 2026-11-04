package app.pxlwallpapers;

import static org.junit.Assert.assertEquals;

import java.nio.FloatBuffer;
import org.junit.Test;

public final class GroovePlaneMeshTest {
  @Test
  public void matchesOriginalWavesConstructorPlaneGeometryAndUvs() {
    GroovePlaneMesh mesh = new GroovePlaneMesh();
    FloatBuffer vertices = mesh.vertices.duplicate();

    assertEquals(-3.5f, vertices.get(0), 0f);
    assertEquals(-2f, vertices.get(1), 0f);
    assertEquals(0f, vertices.get(3), 0f);
    assertEquals(1f, vertices.get(4), 0f);

    int finalTopVertex = (GroovePlaneMesh.POINTS - 1) * 2 * 5;
    assertEquals(3.5f, vertices.get(finalTopVertex), 0f);
    assertEquals(-2f, vertices.get(finalTopVertex + 1), 0f);
    assertEquals(1f, vertices.get(finalTopVertex + 3), 0f);
    assertEquals(1f, vertices.get(finalTopVertex + 4), 0f);
  }
}
