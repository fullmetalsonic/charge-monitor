# Decision and change log

> [한국어 원문 / Korean original](../03_결정_및_변경기록.md)

This file accumulates project requirements, technical decisions, and verification results in date order. Do not delete or overwrite earlier entries; add a new dated entry at the bottom with the reason for a change.

## Entry format

```md
## YYYY-MM-DD - Title

- Category: requirement | decision | verification | change | deferred
- Details:
- Evidence or verification method:
- Affected files:
- Status: confirmed | needs verification | deferred
```

## 2026-08-18 - Planning started

- Category: requirement
- Details: Build an app that automatically shows estimated charging power in a status-bar notification when a charger is connected to a Galaxy Z Fold8, with AOD notification visibility when device settings permit it.
- Evidence: User requirement; verify actual AOD behavior with Fold8 settings.
- Affected files: `docs/01_기획서.md`, `docs/02_구조_및_파일분배.md`
- Status: confirmed

## 2026-08-18 - AOD boundary

- Category: decision
- Details: Exclude direct watt-text insertion into Samsung's stock AOD layout. Support only the system's visibility of standard Android notifications.
- Evidence: One UI AOD notification settings and real Fold8 validation are required.
- Affected files: `docs/01_기획서.md`
- Status: confirmed

## 2026-08-18 - Automatic-detection approach

- Category: decision
- Details: Do not rely only on a charger-connect broadcast to start long-running measurement. Prioritize a persistent execution model for users who enable automatic monitoring.
- Evidence: Recheck current Android background/foreground-service restrictions before implementation and measure on Fold8.
- Affected files: `docs/01_기획서.md`, `docs/02_구조_및_파일분배.md`
- Status: needs verification

## 2026-08-18 - Code deferred

- Category: decision
- Details: At this stage, create only the plan and file layout; do not create the Android project, Kotlin code, or Gradle configuration yet.
- Evidence: User request.
- Affected files: entire project
- Status: confirmed

## 2026-08-18 - Android compatibility and distribution

- Category: requirement
- Details: Minimum compatibility is Android 14 (API 34), targeting the latest stable Android version at implementation time. Play Store distribution is deferred.
- Evidence: User confirmation; recheck current Android and Play policies before implementation.
- Affected files: `docs/01_기획서.md`
- Status: confirmed

## 2026-08-18 - Standby notification and design direction

- Category: decision
- Details: Use `Charge Monitor` as standby notification text. Design a modern interface around a battery outline and lightning bolt with generous whitespace and one accent color.
- Evidence: User requirement; validate contrast and folded/unfolded layouts after implementation.
- Affected files: `docs/01_기획서.md`, `docs/02_구조_및_파일분배.md`
- Status: confirmed

## 2026-08-18 - Graph Engineering workflow

- Category: decision
- Details: Link each feature through requirement, design, implementation, real validation, independent review, and a fix loop. Accumulate evidence and unverified items.
- Evidence: User requirement and global development procedure.
- Affected files: `docs/04_개발_및_검증_루프.md`, `docs/03_결정_및_변경기록.md`
- Status: confirmed

## 2026-08-20 - Selected UI and automatic-monitoring toggle

- Category: implementation
- Details: Implemented a Compose dashboard with circular charge gauge, battery/lightning icon, current watts, voltage/current/battery percent, `Automatic monitoring` toggle, and detailed diagnostics. Turning the toggle off stops both service and notification.
- Evidence: Selected UI concept and user approval. Dashboard, service, battery API, notification, and settings storage were separated into Kotlin files.
- Affected files: `app/src/main/java/com/chargemonitor/**`
- Status: implementation complete; real-device verification required

## 2026-08-20 - Android 17 build configuration

- Category: decision
- Details: Use AGP 9.1.1, Gradle 9.3.1, and JDK 17 for Android 17 (API 37) compatibility while keeping Android 14 (API 34) as the minimum version.
- Evidence: Android's official AGP compatibility table and Android 17 SDK setup documentation.
- Affected files: `build.gradle.kts`, `app/build.gradle.kts`, `gradle/wrapper/gradle-wrapper.properties`
- Status: confirmed

