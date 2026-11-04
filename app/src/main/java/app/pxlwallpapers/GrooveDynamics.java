package app.pxlwallpapers;

final class GrooveDynamics {
  private static final float PLANE_WIDTH = 7f;
  private static final float SCALE_TIME_REGULAR = 0.4f;
  private static final float SCALE_TIME_BATTERY_SAVE = 0.1f;
  private static final float OSC_AMPLITUDE = 0.1f;
  private static final float OSC_AMPLITUDE_LANDSCAPE = 0.03f;
  private static final float OSC_CENTER_LANDSCAPE = 0.5f;
  private static final float OSC_PULL_AMP = 0.1f;
  private static final float OUTER_NOISE_AMP = 0.1f;
  private static final float INNER_NOISE_AMP = 0.2f;
  private static final float INNER_NOISE_AMP_VARIATION = 1.5f;
  private static final float PLANE_ROTATION_FREQ = 0.025f;
  private static final float PLANE_ROTATION_DEGREES = 25f;
  private static final float PHASE_SPEED = 0.01f;
  private static final float SWIPE_PHASE = 3.1415927f;

  private final GrooveConfig config;
  private float time;
  private float seed;
  private float accumulatedPhase;
  private float touchStrength;
  private float pageOffset;
  private float loudness;
  private float loudnessTarget;
  private float aodValue;
  private float unlockedValue = 1f;
  private float lockedValue;
  private float noiseAmpVarPresenceBandEased = 1f;
  private float phase;
  private float noiseAmpVariation = INNER_NOISE_AMP_VARIATION;
  private float noiseAmp = INNER_NOISE_AMP;
  private float aodVignette;
  private float oscAmplitude;
  private float oscPullAmp = OSC_PULL_AMP;
  private float oscCenter;
  private float outerAmp = OUTER_NOISE_AMP;
  private float rotationRadians;
  private float scaleTime = SCALE_TIME_REGULAR;
  private float animatePowerSave = 1f;
  private float powerSaveTweenStart;
  private float powerSaveTweenTarget = 1f;
  private float powerSaveTweenElapsed;
  private boolean powerSaveTweenAnimating;

  GrooveDynamics(GrooveConfig config) {
    this.config = config;
    time = config.initialTime();
    seed = config.initialSeed();
  }

  void setTouchStrength(float value) {
    touchStrength = value;
  }

  void setPageOffset(float value) {
    pageOffset = value;
  }

  void setAodValue(float value) {
    aodValue = value;
  }

  void setUnlockedValue(float value) {
    unlockedValue = value;
  }

  void setLoudness(float value) {
    loudnessTarget = value;
  }

