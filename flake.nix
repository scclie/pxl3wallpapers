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
          pkgs = import nixpkgs { inherit system; };
        in {
          default = pkgs.mkShell {
            packages = [
              pkgs.jdk17
              pkgs.gradle
              pkgs.android-tools
              pkgs.jadx
              pkgs.apktool
              pkgs.python3
            ];

            shellHook = ''
              export JAVA_HOME="${pkgs.jdk17}"
              export PATH="$JAVA_HOME/bin:$PATH"
              echo "tools: jadx, apktool, adb, python3, java, gradle"
            '';
          };
        });
    };
}
