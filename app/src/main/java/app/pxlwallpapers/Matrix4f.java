package app.pxlwallpapers;

public final class Matrix4f {
  private Matrix4f() {}

  public static float[] identity() {
    return new float[] {
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
    };
  }

  public static float[] translate(float[] matrix, float x, float y, float z) {
    float[] translation = identity();
    translation[12] = x;
    translation[13] = y;
    translation[14] = z;
    return multiply(matrix, translation);
  }

  public static float[] rotateZ(float[] matrix, float radians) {
    float cosine = (float) Math.cos(radians);
    float sine = (float) Math.sin(radians);
    float[] rotation = identity();
    rotation[0] = cosine;
    rotation[1] = sine;
    rotation[4] = -sine;
    rotation[5] = cosine;
    return multiply(matrix, rotation);
  }

  public static float[] multiply(float[] left, float[] right) {
    float[] result = new float[16];
    for (int column = 0; column < 4; column++) {
      for (int row = 0; row < 4; row++) {
        for (int term = 0; term < 4; term++) {
          result[column * 4 + row] += left[term * 4 + row] * right[column * 4 + term];
        }
      }
    }
    return result;
  }

  public static float[] transform(float[] matrix, float[] vector) {
    float[] result = new float[4];
    for (int row = 0; row < 4; row++) {
      for (int column = 0; column < 4; column++) {
        result[row] += matrix[column * 4 + row] * vector[column];
      }
    }
    return result;
  }
}
