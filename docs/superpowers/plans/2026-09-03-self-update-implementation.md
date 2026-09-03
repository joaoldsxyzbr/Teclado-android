# Self-Update Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a one-tap in-app updater that checks the latest GitHub Release, downloads the official APK, verifies SHA-256, and opens Android's Package Installer.

**Architecture:** Keep update code in a dedicated `br.com.teclado.update` package with no dependency on the IME package. Pure version/release/security logic is unit-testable; Android-specific installation stays behind `UpdateInstaller`; `MainActivity` only renders state and starts work on a background executor.

**Tech Stack:** Kotlin, Android SDK 26+, `HttpURLConnection`, `androidx.core:core:1.17.0` for `FileProvider`, JUnit 4, GitHub Releases API.

**Spec:** `docs/superpowers/specs/2026-09-03-self-update-design.md`

## Global Constraints

- Repository source is fixed to `joaoldsxyzbr/Teclado-android`.
- Only HTTPS URLs on `api.github.com`, `github.com`, or subdomains of `githubusercontent.com` are accepted.
- The updater must never access `KeyboardService`, `InputConnection`, typed text, keyboard state, or typing history.
- No analytics, Firebase, ads, telemetry, accounts, or background polling.
- Installation is never silent; Android Package Installer remains the final authority.
- A release without a valid SHA-256 digest is rejected.
- The first updater-enabled version is installed manually once; future versions can use the button.
- `minSdk = 26`, `targetSdk = 36`, Java 17 remain unchanged.

---

### Task 1: Update security contract, manifest, FileProvider, and build dependencies

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/xml/update_file_paths.xml`
- Modify: `app/src/test/java/br/com/teclado/ManifestPrivacyTest.kt`

**Interfaces:**
- Produces: `FileProvider` authority `${applicationId}.update.fileprovider` with cache path `updates/`.
- Produces: permissions `android.permission.INTERNET` and `android.permission.REQUEST_INSTALL_PACKAGES`.

- [ ] **Step 1: Replace the old privacy test with failing assertions for the new contract**

```kotlin
package br.com.teclado

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ManifestPrivacyTest {
    private val manifest = File("src/main/AndroidManifest.xml").readText()

    @Test fun updaterPermissionsAreExplicit() {
        assertTrue(manifest.contains("android.permission.INTERNET"))
        assertTrue(manifest.contains("android.permission.REQUEST_INSTALL_PACKAGES"))
    }

    @Test fun updateProviderIsPrivate() {
        assertTrue(manifest.contains(".update.fileprovider"))
        assertTrue(manifest.contains("android:exported=\"false\""))
        assertTrue(manifest.contains("android:grantUriPermissions=\"true\""))
    }

    @Test fun noTelemetrySdkMarkersInProductionSources() {
        val source = File("src/main").walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.name.endsWith(".kts")) }
            .joinToString("\n") { it.readText() }
        assertFalse(source.contains("Firebase"))
        assertFalse(source.contains("Analytics"))
    }
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run: `gradle :app:testDebugUnitTest --tests br.com.teclado.ManifestPrivacyTest`
Expected: FAIL because the two update permissions and provider are not present yet.

- [ ] **Step 3: Add the AndroidX Core dependency**

Add to `dependencies`:

```kotlin
implementation("androidx.core:core:1.17.0")
```

Use 1.17.0 because it supports the current `compileSdk = 36` setup without requiring API 36.1.

- [ ] **Step 4: Add permissions and provider to the manifest**

Add above `<application>`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
```

Add inside `<application>`:

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.update.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/update_file_paths" />
</provider>
```

- [ ] **Step 5: Restrict FileProvider to update cache only**

Create `app/src/main/res/xml/update_file_paths.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <cache-path name="updates" path="updates/" />
</paths>
```

- [ ] **Step 6: Re-run the focused test and build**

Run: `gradle :app:testDebugUnitTest --tests br.com.teclado.ManifestPrivacyTest :app:assembleDebug`
Expected: PASS and build exit 0.

