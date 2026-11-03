{
  description = "local tools for pxlwallpapers";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

  outputs = { self, nixpkgs }:
    let
      systems = [ "x86_64-linux" "aarch64-linux" ];
      forEachSystem = nixpkgs.lib.genAttrs systems;
    in {
      devShells = forEachSystem (system:
        let
          pkgs = import nixpkgs {
            inherit system;
            config = {
              allowUnfree = true;
              android_sdk.accept_license = true;
            };
          };
          androidSdk = pkgs.androidenv.composeAndroidPackages {
            platformVersions = [ "36" ];
            buildToolsVersions = [ "36.0.0" ];
            includeEmulator = false;
            includeNDK = false;
          };
        in {
          default = pkgs.mkShell {
            packages = [
              pkgs.jdk17
              pkgs.gradle
              pkgs.android-tools
              pkgs.jadx
              pkgs.apktool
              pkgs.python3
              androidSdk.androidsdk
            ];

            shellHook = ''
              export JAVA_HOME="${pkgs.jdk17}"
              export PATH="$JAVA_HOME/bin:$PATH"
              export NIX_ANDROID_HOME="${androidSdk.androidsdk}/libexec/android-sdk"
              export ANDROID_HOME="$PWD/.android-sdk"
              export ANDROID_SDK_ROOT="$ANDROID_HOME"
              mkdir -p "$ANDROID_HOME/platforms" "$ANDROID_HOME/build-tools"
              ln -sfn "$NIX_ANDROID_HOME/platforms/android-36" "$ANDROID_HOME/platforms/android-36"
              ln -sfn "$NIX_ANDROID_HOME/build-tools/36.0.0" "$ANDROID_HOME/build-tools/36.0.0"
              ln -sfn "$NIX_ANDROID_HOME/licenses" "$ANDROID_HOME/licenses"
              echo "tools: jadx, apktool, adb, python3, java, gradle"
            '';
          };
        });
    };
}
