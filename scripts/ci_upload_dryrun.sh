#!/usr/bin/env bash
set -euo pipefail
TAG=vTEST
mkdir -p all-installers
# create example artifacts
echo dummy > all-installers/robocode-tank-royale-gui-0.34.2-1.x86_64.rpm
echo dummy > all-installers/robocode-tank-royale-gui_0.34.2_amd64.deb
echo dummy > all-installers/Robocode.Tank.Royale.GUI-1.34.2.pkg
echo dummy > all-installers/Robocode.Tank.Royale.GUI-0.34.2.msi
echo dummy > all-installers/GUI.jar
echo dummy > all-installers/Server.jar
echo dummy > all-installers/sample-csharp.zip
echo dummy > all-installers/sample-java.zip
echo dummy > all-installers/sample-python.zip
# mimic combined checksums
find all-installers -type f ! -name 'SHA256SUMS*' -print0 | sort -z | xargs -0 sha256sum > all-installers/SHA256SUMS
# dry-run upload mapping
urlencode(){ python -c "import sys, urllib.parse as u; print(u.quote(sys.argv[1]))" "$1"; }
upload_asset(){ file_path="$1"; desired_name="$2"; label="$3"; mime=$(file --brief --mime-type "$file_path" || echo application/octet-stream); echo "Would upload: $file_path as $desired_name (label: $label, mime: $mime)"; }
while IFS= read -r -d '' f; do
  rel=${f#./}
  base=$(basename "$rel")
  asset_name="$base"; label="$base"
  case "$base" in
    robocode-tank-royale-gui-*.rpm)
      asset_name=$(echo "$base" | sed -E 's/-[0-9]+\././')
      label="GUI for Linux (Red Hat Package Manager package)"
      ;;
    robocode-tank-royale-gui_*.deb)
      label="GUI for Linux (Debian package)"
      ;;
    *.msi)
      label="GUI for Windows (MSI)"
      ;;
    Robocode.Tank.Royale.GUI-*.pkg)
      asset_name="Robocode.Tank.Royale.GUI-0.34.2.pkg"
      label="GUI for macOS"
      ;;
    *GUI*.jar)
      label="GUI (jar)"
      ;;
    *Server*.jar)
      label="Server (jar)"
      ;;
    *sample*/*csharp*|*Sample*C#*)
      label="Sample bots for C# (zip)"
      ;;
    *sample*bots*java*|*sample-bots*/java/*)
      label="Sample bots for Java (zip)"
      ;;
    *sample*bots*python*|*sample-bots*/python/*)
      label="Sample bots for Python (zip)"
      ;;
    SHA256SUMS)
      continue
      ;;
    SHA256SUMS.asc)
      continue
      ;;
  esac
  upload_asset "$f" "$asset_name" "$label"
done < <(find all-installers -type f -print0)
upload_asset "all-installers/SHA256SUMS" "SHA256SUMS" "SHA256 checksums"
upload_asset "all-installers/SHA256SUMS.asc" "SHA256SUMS.asc" "Signed SHA256 checksums (ASCII-armored)"