## 2026-08-20 - Build and static validation

- Category: verification
- Details: `test assembleDebug lintDebug` succeeded. The debug APK was verified as v2-signed with the Android Debug certificate.
- Evidence: Gradle 9.3.1 output and `apksigner verify --verbose --print-certs`.
- Affected files: `app/build/outputs/apk/debug/app-debug.apk`
- Status: passed

## 2026-08-20 - Real-device validation deferred

- Category: deferred
- Details: No ADB device was connected, so Fold8 installation, charging voltage/current, status-bar notification, and AOD visibility were not run.
- Evidence: `adb devices -l` showed no connected device.
- Affected files: none
- Status: needs verification

## 2026-08-20 - Pre-install independent review

- Category: change and verification
- Details: Safely handled paths where foreground-service start can be denied during boot or system-restricted state; the service no longer crashes and only logs a warning. Independently reviewed service start, manifest permissions, notifications, settings persistence, and boot receiver.
- Evidence: Re-ran `test assembleDebug lintDebug` after the change.
- Affected files: `MonitoringServiceController.kt`, this log
- Status: static validation passed; real-device verification required

## 2026-08-20 - Private GitHub delivery

- Category: decision
- Details: Created a private `charge-monitor` GitHub repository and attached the validated debug APK as the `v0.1.0` Release asset so the user could download it on a phone.
- Evidence: User approval; source and docs are tracked in Git, while build artifacts are only release assets.
- Affected files: `.gitignore`, private GitHub repository, GitHub Release
- Status: complete

## 2026-08-20 - Fold8 feedback: full charge, notification, safe insets

- Category: change
- Details: A `FULL` battery with near-zero or negative current is shown as `Charge complete`, not standby. Ongoing notifications use state-specific and lock-screen-public text. Dashboard layout accounts for system insets so it does not collide with navigation controls.
- Evidence: Fold8 screenshot showed 100%, 4.45 V, and -0.01 A; prior classification treated it as standby. AOD behavior still depends on One UI settings.
- Affected files: battery data source, state classification, notification, dashboard, unit tests
- Status: complete (`v0.1.1` private release); AOD device verification still required

## 2026-08-20 - Charging-start and discharging states

- Category: change
- Details: With no charger connected, show `Discharging`. Immediately after connection, show `Checking charging state` for the first two measurements; only later show `Current measurement unavailable` if no current is available.
- Evidence: The persistent notification is created at service start while current refreshes every two seconds. This separates the initial state to reduce confusion on lock-screen/AOD surfaces that may show only the first notification.
- Affected files: state model/classification/stabilization, service, notification, dashboard, unit tests
- Status: complete (`v0.1.2` private release); AOD device verification still required

## 2026-08-20 - Discharging-power display

- Category: change
- Details: Classify negative current as discharging and calculate positive power magnitude, displaying strings such as `0.6 W discharging` in both UI and ongoing notification.
- Evidence: Fold8 reading of 4.38 V and -0.13 A supports an estimated 0.57 W discharging value.
- Affected files: power calculation, notification, unit tests
- Status: complete (`v0.1.3` private release)

## 2026-08-20 - Corrected toggle wording

- Category: change
- Details: Changed the automatic-monitoring description to `Show charging/discharging status in notifications` so it does not imply direct AOD watt insertion.
- Evidence: Fold8 shows watts through status-bar/lock-screen notification; AOD visibility is restricted by One UI settings and policy.
- Affected files: dashboard text
- Status: complete (`v0.1.4` private release)

## 2026-08-20 - Public release signing and review

- Category: change and verification
- Details: Created a Git-ignored 4,096-bit RSA release key in `.signing`, connected it to the release build, and adopted `ChargeMonitor-v0.1.4.apk` as the public filename.
- Evidence: `test assembleRelease lint` passed; package/signing certificate were checked. The signature differs from earlier debug builds, so debug installs must be removed before installing the release APK.
- Affected files: `.gitignore`, `app/build.gradle.kts`, `.signing/` (Git-ignored), GitHub Release
- Status: complete; repository made public and signed `v0.1.4` APK released

