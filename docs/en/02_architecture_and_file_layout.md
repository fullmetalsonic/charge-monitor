# Architecture and file layout

> [한국어 원문 / Korean original](../02_구조_및_파일분배.md)

## Principles

- Keep measurement, calculation, service lifecycle, notifications, settings storage, and UI separate.
- An Activity or Compose UI must not read battery APIs directly or build notifications.
- Centralize Android system-API access in the `platform` layer so device-specific issues can be diagnosed in one place.
- The following tree is the intended file design; it began as a plan before Kotlin implementation and remains the boundary reference.

## Planned file tree

```text
Charge Monitor/
├─ README.md
├─ docs/
│  ├─ 01_기획서.md
│  ├─ 02_구조_및_파일분배.md
│  ├─ 03_결정_및_변경기록.md
│  └─ 04_개발_및_검증_루프.md
├─ gradle/wrapper/
├─ gradlew.bat
├─ build.gradle.kts
├─ settings.gradle.kts
└─ app/
   └─ src/main/
      ├─ AndroidManifest.xml
      └─ java/com/example/chargemonitor/
         ├─ app/
         ├─ data/model/
         ├─ data/repository/
         ├─ domain/
         ├─ platform/battery/
         ├─ platform/notification/
         ├─ platform/system/
         ├─ service/
         ├─ ui/design/
         ├─ ui/dashboard/
         ├─ ui/diagnostic/
         └─ util/
```

The actual Kotlin package is `com.chargemonitor`; see the Korean tree for the original planned filename-level map.

## Responsibilities by module

| Area | Responsibility | Excludes |
|---|---|---|
| `data/model` | Pure voltage, current, watts, and charging-state data | Android API calls |
| `domain` | Unit conversion, watt calculation, value stabilization, state decisions, measurement/notification cadence | Notification creation and UI rendering |
| `platform/battery` | `BatteryManager` and battery Intent access | UI and settings decisions |
| `platform/notification` | Notification channels and standby/charging notification composition | Measurement cadence |
| `platform/system` | System events such as charger connection, disconnection, and boot | Service calculations |
| `service` | Automatic monitoring lifecycle, periodic sampling, service start/stop | UI code |
| `data/repository` | Shared measurement state and settings persistence for service and UI | Android screen composition |
| `ui` | Dashboard, settings, and diagnostic presentation | Direct battery API access |
| `ui/design` | Color tokens, battery/lightning icons, shared theme | Charging-power calculation |
| `util` | Small shared functions such as unit, time, and message formatting | Business policy |

## Data flow

```text
Android BatteryManager / battery-status Intent
        ↓
AndroidBatteryDataSource
        ↓
ObserveChargingState → CalculateChargingPower → StabilizePowerReading
        ↓
ChargeMonitorRepository
        ├─ ChargeMonitoringService → ChargeNotificationFactory → status bar / AOD notification
        └─ DashboardViewModel / DiagnosticViewModel → app UI
```

## Automatic-monitoring boundaries

- When the user enables automatic monitoring, `MonitoringServiceController` manages the conditions for starting the service.
- A charger-event receiver helps update state quickly while the service is ready.
- Because recent Android versions restrict starting long-running background services from only an event receiver, charger events are not the primary long-term reliability mechanism.
- Reboot recovery remains a separate decision after reviewing distribution method, Android version, and Fold8 measurements.
- Confirm service type and manifest permissions against current Android requirements immediately before implementation/distribution.

## Test-file layout

```text
app/src/test/.../
├─ domain/CalculateChargingPowerTest.kt
├─ domain/StabilizePowerReadingTest.kt
└─ util/PowerFormatterTest.kt

app/src/androidTest/.../
├─ platform/battery/AndroidBatteryDataSourceTest.kt
├─ platform/notification/ChargeNotificationTest.kt
└─ service/ChargeMonitoringServiceTest.kt
```

Verify calculations and stabilization in ordinary JVM tests. BatteryManager, notification, AOD, and fold-layout behavior require a real Fold8 device.

## Applying Graph Engineering

Do not treat a feature as complete in one step. Connect each feature through requirement, design, implementation, verification, review, and completion states, and retain evidence when advancing. The detailed workflow and record format live in [the development and verification loop](04_development_and_verification_loop.md).