- [ ] **Step 7: Commit**

```bash
git add app/build.gradle.kts app/src/main/AndroidManifest.xml app/src/main/res/xml/update_file_paths.xml app/src/test/java/br/com/teclado/ManifestPrivacyTest.kt
git commit -m "feat: prepare secure in-app update permissions"
```

---

### Task 2: Implement pure release, version, URL, and checksum logic

**Files:**
- Create: `app/src/main/java/br/com/teclado/update/UpdateModels.kt`
- Create: `app/src/main/java/br/com/teclado/update/VersionComparator.kt`
- Create: `app/src/main/java/br/com/teclado/update/UpdateUrlPolicy.kt`
- Create: `app/src/main/java/br/com/teclado/update/ChecksumVerifier.kt`
- Create: `app/src/main/java/br/com/teclado/update/GitHubReleaseParser.kt`
- Create: `app/src/test/java/br/com/teclado/update/VersionComparatorTest.kt`
- Create: `app/src/test/java/br/com/teclado/update/UpdateUrlPolicyTest.kt`
- Create: `app/src/test/java/br/com/teclado/update/ChecksumVerifierTest.kt`
- Create: `app/src/test/java/br/com/teclado/update/GitHubReleaseParserTest.kt`

**Interfaces:**
- Produces: `data class ReleaseInfo(val tag: String, val version: String, val apkName: String, val apkUrl: String, val sha256: String, val size: Long)`.
- Produces: `VersionComparator.isNewer(installed: String, candidate: String): Boolean`.
- Produces: `UpdateUrlPolicy.requireAllowed(url: URL): URL`.
- Produces: `ChecksumVerifier.sha256(file: File): String` and `matches(file: File, expected: String): Boolean`.
- Produces: `GitHubReleaseParser.parse(json: String): ReleaseInfo`.

- [ ] **Step 1: Write version comparison tests**

```kotlin
@Test fun newerPatchIsDetected() = assertTrue(VersionComparator.isNewer("1.0.2", "v1.0.3"))
@Test fun equalVersionIsNotNewer() = assertFalse(VersionComparator.isNewer("1.0.2", "1.0.2"))
@Test fun olderCandidateIsNotNewer() = assertFalse(VersionComparator.isNewer("1.2.0", "v1.1.9"))
@Test fun malformedVersionIsRejected() = assertThrows(IllegalArgumentException::class.java) {
    VersionComparator.isNewer("1.0.2", "latest")
}
```

- [ ] **Step 2: Run version tests and verify RED**

Run: `gradle :app:testDebugUnitTest --tests br.com.teclado.update.VersionComparatorTest`
Expected: FAIL because `VersionComparator` does not exist.

- [ ] **Step 3: Implement numeric semantic comparison**

```kotlin
object VersionComparator {
    private fun parts(value: String): List<Int> {
        val clean = value.removePrefix("v")
        require(Regex("\\d+(\\.\\d+)*").matches(clean))
        return clean.split('.').map(String::toInt)
    }

    fun isNewer(installed: String, candidate: String): Boolean {
        val a = parts(installed)
        val b = parts(candidate)
        val size = maxOf(a.size, b.size)
        repeat(size) { i ->
            val left = a.getOrElse(i) { 0 }
            val right = b.getOrElse(i) { 0 }
            if (right != left) return right > left
        }
        return false
    }
}
```

- [ ] **Step 4: Write URL policy tests before implementation**

Test allowed hosts `api.github.com`, `github.com`, `release-assets.githubusercontent.com`, `objects.githubusercontent.com`; reject `http://github.com`, `evil.example`, and `github.com.evil.example`.

- [ ] **Step 5: Implement exact host policy**

```kotlin
object UpdateUrlPolicy {
    fun requireAllowed(url: URL): URL {
        require(url.protocol == "https")
        val host = url.host.lowercase()
        require(host == "api.github.com" || host == "github.com" || host.endsWith(".githubusercontent.com"))
        return url
    }
}
```

- [ ] **Step 6: Write checksum tests and implement SHA-256**