## 2026-08-20 - README examples and full-charge percent fix

- Category: documentation, asset change, and bug fix
- Details: Added privacy-cropped dashboard examples to the public README and updated it for current functionality/download instructions. Fixed a `100%% charged` diagnostic state so full charge shows simply `Charge complete`.
- Evidence: The duplicated `%%` came from a state string rendered without a format function; battery-level and notification format strings had separate single-percent paths.
- Affected files: `README.md`, `assets/readme/dashboard-discharge.png`, dashboard, detailed diagnostics
- Status: complete (`v0.1.10` public release for the percent fix)

## 2026-08-20 - Experimental status-bar watt display

- Category: feature addition
- Details: Added an opt-in `Status-bar watts (experimental)` toggle. On Android 16 QPR1 (API 36.1)+ it requested system promotion to a status-bar chip, with ordinary ongoing notification as a safe fallback when unsupported.
- Evidence: Applied official Live Update promotion APIs. `test assembleRelease lint` passed; Fold8 chip visibility required installation testing.
- Affected files: settings, dashboard, notification factory, foreground service, manifest, language resources
- Status: complete (`v0.1.11` public release); later removed when the system did not offer reliable support

## 2026-08-20 - 30-day trend history

- Category: feature addition
- Details: Added a user-controlled `Trend history` toggle. When enabled, the app aggregates two-second measurements into five-minute averages, stores them on-device, and cleans entries older than 30 days on the next save.
- UI: Implemented the selected concept with recent-date selection, daily battery flow, charging/discharging periods, charge-session/gained/discharged/power-peak summary, and charging-power changes.
- Data: No network or account permission; data remains only in the app's internal storage. Turning the toggle off stops new writes but preserves previous history.
- Evidence: Added unit tests for five-minute aggregation and daily summary; `test assembleRelease lint` passed.
- Affected files: trend model, repository, analysis, tests, trend UI, dashboard, navigation, language resources
- Status: complete (`v0.1.12` public release); Fold8 layout and 30-day accumulation needed further validation

## 2026-08-20 - Trend-date navigation and entry order

- Category: bug fix and UI adjustment
- Details: Separate selected date from the visible date strip so today remains selectable after opening a past date. Swipe the strip smoothly by one day, not seven, and prevent future dates. Place `Trend history ›` directly under its toggle and `Detailed diagnostics ›` below it.
- Evidence: A Fold8 screen showed that today could not be returned to and swipe did not work.
- Affected files: `TrendViewModel.kt`, `TrendScreen.kt`, `DashboardScreen.kt`
- Status: complete (`v0.1.13` public release); `test assembleRelease lint` passed

## 2026-08-20 - Quick Settings watt tile and daily timeline

- Category: feature addition, bug fix, and UI improvement
- Details: Added a Quick Settings tile to view current charging/discharging watts and toggle automatic monitoring. Trend charts now use fixed 00:00–24:00 time positions rather than expanding to the number of samples.
- UI: Both graphs show `00 · 06 · 12 · 18 · 24`. Left swipe moves to the next day; right swipe moves to the previous day.
- Data: After midnight, a daily summary uses only the new date, so a new chart accumulates again from the left at 00:00.
- Evidence: `test assembleRelease lint` passed. Fold8 verified the fixed daily axis, real sample placement, and swipe direction. The tile can be repositioned in Quick Settings edit mode.
- Affected files: Quick Settings tile service, manifest, settings, trend timeline, tests, trend UI
- Status: complete (`v0.1.14` public release)

## 2026-08-20 - Icon corrections and language packs

