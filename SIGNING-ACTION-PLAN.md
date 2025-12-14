# SIGNING-ACTION-PLAN

Minimal action plan focused on GPG-only signing, aligned with step 8 ("Signing Installers and Artifacts") in
`jpackage-execution-plan.md`.

The goal is:

- Reuse the **existing** GPG key already used for Maven Central JARs.
- Sign **checksums** for all installers.
- Optionally sign **Linux artifacts** individually.
- Automate this in **GitHub Actions**.

Everything about paid Windows/macOS code-signing certs, Apple Developer ID, etc. is **out of scope for now**.

---

## 1. What You Already Have

You already have:

- A PGP/GPG key pair used for signing JAR artifacts for Maven Central (see `publish.md`).
- The private key exported as `.asc`.
- The public key exported as `.asc`.
- The key identity, e.g. `Flemming Nørnberg Larsen <flemming.n.larsen@gmail.com>`.
- Local Gradle configured with `signingKey` and `signingPassword` in `%USERPROFILE%\.gradle\gradle.properties` so
  `./gradlew publishToSonatype` works.

This **single** key will also be used to sign:

- A checksum file (`SHA256SUMS`) for all platform installers created by jpackage.
- Optional Linux artifacts (e.g. `.deb`, `.rpm`, AppImage).

---

## 2. Minimal To-Do List (to unblock jpackage step 8)

These are the only things you need to do now.

### 2.1 Verify the Key and Passphrase on Your Windows Dev Machine

1. Install GPG on Windows (e.g. [Gpg4win](https://www.gpg4win.org/)) if not already installed.
2. Import your private key `.asc`:
   ```powershell
   gpg --import .\my-secret-key.asc
   ```
3. Test that the passphrase works by signing a dummy file:
   ```powershell
   echo test > test.txt
   gpg --pinentry-mode loopback --sign test.txt
   ```
4. If the command completes without a "Bad passphrase" error, the key + passphrase are OK.

> This step is just to be sure the key file you plan to use in CI is valid and matches your password.

### 2.2 Add the Key to GitHub Actions

1. Open your private key `.asc` file and copy **all** of it, including the `-----BEGIN` / `-----END` lines.
2. In the GitHub repository:
    - Go to **Settings → Secrets and variables → Actions**.
    - Add:
        - `GPG_PRIVATE_KEY` – value: full `.asc` private key content.
        - `GPG_PASSPHRASE` – value: the same passphrase as `signingPassword` in `gradle.properties`.

These two secrets are what the workflows will use to import the key and sign files.

### 2.3 Publish the Public Key for Users

1. Create a top-level folder `keys/` in the repo.
2. Inside it, create a file `keys/KEYS`.
3. Paste the full content of your **public** key `.asc` into `keys/KEYS`.
4. (Later) Link to `/keys/KEYS` from your website / README / GitHub Pages so users can verify signatures.

This is needed so users can actually verify your signatures, but does not block CI work.

### 2.4 Add GPG Signing Steps to the Release Workflow

In the GitHub Actions workflow that already builds jpackage installers (e.g.
`.github/workflows/package-release.yml`):

1. **Import the GPG key** early in the job that prepares the release (Linux or Windows runner):

   ```yaml
   - name: Import GPG key
     run: echo "$GPG_PRIVATE_KEY" | gpg --batch --import
     env:
       GPG_PRIVATE_KEY: ${{ secrets.GPG_PRIVATE_KEY }}
   ```

2. **Generate checksums** for all installers produced by jpackage.

   Adjust the directory and commands to match where your installers are placed (example for Linux shell):

   ```yaml
   - name: Generate SHA256 checksums
     run: |
       cd dist  # or wherever jpackage installers end up
       sha256sum * > SHA256SUMS
   ```

   For a Windows runner using PowerShell, an example could be:

   ```yaml
   - name: Generate SHA256 checksums (Windows)
     shell: pwsh
     run: |
       Set-Location dist  # adjust path
       Get-ChildItem -File | ForEach-Object {
         $hash = Get-FileHash $_.FullName -Algorithm SHA256
         "$($hash.Hash)  $($_.Name)" | Out-File -Encoding ascii -Append SHA256SUMS
       }
   ```

3. **Sign the checksum file** with your existing GPG key:

   ```yaml
   - name: Sign SHA256SUMS
     run: |
       cd dist  # same directory as above
       gpg --batch --yes --pinentry-mode loopback \
           --passphrase "$GPG_PASSPHRASE" \
           --armor --detach-sign SHA256SUMS
     env:
       GPG_PASSPHRASE: ${{ secrets.GPG_PASSPHRASE }}
   ```

   This will create `SHA256SUMS.asc` alongside `SHA256SUMS`.

4. **(Optional) Sign Linux installers individually** (if you later add `.deb`/`.rpm`/AppImage):

   ```yaml
   - name: Sign Linux installer with GPG (optional)
     run: |
       cd dist
       gpg --batch --yes --pinentry-mode loopback \
           --passphrase "$GPG_PASSPHRASE" \
           --armor --detach-sign your-linux-installer-file
     env:
       GPG_PASSPHRASE: ${{ secrets.GPG_PASSPHRASE }}
   ```

5. Make sure `SHA256SUMS` and `SHA256SUMS.asc` are **uploaded as artifacts** or attached to the GitHub Release
   together with the installers.

---

## 3. How This Maps to jpackage Step 8

In `jpackage-execution-plan.md`, step 8 is:

> **Signing Installers and Artifacts**

With this minimal plan, you can mark the following as done (or in progress):

- **Clarify signing requirements for Windows, Linux, and macOS installers**
    - Decision: **no platform-native signing for now**.
    - Only **GPG signing of checksums** (and optionally Linux artifacts) using the existing key.

- **Decide if signing is required for each platform**
    - Windows: no native signing, but included in `SHA256SUMS` that is GPG-signed.
    - macOS: same as Windows.
    - Linux: same as above, with optional per-artifact GPG signing.

- **Choose signing method/tool for each platform**
    - Tool: `gpg` only.

- **Obtain necessary certificates/keys for signing**
    - Already done: reuse existing Maven Central GPG key.

- **Decide how to securely store and access certificates/keys for CI/CD**
    - Store private key and passphrase in GitHub Actions secrets: `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`.

- **Configure signing in build scripts and GitHub Actions**
    - Only change needed: add the GPG import + checksum + signing steps to the existing packaging workflow.

Once you’ve done the items in section 2, you can confidently tick off the signing-related sub-items in
`jpackage-execution-plan.md` and move on to uploading and testing installers.
