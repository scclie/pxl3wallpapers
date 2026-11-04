package app.pxlwallpapers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

final class GroovePlaneMesh {
  static final int POINTS = 100;
  static final float WIDTH = 7f;
  static final float HEIGHT = 4f;

  final FloatBuffer vertices;
  final ShortBuffer indices;

  GroovePlaneMesh() {
    float[] vertexData = new float[POINTS * 2 * 5];
    short[] indexData = new short[(POINTS - 1) * 2 * 3];
    for (int point = 0; point < POINTS; point++) {
      float position = point / (float) (POINTS - 1);
      for (int row = 0; row < 2; row++) {
        int offset = (point * 2 + row) * 5;
        vertexData[offset] = position * WIDTH - WIDTH / 2f;
        vertexData[offset + 1] = (row == 0 ? 0f : HEIGHT) - HEIGHT / 2f;
        vertexData[offset + 2] = 0f;
        vertexData[offset + 3] = position;
        vertexData[offset + 4] = row == 0 ? 1f : 0f;
      }
      if (point + 1 < POINTS) {
        int vertex = point * 2;
        int next = (point + 1) * 2;
        int offset = point * 6;
        indexData[offset] = (short) vertex;
        indexData[offset + 1] = (short) (next + 1);
        indexData[offset + 2] = (short) (vertex + 1);
        indexData[offset + 3] = (short) (next + 1);
        indexData[offset + 4] = (short) vertex;
        indexData[offset + 5] = (short) next;
      }
    }
    vertices = floats(vertexData);
    indices = shorts(indexData);
  }

  private static FloatBuffer floats(float[] values) {
    FloatBuffer buffer = ByteBuffer.allocateDirect(values.length * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
    buffer.put(values);
    buffer.position(0);
    return buffer;
  }

  private static ShortBuffer shorts(short[] values) {
    ShortBuffer buffer = ByteBuffer.allocateDirect(values.length * Short.BYTES).order(ByteOrder.nativeOrder()).asShortBuffer();
    buffer.put(values);
    buffer.position(0);
    return buffer;
  }
}