- Category: bug fix, UI change, and localization
- Details: Unified home, notification, and status-bar icon treatment around a blue battery/plug graphic and a simple monochrome horizontal plug silhouette. Replaced incorrect vector reinterpretations that appeared as a `+` sign with approved PNG assets, then removed the large duplicate notification icon by dropping `setLargeIcon`.
- Localization: Added core dashboard and notification messages for Korean, English, Japanese, Simplified/Traditional Chinese, Spanish, French, German, Brazilian Portuguese, Russian, Arabic, and Hindi.
- Evidence: Device screenshots exposed the misleading icon shape and duplicate left/right notification image. `test assembleRelease lint` passed; Arabic RTL remained a device-validation item.
- Affected files: manifest, notification factory, `res/drawable*`, `res/drawable-nodpi`, `res/values*`, dashboard
- Status: complete (`v0.1.5`–`v0.1.9` public releases)

## 2026-08-20 - Privacy-cropped real-device screens

- Category: documentation and asset change
- Details: Added actual Fold screenshots for discharging, charging, and monitoring-off states to README. Removed only the status-bar and bottom navigation areas so carrier, time, and status information are not public.
- Evidence: Visually checked that the displayed 7.6 W discharge, 10.3 W charge, and toggle-off state match the original screens.
- Affected files: `README.md`, `assets/readme/dashboard-*-device.png`
- Status: complete

## 2026-08-20 - Trend time-selection cursor

- Category: feature addition and UI improvement
- Details: Tapping or dragging either trend chart snaps the touch location to the nearest five-minute bucket. Both charts show the same vertical selection line. A recorded bucket shows time, battery level, and charging or discharging watts; an empty bucket shows time and `No record`. The last selected value remains after touch release.
- Evidence: The storage model uses five-minute average buckets, so missing periods are never rendered as estimates. Fold8 verified an empty 11:25, recorded 20:00 data, dragging, and retained selection.
- Affected files: `TrendTimeline.kt`, `TrendTimelineTest.kt`, `TrendScreen.kt`, `res/values*`, `build.gradle.kts`
- Status: complete (`v0.1.15` public release); `test assembleRelease lint` passed

## 2026-08-20 - Structure, performance, and localization optimization

- Category: optimization, bug fix, UI improvement, and translation expansion
- Details: Removed the unsupported experimental status-bar watts toggle and promotion path; retained ordinary ongoing notifications and the Quick Settings watt tile. Sampling stays at two seconds while charging and uses longer intervals while discharging/idle according to screen state; notifications repost only when visible content changes.
- Data: Changed trend persistence from rewriting the full JSON file every two seconds to memory aggregation with safe writes on five-minute bucket change or service shutdown. `AtomicFile` lowers the risk of data corruption during an interrupted save.
- UI: On Fold landscape, split gauge and settings/navigation into two columns. Corrected date-swipe direction, added immediate `Today` return, and keep the selected-time card directly under the graph.
- Localization: Completed new guidance and detailed-diagnostic translations for all 11 supported languages.
- Evidence: `lintDebug` and `assembleRelease` passed. Twenty-one JVM tests passed through direct JUnit execution because Gradle workers in the Korean Windows path could not find classes. Fold8 (Android 17) verified service restart, portrait/landscape dashboard, past-date-to-Today return, swipe, chart-time selection, and a single notification icon. Also fixed a discovered update issue by restarting the service when dashboard opens with monitoring enabled.
- Affected files: monitoring, notification, settings, trend repository, dashboard, trend UI, domain policy, language resources, tests, README
- Status: complete (`v0.1.16` public release). Signing certificate SHA-256: `229b6020bc74d06279d4ead5f8a00c119f3a599304a156bd392aeb4333410630`; APK SHA-256: `F48D892ED7DFD6F98715F4DC1B5AC6DD1363FD5259E2D9149269555FC5FAEBA7`.

## 2026-08-20 - Precise 1-minute trend option

