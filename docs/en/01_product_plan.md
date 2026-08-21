# Charge Monitor product plan

> [한국어 원문 / Korean original](../01_기획서.md)

## 1. Product goal

When a charger is connected to a Galaxy Z Fold8, the app estimates battery charging power without requiring the user to press Start every time and displays it in a status-bar notification. If the device's AOD notification settings allow it, the charging-watt text from that same notification may also appear on the Always On Display.

The precise name of the displayed value is **estimated battery charging power**. It can differ from the input power supplied by the charger or cable because phone consumption and charging-circuit loss are included.

## 2. Target environment

| Item | Standard |
|---|---|
| Primary validation device | Samsung Galaxy Z Fold8 |
| Platform | Android / Kotlin |
| Minimum supported version | Android 14 (API 34) |
| Target compatibility | Latest stable Android version available at implementation time |
| Default display | Ongoing status-bar notification and notification shade |
| AOD visibility | Within what Samsung AOD notification settings allow |
| Measurements | Battery voltage, net battery current, calculated watts |

## 3. Core user flow

1. After installation, the user turns on `Automatic charging monitoring` once.
2. The app remains ready to observe charging state.
3. When a charger is connected and stabilized power is at least 1 W, the notification title or content updates to text such as `Charging at 18.6 W`.
4. When the screen is off, this notification text may appear on AOD if Samsung's AOD notification-content setting is enabled.
5. When charging ends or power drops below 1 W, numeric charging display stops and the app returns to standby.
6. The user can turn automatic monitoring off or inspect raw measurements on the diagnostic screen.

## 4. Scope

### 4.1 Included in the first version

- Read voltage and current through `BatteryManager` and battery-status information.
- Convert units and calculate estimated battery charging power.
- Measure every two seconds by default and stabilize the value.
- Update an ongoing notification with charging-power text at 1 W or above.
- Provide an automatic-monitoring on/off setting.
- Provide a diagnostic screen with voltage, current, calculated value, charging state, and last-update time.
- Explain notification/AOD visibility conditions and Samsung settings.
- Use a modern visual system centered on a simple battery outline and lightning bolt.

### 4.2 Excluded from the first version

- Direct widgets or text inside Samsung's stock AOD clock or layout.
- Guaranteed charger/cable input watts, PPS contract values, or USB-PD protocol values.
- Forcing the screen on or covering the lock screen with a black Activity.
- Faster charging, thermal control, or charge-limit control.
- Identical readings guaranteed across all manufacturers.

## 5. Display and battery policy

### While charging

- Condition: charging with stabilized power of at least 1 W.
- Notification: a short message such as `Battery charging power 18.6 W`.
- Sound, vibration, and pop-up: not used.
- Update: every two seconds by default; avoid needless notification reposts when values barely change.

### When not charging

- Do not show a charging-watt message.
- Use `Charge Monitor` as the standby notification text. Use a small battery-and-lightning icon with no sound, vibration, or pop-up.
- Reliable automatic detection may require a quiet foreground-service standby notification or an equivalent user-visible state.
- Removing the standby notification entirely can make automatic detection of the next charger connection less reliable because the service can lose foreground status.

### AOD

- The app only provides a standard Android notification.
- Whether notification content is actually visible on AOD depends on Fold8 One UI version, lock-screen notification settings, AOD settings, and the user's privacy-display choice.
- AOD is therefore a supported visibility path, but the app does not guarantee a large, always-visible number there.

## 6. Screen and design direction

- The primary icon is a rounded battery outline with a lightning bolt. Avoid complex gauges, 3D effects, and excessive decoration.
- Use neutral colors with one vivid accent. Charging uses a modern green family; unavailable/caution states use yellow or orange.
- Prioritize large numbers and generous whitespace. The first dashboard screen shows only current watts, charging state, and a simple battery graphic.
- Keep settings and diagnostics on separate screens so the dashboard never becomes an old-fashioned control panel.
- Consider both narrow folded and wide unfolded Fold layouts, and validate both widths during implementation.

## 7. Technical assumptions and risks

| Item | Handling principle |
|---|---|
| Current unit | Treat Android API values as microamperes (µA); do not apply arbitrary 1,000x scaling guesses. |
| Unsupported measurement | Show `Measurement unavailable` when data is missing or abnormal; do not assume it is 0 W. |
| Fluctuation | Stabilize the displayed value with a short moving average or median rather than a single instant sample. |
| Automatic execution | Validate Android version-specific background and foreground-service restrictions on a real Fold8. |
| App stopped | The user can stop the app in system settings, and manufacturer power policy can stop the service; explain recovery conditions clearly. |
| Distribution | Recheck current Android 14+ foreground-service requirements and Play policies immediately before distribution. |

## 8. Acceptance criteria

- With automatic monitoring enabled, connecting a charger updates notification data without another user action.
- While charging, voltage, current, and calculated watts agree between the diagnostic screen and notification.
- Disconnecting the charger removes charging-watt text and returns the app to standby.
- If notifications are not allowed or current measurement is unsupported, the app explains the cause and next step.
- Validate charging, fast charging, screen on/off, locked, AOD, and charger-disconnected states on a Fold8.
- Record AOD outcomes as `shown depending on device settings`, never as a guaranteed feature.

## 9. Items to decide before implementation

- Play Store distribution versus personal-install APK distribution.
- Whether automatic monitoring resumes after a reboot.
- Whether users can adjust the 1 W threshold.
- Fold8 One UI version and results from real AOD settings screens.
