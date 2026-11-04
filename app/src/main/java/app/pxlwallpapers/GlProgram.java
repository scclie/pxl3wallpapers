package app.pxlwallpapers;

import android.opengl.GLES31;

final class GlProgram {
  private final int handle;

  private GlProgram(int handle) {
    this.handle = handle;
  }

  static GlProgram create(String vertexSource, String fragmentSource, String name) {
    int vertex = compile(GLES31.GL_VERTEX_SHADER, vertexSource, name + " vertex");
    int fragment = compile(GLES31.GL_FRAGMENT_SHADER, fragmentSource, name + " fragment");
    int program = GLES31.glCreateProgram();
    GLES31.glAttachShader(program, vertex);
    GLES31.glAttachShader(program, fragment);
    GLES31.glLinkProgram(program);
    int[] status = new int[1];
    GLES31.glGetProgramiv(program, GLES31.GL_LINK_STATUS, status, 0);
    GLES31.glDeleteShader(vertex);
    GLES31.glDeleteShader(fragment);
    if (status[0] == 0) {
      String log = GLES31.glGetProgramInfoLog(program);
      GLES31.glDeleteProgram(program);
      throw new IllegalStateException(name + " link failed: " + log);
    }
    return new GlProgram(program);
  }

  void use() {
    GLES31.glUseProgram(handle);
  }

  int uniform(String name) {
    return GLES31.glGetUniformLocation(handle, name);
  }

  int attribute(String name) {
    return GLES31.glGetAttribLocation(handle, name);
  }

  void release() {
    GLES31.glDeleteProgram(handle);
  }

  private static int compile(int type, String source, String name) {
    int shader = GLES31.glCreateShader(type);
    GLES31.glShaderSource(shader, source);
    GLES31.glCompileShader(shader);
    int[] status = new int[1];
    GLES31.glGetShaderiv(shader, GLES31.GL_COMPILE_STATUS, status, 0);
    if (status[0] == 0) {
      String log = GLES31.glGetShaderInfoLog(shader);
      GLES31.glDeleteShader(shader);
      throw new IllegalStateException(name + " compile failed: " + log);
    }
    return shader;
  }
}