- Category: feature addition, UI improvement, and localization
- Details: Tapping the explanation area of the Trend history row opens settings. Users choose `Standard · 5-minute average` or `Precise · 1-minute average`; the interval applies to new records and both modes retain the latest 30 days.
- UI: The on/off switch remains separate while interval choice moves to its own settings sheet. Dashboard description, empty-chart note, power-average note, and touch cursor reflect the selected 1- or 5-minute interval.
- Data: Changing intervals never deletes history. Displaying stored one-minute samples at five minutes averages each group of five. Switching old five-minute data to a one-minute view does not invent intermediate values: historic five-minute samples remain at five-minute locations while new samples use one minute. New buckets are inserted and aggregated chronologically.
- Localization: Added settings-sheet and one-/five-minute guidance in all 11 existing languages.
- Evidence: `lintDebug` and `assembleRelease` passed. Twenty-seven JVM tests covered 1→5-minute conversion, mixed historic five-minute and new one-minute display, and cursor behavior after an interval change. Fold8 (Android 17) verified the sheet, one-minute selection, persistence after restart, and return to default five minutes.
- Affected files: settings, trend repository, timeline domain, dashboard control, trend UI, language resources, unit tests, README
- Status: complete (`v0.1.17` public release). Signing certificate SHA-256: `229b6020bc74d06279d4ead5f8a00c119f3a599304a156bd392aeb4333410630`; APK SHA-256: `D6E88F527FD48EDCC95D8232944441BC59490B54D5154019319CD698D442268E`.

## 2026-08-20 - Discoverable trend settings and README refresh

- Category: UI, documentation, and asset improvement
- Details: In the `Trend history` row, the right switch controls recording only. The left description shows the current interval and a prominent `Trend recording settings ›`, making the settings-sheet entry clear. Graph and detailed-diagnostic links remain unchanged.
- Documentation: Refreshed README for ongoing notification, Quick Settings tile, and 30-day history, and added the settings/precise-one-minute screens. Public images were cropped to remove status-bar, carrier, time, and bottom-navigation details.
- Evidence: Verified the settings-entry copy, separation of switch behavior, and privacy crop visually; statically checked document links, image paths, and changed files. On Fold8 Android 17, verified entry copy, separated switch behavior, and standard five-minute restoration. `assembleRelease` and `lintDebug` passed.
- Affected files: `DashboardControls.kt`, `README.md`, `assets/readme/trend-recording-*-device.png`
- Status: complete

## 2026-08-21 - Locked-state power connect/disconnect recording correction

- Category: bug fix and device validation
- Details: On power connect/disconnect broadcast, perform the next measurement immediately. Right after connecting, show `Checking charging state` until battery state is confirmed and exclude this short interval from trend history so locked-state charging is not recorded as discharging.
- Disconnect correction: `CURRENT_NOW` can remain positive briefly after wireless charging is removed. During that period, show only `Discharging`; do not display or record previous charging watts or opposite-direction watts until negative current arrives. Clear the power-smoothing window on every connect/disconnect event to prevent old values mixing into the next state.
- Device evidence: On Fold8 Android 17, locked wireless connection showed `Charging`. Right after removal, the system reported `Wireless powered: false`, `status: 3`, `current now: +506515`; the app showed `Discharging` with no number. When `current now: -1128100` arrived, it changed to `5.6 W discharging`. USB connect later reported `USB powered: true`, `status: 2`, then `Charging`; after removal, `USB powered: false`, `status: 3`, and negative current produced normal discharge watts.
- Verification: `assembleRelease`, `lintDebug`, and 34 domain tests run directly with JUnit passed. Gradle workers still could not run every test class from the Korean path, so direct JUnit results are the evidence.
- Affected files: power-connection observer, connection-confirmation domain, state watt filter, smoothing, monitoring service, trend repository, unit tests
- Status: complete (`v0.1.18` public release). `versionCode 19`, `versionName 0.1.18`; signing certificate SHA-256: `229b6020bc74d06279d4ead5f8a00c119f3a599304a156bd392aeb4333410630`; APK SHA-256: `11DADB88750EAAC16C7890255CD92DDAA4A963C6D84F11FD38882685AA0BACE8`. The same-signed APK was installed on Fold8 and service execution was confirmed.

## 2026-08-21 - UI design baseline documented

