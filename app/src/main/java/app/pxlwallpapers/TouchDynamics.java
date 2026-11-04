package app.pxlwallpapers;

final class TouchDynamics {
  private boolean touchDown;
  private boolean currentTouchDown;
  private boolean wasScrollAnimatingDuringTouch;
  private boolean touchInteracting;
  private long initTimeTouch;
  private float positionX;
  private float positionY;
  private float deltaStrength;
  private float deltaStrengthEased;

  void touchDown(float screenX, float screenY, int pointer, long elapsedRealtimeMillis, boolean powerSave) {
    if (!powerSave && pointer == 0) {
      touchDown = true;
      initTimeTouch = elapsedRealtimeMillis;
      positionX = screenX;
      positionY = screenY;
      touchInteracting = false;
    }
  }

  void touchDragged(float screenX, float screenY, int pointer, float density) {
    if (pointer == 0 && touchDown) {
      float deltaX = screenX - positionX;
      float deltaY = screenY - positionY;
      positionX = screenX;
      positionY = screenY;
      if ((float) Math.sqrt(deltaX * deltaX + deltaY * deltaY) > 3f * density && !touchInteracting) {
        touchDown = false;
      }
    }
  }

  void touchUp(int pointer, boolean powerSave) {
    if (!powerSave && pointer == 0) {
      touchDown = false;
      touchInteracting = false;
    }
  }

  void update(long elapsedRealtimeMillis, float delta, boolean scrollAnimating, float previewTweenValue) {
    if (currentTouchDown != touchDown && touchDown) {
      wasScrollAnimatingDuringTouch = scrollAnimating;
    }
    currentTouchDown = touchDown;
    if (!touchDown) {
      deltaStrength = 0f;
    } else if (elapsedRealtimeMillis - initTimeTouch > 70L) {
      deltaStrength = 1f;
      touchInteracting = true;
    } else {
      deltaStrength = 0f;
      touchInteracting = false;
    }
    float easing = deltaStrength == 0f ? 4f : 2f;
    if (touchDown && !wasScrollAnimatingDuringTouch && scrollAnimating) {
      deltaStrength = 0f;
      easing = 10f;
    }
    deltaStrength = mix(deltaStrength, 1f, previewTweenValue);
    deltaStrengthEased += (deltaStrength - deltaStrengthEased) * mix(easing, 2f, previewTweenValue) * delta;
  }

  float strength() {
    return deltaStrengthEased;
  }

  private static float mix(float fromValue, float toValue, float progress) {
    if (progress < 0f) return fromValue;
    if (progress > 1f) return toValue;
    return (toValue - fromValue) * progress + fromValue;
  }
}
