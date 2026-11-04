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
  public void rotateZMovesUnitXToUnitY() {
    float[] matrix = Matrix4f.rotateZ(Matrix4f.identity(), (float) (Math.PI / 2));
    float[] result = Matrix4f.transform(matrix, new float[] {1f, 0f, 0f, 1f});

    assertEquals(0f, result[0], 0.001f);
    assertEquals(1f, result[1], 0.001f);
  }
}