- Category: design baseline and documentation
- Details: Fixed the roles, screen order, colors, spacing, charts, and information-density rules for the dashboard, detailed diagnostics, trend history, trend settings sheet, and external notifications in one guide. The guide states the fixed 00:00–24:00 bar-chart principle and assigns future charging/discharging peak time and battery-level information only below `Today's flow` in trend history, never to the dashboard.
- Evidence: Compared `DashboardScreen`, `DashboardControls`, `DiagnosticScreen`, `TrendScreen`, `TrendRecordingSettingsSheet`, color tokens, and existing Fold8 validation records. No app UI code or behavior changed in this documentation step.
- Affected files: `docs/05_UI_설계_기준서.md`, `docs/03_결정_및_변경기록.md`
- Status: complete; use this guide as the design/review checklist before future UI work

## 2026-08-21 - Trend graph order and directional peak records

- Category: UI improvement, feature addition, and localization
- Details: Set trend-screen order to `Battery flow → Charging power changes → Today's flow → Charging peak / Discharging peak`. Both graphs retain the fixed 00:00–24:00 bar-chart axis and time cursor. Under the daily summary, two divider-based rows show the highest recorded averaged power per direction, its time, and battery level.
- Data: A peak is the maximum stored average for the selected interval, never a raw instant reading. When a direction has no power values, show `No record / —` without estimating.
- Localization: Added peak, time/level, and no-record strings to the 11 supported languages.
- Evidence: `assembleRelease` and `lintDebug` passed. Direct JUnit execution passed all 35 tests, including directional maxima and exclusion of records with no power. A static check found all four new trend keys in every supported language. Gradle workers have the known Korean-path limitation, so the same compiled output was run directly with JUnit. Fold8 (SM-F971N) via USB ADB showed the real-data order and directional values: charging peak `10.7 W / 21:40 / 59%`, discharging peak `4.9 W / 19:10 / 60%`.
- Affected files: `TrendPeak`, `DailyTrendSummary`, `BuildDailyTrendSummary`, `TrendScreen`, trend resources, domain tests, `docs/05_UI_설계_기준서.md`
- Status: complete; included in public `v0.1.19`. Release verification records APK, GitHub Release, and Fold8 installed-version/hash alignment.

## 2026-08-21 - README update and privacy-safe trend screenshots

- Category: documentation and asset change
- Details: Updated README's opening description for the current feature set and added `v0.1.19` trend order and directional peak information to the feature list. Added actual Fold8 images for the top chart area and lower peak area.
- Privacy: Precisely cropped status-bar and bottom-navigation regions from originals so no carrier, time, status icons, or battery indicator remains in public assets. Chart data, numbers, and date-selection rows remain unmodified real records.
- Evidence: Visually checked both PNGs for removed personal areas and readable `Battery flow → Charging power changes` and `Today's flow → Charging peak → Discharging peak` sequences.
- Affected files: `README.md`, `assets/readme/trend-flow-v0119-device.png`, `assets/readme/trend-peaks-v0119-device.png`, this log
- Status: complete; no app code or APK version changed.

## 2026-08-21 - Bilingual public documentation and repository discovery

- Category: documentation and public-metadata change
- Details: README now pairs its core description, features, captions, image alt text, and build information in Korean and English. Every Korean document links to a complete English document with the same number under `docs/en/`: product plan, architecture, decision/change log, development/verification loop, and UI design guide.
- Discovery: Set the GitHub repository description to identify an Android 14+ charging/discharging battery-power monitor with ongoing notifications, a Quick Settings tile, and 30-day trend history, then add relevant repository topics. These English documents and metadata help GitHub repository search and external search engines understand the app, but neither ranking nor indexing time is guaranteed.
- Privacy: Reconfirm that existing public screenshots exclude status-bar, carrier, time, and navigation-bar areas. Do not add personal device identifiers or account information to the new documentation.
- Affected files: `README.md`, `docs/01_기획서.md` through `docs/05_UI_설계_기준서.md`, `docs/en/*`, GitHub repository description and topics
- Verification: Statically check Markdown links and public-image paths, then check README, English docs, description, and topics on the public GitHub page after merge.
- Status: complete. No app code, version, or APK asset changes.

## 2026-08-21 - Replace README screens with current Fold8 captures