Use a temporary file containing `abc` and assert digest `ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad`.

Implementation:

```kotlin
object ChecksumVerifier {
    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun matches(file: File, expected: String): Boolean =
        sha256(file).equals(expected.removePrefix("sha256:"), ignoreCase = true)
}
```

- [ ] **Step 7: Write release parser tests**

Valid fixture requirements:
- `tag_name = "v1.1.0"`
- asset name `teclado-joaoldsxyzbr-v1.1.0.apk`
- `content_type = "application/vnd.android.package-archive"`
- HTTPS `browser_download_url`
- digest beginning `sha256:`
- positive size.

Invalid fixtures: missing digest, wrong filename, wrong content type, non-GitHub URL.

- [ ] **Step 8: Implement release parser**

Use `org.json.JSONObject` from Android runtime and isolate parsing behind `GitHubReleaseParser`; if local JVM tests need a concrete JSON implementation, add `testImplementation("org.json:json:20250517")` only for tests.

Core selection logic:

```kotlin
val tag = root.getString("tag_name")
val expectedName = "teclado-joaoldsxyzbr-$tag.apk"
val asset = (0 until assets.length())
    .map { assets.getJSONObject(it) }
    .singleOrNull { it.getString("name") == expectedName }
    ?: error("Expected APK asset not found")
```

Require the URL policy, APK MIME, positive size, and a `sha256:` digest before constructing `ReleaseInfo`.

- [ ] **Step 9: Run all update pure-logic tests**

Run: `gradle :app:testDebugUnitTest --tests 'br.com.teclado.update.*'`
Expected: PASS.

- [ ] **Step 10: Commit**

```bash
git add app/src/main/java/br/com/teclado/update app/src/test/java/br/com/teclado/update app/build.gradle.kts
git commit -m "feat: add verified GitHub release model"
```

---

### Task 3: Implement safe HTTP requests and APK download

**Files:**
- Create: `app/src/main/java/br/com/teclado/update/SafeHttpClient.kt`
- Create: `app/src/main/java/br/com/teclado/update/UpdateChecker.kt`
- Create: `app/src/main/java/br/com/teclado/update/ApkDownloader.kt`
- Create: `app/src/test/java/br/com/teclado/update/SafeHttpClientTest.kt`

**Interfaces:**
- Consumes: `UpdateUrlPolicy`, `GitHubReleaseParser`, `ReleaseInfo`.
- Produces: `SafeHttpClient.getText(url: URL): String` and `download(url: URL, target: File, expectedSize: Long)`.
- Produces: `UpdateChecker.latest(): ReleaseInfo`.
- Produces: `ApkDownloader.download(release: ReleaseInfo): File`.

- [ ] **Step 1: Write redirect validation tests against an injectable connection factory**

Define:

```kotlin
fun interface ConnectionFactory {
    fun open(url: URL): HttpURLConnection
}
```

Tests must prove that a redirect from an allowed GitHub URL to `https://evil.example/file.apk` is rejected before the second connection is opened, and that up to five GitHub-host redirects are accepted.

- [ ] **Step 2: Run HTTP client tests and verify RED**

Run: `gradle :app:testDebugUnitTest --tests br.com.teclado.update.SafeHttpClientTest`
Expected: FAIL because client does not exist.

- [ ] **Step 3: Implement manual redirect handling**

For every request:

```kotlin
connection.instanceFollowRedirects = false
connection.connectTimeout = 10_000
connection.readTimeout = 30_000
connection.setRequestProperty("Accept", "application/vnd.github+json")
connection.setRequestProperty("User-Agent", "Teclado-joaoldsxyzbr/${BuildConfig.VERSION_NAME}")
```

On 301/302/303/307/308, resolve `Location`, call `UpdateUrlPolicy.requireAllowed()` again, and stop after 5 redirects.

- [ ] **Step 4: Implement release check**

`UpdateChecker.latest()` must request exactly:

```text
https://api.github.com/repos/joaoldsxyzbr/Teclado-android/releases/latest
```

