package app.pxlwallpapers;

final class PresenceDynamics {
  static final String AOD = "aod";
  static final String LOCKED = "locked";
  static final String OFF = "off";
  static final String UNLOCKED = "unlocked";

  private final Tween aod = new Tween(0f);
  private final Tween unlocked = new Tween(1f);

  void setUserPresence(String presence, boolean animate, boolean isPaused) {
    float aodValue = 0f;
    float unlockedValue = 0f;
    float delay = 0f;
    boolean animateUpdate = animate;
    switch (presence) {
      case OFF:
        animateUpdate = !isPaused;
        delay = animateUpdate ? .2f : 0f;
      case AOD:
        aodValue = 1f;
        break;
      case LOCKED:
        break;
      case UNLOCKED:
        unlockedValue = 1f;
        break;
      default:
        throw new IllegalArgumentException("Unknown user presence: " + presence);
    }
    if (!animateUpdate) {
      aod.set(aodValue);
      unlocked.set(unlockedValue);
    } else {
      aod.to(aodValue, 2f, delay);
      unlocked.to(unlockedValue, 2f, delay);
    }
  }

  void update(float delta) {
    aod.update(delta);
    unlocked.update(delta);
  }

  float aodValue() {
    return aod.value();
  }

  float unlockedValue() {
    return unlocked.value();
  }

  private static final class Tween {
    private float duration;
    private float delay;
    private float elapsedTime;
    private float initialValue;
    private float endValue;
    private float value;
    private boolean animating;

    Tween(float initialValue) {
      set(initialValue);
    }

    void set(float toValue) {
      animating = false;
      elapsedTime = 0f;
      value = toValue;
      initialValue = toValue;
      endValue = toValue;
      duration = 0f;
      delay = 0f;
    }

    void to(float toValue, float duration, float delay) {
      initialValue = value;
      endValue = toValue;
      this.duration = duration;
      this.delay = delay;
      elapsedTime = -delay;
      animating = true;
    }

    void update(float delta) {
      if (!animating) return;
      elapsedTime += delta;
      float clampedElapsed = clamp(elapsedTime, 0f, duration);
      float transition = expoEaseOut(clampedElapsed, 0f, 1f, duration);
      value = (endValue - initialValue) * transition + initialValue;
      animating = transition < 1f || elapsedTime < duration;
    }

    float value() {
      return value;
    }

    private static float expoEaseOut(float time, float begin, float change, float duration) {
      return time == duration ? begin + change
          : (-(float) Math.pow(2d, -10f * time / duration) + 1f) * change + begin;
    }

    private static float clamp(float value, float minimum, float maximum) {
      return value < minimum ? minimum : Math.min(value, maximum);
    }
  }
}