- Category: documentation and asset change
- Details: Remove the three older dashboard images from README and replace them with three fresh captures from `v0.1.19` installed on Fold8: the current dashboard, battery flow with power changes, and directional peak records. The first visible screenshots now show automatic monitoring, trend-recording settings, fixed 00:00–24:00 charts, and directional peaks together, reducing confusion between the public APK and README.
- Privacy: Remove only the top status-bar and bottom navigation-bar regions from each raw capture. The public images contain no carrier, time, system status icons, battery indicator, or navigation buttons. App watt values, charts, and text are not changed.
- Affected files: `README.md`, `assets/readme/dashboard-v0119-current-device.png`, `assets/readme/trend-flow-v0119-current-device.png`, `assets/readme/trend-peaks-v0119-current-device.png`; delete the three older public dashboard images
- Verification: Confirmed `versionName 0.1.19` on an ADB-connected Fold8 and captured all three screens again. Visually check public PNGs for removed system information and readable dashboard, battery flow, power changes, and peak information. Recheck README paths and the public GitHub page after merge.
- Status: complete. No app code, version, or APK asset changes; only current Fold8 captures refresh the public README assets.

## 2026-08-22 - Full detailed battery diagnostics

- Category: feature, UI improvement, device verification, localization
- Details: Expanded detailed diagnostics into `Live measurements / Battery condition / Connection details`. Live measurements show battery power, voltage, current and average current, charge level (SOC), and last measured time. Battery condition shows temperature, system battery status, battery health (SOH), cycles, remaining charge/energy, and estimated time to full. Connection details show power source, technology, charging state, and battery presence. Entering the screen and `Refresh` read a new system sample independently of automatic monitoring.
- SOC/SOH boundary: SOC is the present charge calculated from `EXTRA_LEVEL / EXTRA_SCALE`. SOH is health relative to a new battery and has its own row. Fold8 system diagnostics contain health-related internal values, but the public Android APIs available to an ordinary app did not provide an SOH percentage. The app therefore does not estimate or use hidden system paths; it shows `Not provided by system`.
- Device evidence: Installed `v0.2.0` over the existing app on Fold8 (SM-F971N, Android 17). Detailed diagnostics showed SOC 100%, 4.49 V, current -0.17 A, average current -0.21 A, 33.5 °C, system status `Good`, SOH `Not provided by system`, cycle count 0, remaining charge 4,171 mAh, Li-ion, no power source, and discharging. Tapping `Refresh` changed the last-measured time from 05:09:07 to 05:11:45 and refreshed current and remaining-charge readings.
- Verification: `compileDebugKotlin`, `lintDebug`, and `assembleRelease` passed. The Gradle test worker hit the known Korean-path `GradleWorkerMain` class-loading failure; running the identical compiled output directly with JUnit 4.13.2 passed all 41 tests. A static check found all 36 new keys in each of the 12 locale resources, and `git diff --check` passed.
- Documentation and assets: Added two privacy-cropped real Fold8 screenshots to README. The top image shows live measurements and the SOC/SOH distinction; the bottom image shows remaining charge and connection details.
- Affected files: battery data model, Android data source, SOC calculation, diagnostic ViewModel/UI, formatter, unit tests, 12 locale resources, README, and UI guide
- Release verification: Published GitHub Release [`v0.2.0`](https://github.com/fullmetalsonic/charge-monitor/releases/tag/v0.2.0) from merge commit `af824565c6e14becdc712ad36faecf27f75e9ff4`. GitHub reports SHA-256 `8e12344d9fa6b573a8cf613078e6cba8e8024ad0fb4c1a3efa09dbf348d4f641` for the public `app-release.apk` (40,464,995 bytes), matching the local release APK. The release title and both Korean and English descriptions were also checked.
- Status: complete. `versionCode 21` / `versionName 0.2.0`; release APK SHA-256 `8E12344D9FA6B573A8CF613078E6CBA8E8024AD0FB4C1A3EFA09DBF348D4F641`; signing certificate SHA-256 `229b6020bc74d06279d4ead5f8a00c119f3a599304a156bd392aeb4333410630`. GitHub Release asset/page parity is verified.
