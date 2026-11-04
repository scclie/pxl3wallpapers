package app.pxlwallpapers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class Matrix4fTest {
  @Test
  public void translateUsesColumnMajorOpenGlOffsets() {
    float[] matrix = Matrix4f.translate(Matrix4f.identity(), 2f, 3f, 0f);

    assertEquals(2f, matrix[12], 0f);
    assertEquals(3f, matrix[13], 0f);
  }

  @Test
  public void transformUsesRotationThenNonUniformScale() {
    float[] matrix = Matrix4f.transform(0f, 0f, 0f, (float) (Math.PI / 2), 3.5f, 4.9f, 1f);

    float[] xAxis = Matrix4f.transform(matrix, new float[] {1f, 0f, 0f, 1f});
    float[] yAxis = Matrix4f.transform(matrix, new float[] {0f, 1f, 0f, 1f});
    assertEquals(0f, xAxis[0], 0.001f);
    assertEquals(3.5f, xAxis[1], 0.001f);
    assertEquals(-4.9f, yAxis[0], 0.001f);
    assertEquals(0f, yAxis[1], 0.001f);
  }
}

