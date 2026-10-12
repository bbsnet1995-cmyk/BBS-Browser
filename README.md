# BBS Browser — Android starter project

BBS Browser is a minimal Android browser prototype using Mozilla GeckoView, with a green/blue/aqua interface and a local `.xpi` extension picker.

## Current prototype features
- Browser engine based on GeckoView (Firefox engine).
- Address/search bar, open, back, forward, reload.
- Select an extension package from device storage and ask GeckoView to install it.
- Minimal green, blue, and aqua styling.

## Important limitations
- This is a **starter source project**, not a verified APK.
- A successful extension installation generally requires a Mozilla-signed, compatible `.xpi` extension. Arbitrary unsigned or Chrome-only extensions are not guaranteed to work.
- Not every website can be guaranteed to work: sites may block embedded browsers, require special device APIs, or behave differently from Chrome.
- This prototype does not yet include tabs, bookmarks, download manager, private browsing UI, safe-browsing controls, extension management UI, or polished permission prompts.
- The selected file is copied to the app cache before installation. Review extension permissions and only install extensions you trust.

## Build in Android Studio
1. Install Android Studio and a JDK 17.
2. Open this project folder.
3. Let Gradle sync. Internet access is needed to download Android and GeckoView dependencies.
4. Build > Build Bundle(s) / APK(s) > Build APK(s).
5. Install the generated debug APK on an Android device and test navigation and extension installation.

If Gradle sync fails because the GeckoView version is unavailable for your environment, check Mozilla's GeckoView Maven repository and update the dependency in `app/build.gradle.kts`.

## Technical references
- GeckoView quick start: https://mozilla.github.io/geckoview/consumer/docs/geckoview-quick-start
- GeckoView WebExtensions: https://mozilla.github.io/geckoview/consumer/docs/web-extensions
## Build APK in the cloud (no PC required)
The repository includes `.github/workflows/android-apk.yml`. Upload this project to a GitHub repository from your phone, then open the repository's **Actions** tab and run **Build BBS Browser APK** (or push to `main`). After the workflow finishes, open its run, scroll to **Artifacts**, and download `BBS-Browser-debug-apk`. Extract the ZIP to get `app-debug.apk`.

This workflow is a build recipe, not a prebuilt APK. The first cloud build can still fail if dependency/API compatibility needs fixes. The APK produced by this workflow is a debug build, not a Play Store release.