  void update(float delta, float aspectRatio) {
    float deltaScaled = scaleTime * delta;
    lockedValue = 1f - (aodValue + unlockedValue);
    time += (PLANE_WIDTH * loudness + 1f) * deltaScaled;

    float rotation = PLANE_ROTATION_DEGREES * map(aodValue, 0f, 1f, 1f, 0.5f);
    float oscillation = (float) (0.5d * (1d + Math.cos(time * PLANE_ROTATION_FREQ * Math.PI * 2d)));
    rotationRadians = (float) Math.toRadians(rotation * oscillation
        * Math.signum((float) Math.cos(time * PLANE_ROTATION_FREQ * 0.5f * Math.PI * 2d)));

    float speedWave = PHASE_SPEED * (2f * loudness + 1f);
    accumulatedPhase = (accumulatedPhase + deltaScaled * speedWave) % 1f;
    updatePowerSaveTween(delta);
    phase = 0.65f + SWIPE_PHASE * pageOffset * unlockedValue * animatePowerSave
        - accumulatedPhase * (float) (Math.PI * 2d);

    float noiseAmpVarPresenceBand = map(0f, 0.4f, 0.7f, 1f, 0f);
    noiseAmpVarPresenceBandEased += (noiseAmpVarPresenceBand - noiseAmpVarPresenceBandEased) * deltaScaled * 4f;
    float noiseAmpVarOsc = (float) (Math.cos(0.05820266452d * time * Math.PI * 2d)
        * Math.sin(0.0254529d * time * Math.PI * 2d));
    float noiseAmpVarOsc2 = lerp(
        expoEaseInOut(map((1f + noiseAmpVarOsc) * 0.5f, 0f, 0.5f, 0f, 1f), 0f, 1f, 1f),
        0f,
        touchStrength);
    noiseAmpVariation = INNER_NOISE_AMP_VARIATION * map(aodValue, 0f, 1f,
        noiseAmpVarOsc2 * noiseAmpVarPresenceBandEased * map(lockedValue, 0f, 1f, 1f, 0f),
        1f);

    noiseAmp = INNER_NOISE_AMP * lerp(1f, 0.12f, aodValue);
    aodVignette = map(aodValue, 0f, 0.5f, 0f, 1f);
    oscAmplitude = (aspectRatio > 1f ? OSC_AMPLITUDE_LANDSCAPE : OSC_AMPLITUDE)
        * (1f - loudness * 0.5f)
        * map(aodValue, 0f, 1f, 1f, 0.5f);
    oscPullAmp = OSC_PULL_AMP * map(lockedValue, 0f, 1f, 1f, 0.5f);
    oscCenter = lerp(aspectRatio > 1f ? OSC_CENTER_LANDSCAPE : config.oscCenter(), config.oscCenterAod(), aodValue + lockedValue);
    outerAmp = OUTER_NOISE_AMP * lerp(1f, 0.25f, aodValue);
    loudness += (loudnessTarget - loudness) * 0.03f;
  }

  void resume() {
    if (time >= 14400f) {
      time = 7200f * (float) Math.random() + .1f;
      seed = 513.3400268554688f * (float) Math.random() + .1f;
    }
  }

  void setLowSpeed(boolean lowSpeed) {
    scaleTime = lowSpeed ? SCALE_TIME_BATTERY_SAVE : SCALE_TIME_REGULAR;
    powerSaveTweenStart = animatePowerSave;
    powerSaveTweenTarget = lowSpeed ? 0f : 1f;
    powerSaveTweenElapsed = 0f;
    powerSaveTweenAnimating = true;
  }

  private void updatePowerSaveTween(float delta) {
    if (!powerSaveTweenAnimating) return;
    powerSaveTweenElapsed += delta;
    float time = Math.min(powerSaveTweenElapsed, 2f) / 2f;
    float transition = -time * (time - 2f);
    animatePowerSave = (powerSaveTweenTarget - powerSaveTweenStart) * transition + powerSaveTweenStart;
    powerSaveTweenAnimating = powerSaveTweenElapsed < 2f;
  }

  float time() { return time; }
  float seed() { return seed; }
  float phase() { return phase; }
  float rotationRadians() { return rotationRadians; }
  float aodValue() { return aodValue; }
  float lockedValue() { return lockedValue; }
  float aodVignette() { return aodVignette; }
  float oscCenter() { return oscCenter; }
  float oscAmplitude() { return oscAmplitude; }
  float aodAmplitude() { return map(aodValue, 0f, 1f, 1f, 2f); }
  float pullNoiseAmplitude() { return oscPullAmp; }
  float outerNoiseAmplitude() { return outerAmp; }
  float innerNoiseAmplitude() { return noiseAmp; }
  float innerNoiseVariation() { return noiseAmpVariation; }

  private static float map(float value, float inMin, float inMax, float outMin, float outMax) {
    if (value < inMin) return outMin;
    if (value > inMax) return outMax;
    return ((value - inMin) / (inMax - inMin)) * (outMax - outMin) + outMin;
  }

  private static float lerp(float from, float to, float progress) {
    return from + (to - from) * progress;
  }

  private static float expoEaseInOut(float time, float begin, float change, float duration) {
    if (time == 0f) return begin;
    if (time == duration) return begin + change;
    float halfTime = time / (duration / 2f);
    if (halfTime < 1f) return change / 2f * (float) Math.pow(2d, 10f * (halfTime - 1f)) + begin;
    return change / 2f * (-(float) Math.pow(2d, -10f * (halfTime - 1f)) + 2f) + begin;
  }
}