and return `GitHubReleaseParser.parse(response)`.

- [ ] **Step 5: Implement private-cache downloader**

`ApkDownloader` creates `File(context.cacheDir, "updates")`, deletes stale `.apk` files, downloads to `*.part`, validates byte count when `expectedSize > 0`, then atomically renames to the expected APK filename.

- [ ] **Step 6: Run update tests and assemble**

Run: `gradle :app:testDebugUnitTest :app:assembleDebug`
Expected: PASS and build exit 0.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/br/com/teclado/update app/src/test/java/br/com/teclado/update
git commit -m "feat: download updates through restricted GitHub client"
```

---

### Task 4: Add update state machine and Android installer

**Files:**
- Create: `app/src/main/java/br/com/teclado/update/UpdateState.kt`
- Create: `app/src/main/java/br/com/teclado/update/SelfUpdateManager.kt`
- Create: `app/src/main/java/br/com/teclado/update/UpdateInstaller.kt`
- Create: `app/src/test/java/br/com/teclado/update/SelfUpdateManagerTest.kt`

**Interfaces:**
- Consumes: `UpdateChecker`, `VersionComparator`, `ApkDownloader`, `ChecksumVerifier`, `UpdateInstaller`.
- Produces: `sealed interface UpdateState` with `Checking`, `UpToDate`, `Available`, `Downloading`, `Verifying`, `PermissionRequired`, `Installing`, `Failed`.
- Produces: `SelfUpdateManager.run(installedVersion: String, onState: (UpdateState) -> Unit)`.

- [ ] **Step 1: Write state-machine tests with fake collaborators**

Cover:
- equal release -> `Checking`, `UpToDate` and no download;
- newer release -> `Checking`, `Available`, `Downloading`, `Verifying`, `Installing`;
- digest mismatch -> `Failed` and installer never called;
- installer permission missing -> `PermissionRequired`;
- network/parser exception -> `Failed` without touching keyboard code.

- [ ] **Step 2: Run manager tests and verify RED**

Run: `gradle :app:testDebugUnitTest --tests br.com.teclado.update.SelfUpdateManagerTest`
Expected: FAIL because manager/state classes do not exist.

- [ ] **Step 3: Implement the one-tap orchestration**

The manager must be synchronous from its own perspective; `MainActivity` is responsible for running it off the UI thread. It emits states before each long-running operation and never catches `SecurityException` as success.

- [ ] **Step 4: Implement installation permission handling**

Use `packageManager.canRequestPackageInstalls()` on API 26+.

When false, open:

```kotlin
Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
    data = Uri.parse("package:$packageName")
}
```

and return `PermissionRequired` without opening the APK.

- [ ] **Step 5: Implement FileProvider installation intent**

```kotlin
val uri = FileProvider.getUriForFile(
    context,
    "${context.packageName}.update.fileprovider",
    apk
)
val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
    data = uri
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, false)
}
context.startActivity(intent)
```

If `ACTION_INSTALL_PACKAGE` cannot be handled, fall back to `ACTION_VIEW` with MIME `application/vnd.android.package-archive`, still using the same `content://` URI and read grant.

- [ ] **Step 6: Run manager tests and full build**

Run: `gradle :app:testDebugUnitTest :app:assembleDebug`
Expected: PASS and build exit 0.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/br/com/teclado/update app/src/test/java/br/com/teclado/update
git commit -m "feat: verify and hand updates to Android installer"
```

---

### Task 5: Integrate updater UI, privacy copy, CI audit, and release v1.1.0

**Files:**
- Modify: `app/src/main/java/br/com/teclado/MainActivity.kt`
- Modify: `app/src/main/res/layout/activity_main.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `README.md`
- Modify: `.github/workflows/android.yml`
- Modify: `app/build.gradle.kts`

**Interfaces:**
- Consumes: `SelfUpdateManager` and `UpdateState`.
- Produces: visible `@id/check_update` button and `@id/update_status` text.

- [ ] **Step 1: Add updater UI strings**

Use:

```xml
<string name="check_update">Verificar atualização</string>
<string name="update_checking">Verificando…</string>
<string name="update_current">Você já está na versão mais recente.</string>
<string name="update_available">Nova versão %1$s disponível.</string>
<string name="update_downloading">Baixando atualização…</string>
<string name="update_verifying">Validando atualização…</string>
<string name="update_installing">Abrindo instalador…</string>
<string name="update_permission">Autorize “Instalar apps desconhecidos” e toque novamente.</string>
<string name="update_error">Não foi possível atualizar: %1$s</string>
```

Change privacy copy so it no longer falsely says the app has no internet permission. Use wording that explicitly says typing remains local and internet is only used when the user taps the update button.

- [ ] **Step 2: Add status TextView and update Button below keyboard selection**

The button is full width, 52dp high, `textAllCaps=false`; the status text is centered and hidden/empty until an update operation starts.

- [ ] **Step 3: Wire MainActivity with a single-thread executor**

Create the manager once in `onCreate`, and on button click:

```kotlin
checkUpdateButton.isEnabled = false
executor.execute {
    updater.run(BuildConfig.VERSION_NAME) { state ->
        runOnUiThread { renderUpdateState(state) }
    }
    runOnUiThread { checkUpdateButton.isEnabled = true }
}
```

`renderUpdateState` maps every state to the strings above. Shut down the executor in `onDestroy()`.

- [ ] **Step 4: Replace absolute no-network CI audit with isolation audit**

The audit must:
- require `INTERNET` and `REQUEST_INSTALL_PACKAGES` in the manifest;
- fail on `Firebase|Analytics|Telemetry` markers;
- fail if `HttpURLConnection`, `URLConnection`, `java.net.URL(`, `https://` or `http://` appears under `app/src/main/java/br/com/teclado/ime`;
- fail on literal HTTP URLs anywhere in production Kotlin;
- allow production HTTPS literals only under `br/com/teclado/update` and only GitHub hosts;
- keep `testDebugUnitTest` and `assembleDebug` as mandatory gates.

- [ ] **Step 5: Update README privacy model**

State explicitly that the keyboard itself is offline and that network access is only used after a manual tap on `Verificar atualização` to access this repository's GitHub Release infrastructure.

- [ ] **Step 6: Bump version to the first updater release**

Set:

```kotlin
versionCode = 4
versionName = "1.1.0"
```

This causes the existing release workflow to publish `teclado-joaoldsxyzbr-v1.1.0.apk` after the main build passes.

- [ ] **Step 7: Run final local-equivalent verification**

Run: `gradle clean testDebugUnitTest assembleDebug`
Expected: exit 0, all unit tests pass, APK generated.

Run privacy checks from `.github/workflows/android.yml` exactly as CI will execute them.
Expected: exit 0.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/br/com/teclado/MainActivity.kt app/src/main/res/layout/activity_main.xml app/src/main/res/values/strings.xml README.md .github/workflows/android.yml app/build.gradle.kts
git commit -m "feat: add one-tap verified app updater v1.1.0"
```

- [ ] **Step 9: Verify GitHub Actions and release**

Confirm the `main` workflow run reports success for `Test and build`, `Privacy audit`, `Upload debug APK`, and `Publish version release`. Then fetch `/releases/tags/v1.1.0` and verify asset name `teclado-joaoldsxyzbr-v1.1.0.apk`, non-zero size, and published state.

---

## Plan self-review

- Spec coverage: version comparison, release parsing, asset selection, redirect host validation, SHA-256, private cache, FileProvider, unknown-source permission, one-tap state flow, UI, privacy isolation, CI, and first updater release are all mapped to tasks.
- Placeholder scan: no TBD/TODO or unspecified implementation steps remain.
- Type consistency: `ReleaseInfo`, `UpdateState`, `UpdateUrlPolicy`, `SafeHttpClient`, `UpdateChecker`, `ApkDownloader`, `UpdateInstaller`, and `SelfUpdateManager` are introduced before consumers depend on them.
