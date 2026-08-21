# Charge Monitor UI design guide

> [한국어 원문 / Korean original](../05_UI_설계_기준서.md)

> Purpose: Preserve the information architecture and visual rules of the current screens as a baseline. Read this guide before adding a feature or changing UI, then decide whether the new element fits the existing flow, color, and density.

## 1. Product character

- Character: a **quiet measuring tool** for reading battery state quickly and checking history or raw values when needed.
- Priority: current state → simple control → time-based history → raw diagnostics.
- Feel: dark canvas, generous whitespace, one lime accent, and screens where numbers lead.
- Do not: turn the dashboard into a crowded collection of cards, gauge panel, or settings sheet. Add icons, badges, shadows, tabs, and filters only when they are essential to understanding the function.

## 2. Shared visual tokens

| Role | Value | Usage rule |
|---|---:|---|
| Base background | `Slate` `#071525` | Default canvas for all primary screens |
| Calm graph / empty-state surface | `SlateSurface` `#0D2035` | Use only where a grouped graph or empty state needs a surface |
| Accent | `Lime` `#C7F31B` | Charging, selection, gained percentage, and key charging data |
| Primary text | `Ink` `#F2F5F7` | Titles and current values |
| Secondary text | `Muted` `#AAB7C5` | Explanations, units, time, and supplementary data |
| Discharge / inactive chart | `GaugeTrack` `#40536A` | Discharging bars and remaining gauge track |
| Divider | `Divider` `#30445A` | Between setting rows and information groups |

### Typography and spacing

- Large current power: `64sp` number plus `28sp` unit, used only in the main gauge.
- State, complete, or confirmation text: no larger than `42sp`, and large only when a numeric value is unavailable.
- Screen title: `headlineMedium`; section title: `headlineSmall`.
- Row title: `titleMedium`; explanation: `bodyMedium`; chart axes and auxiliary data: `bodySmall`.
- Default portrait outer padding is `28dp` left/right and `16dp` top/bottom. Always account for safe areas.
- Use roughly `22–28dp` between information groups and `12–18dp` inside a row. Reuse existing spacing before introducing new values.

## 3. Screen flow and responsibilities

```text
Main dashboard
  ├─ Automatic monitoring / trend-recording controls
  ├─ Trend-recording settings sheet
  ├─ Trend-history screen
  │    ├─ Date selection
  │    ├─ Battery-flow bar chart
  │    ├─ Charging-power-change bar chart
  │    └─ Daily summary and charging/discharging peak records
  └─ Detailed diagnostic screen
```

No screen takes another screen's job. The dashboard focuses on live values and controls, trend history on comparison over time, and detailed diagnostics on raw measurements.

## 4. Main dashboard

### Portrait

Keep this order fixed:

1. Battery/lightning icon and circular gauge
2. Current watts or state message
3. State message: charging, discharging, complete, and so on
4. One-line summary: `battery % · voltage · current`
5. Divider
6. Automatic-monitoring setting row
7. Divider
8. Trend-recording setting row and recording-interval entry text
9. Divider
10. Navigation rows: `Trend history ›`, `Detailed diagnostics ›`

Do not put historical peaks, time-based data, or chart summaries on the dashboard. Those belong to trend history.

### Landscape / unfolded

- Left: gauge and one-line raw-value summary.
- Right: setting rows and navigation rows.
- Both columns have equal importance so controls do not collect at the bottom in wide layouts.

## 5. Detailed diagnostics

- Purpose: quickly confirm the evidence behind a displayed value and the current battery condition.
- Order: `Live measurements` (battery power, voltage, current/average current, charge level (SOC), last measured) → `Battery condition` (temperature, system status, health (SOH), cycles, remaining charge/energy, time to full) → `Connection details` (power source, technology, charging status, presence) → disclaimer.
- Clearly separate SOC (current charge) from SOH (health relative to a new battery). Do not estimate SOH; show `Not provided by system` when Android does not publish a value.
- Entering the screen or tapping `Refresh` reads a new system sample independently of automatic monitoring.
- Each item is one `left label / right value` row with only one divider between rows. The screen may scroll vertically when required.
- Do not add charts, toggles, or extra explanation cards. This screen is a measurement table.

## 6. Trend history

### Screen order

1. `Trend history` title
2. Recent-date row: tap selection, left/right swipe, and `Today` return when viewing a past date
3. `Battery flow`
4. Battery-level bars on a fixed 00:00–24:00 axis
5. Time-cursor information fixed by chart touch or horizontal drag
6. `Charging power changes`
7. Charging/discharging-power bars on a fixed 00:00–24:00 axis
8. Recording-interval average note
9. Four-item `Today's flow` summary
10. Divider-based `Charging peak` and `Discharging peak` record rows

### Chart rules

- Do not connect inferred lines. Draw vertical bars only at recorded times.
- The full day always spans `00 · 06 · 12 · 18 · 24`; never stretch sparse samples to fill the width.
- Charging bars use lime; discharging bars use `GaugeTrack`.
- A selected cursor uses a lime vertical line and dot at the same time in both charts.
- Do not estimate blank periods; show them as `No record`.
- When the selected interval is 1 or 5 minutes, do not force interpolation into older data captured at another interval.

### Today's flow summary

- Default items: `charge sessions / gained % / discharged % / power peak`.
- Keep a one-row, four-column layout: label above, compact value below.
- Use lime only for gained percentage and charging-related emphasis. Use white or secondary text for discharge.

### Peak-record rules

- Keep charging peak, discharging peak, their time, and battery level **only on the trend-history screen**.
- Place them directly under the four-column `Today's flow` summary, below both charts.
- Do not duplicate them on the dashboard or detailed diagnostics.
- Limit treatment to the existing two divider-based information rows or a two-column band. Do not create extra cards, icons, or charts.
- Per direction, show only `peak watts`, `HH:mm`, and `battery %`. If no peak exists, use `—` consistently and omit time and level.
- A peak is the maximum average for the selected recording interval, not an instantaneous spike; retain the same meaning as the chart's interval-average note.

## 7. Settings sheet

- Select the trend-recording interval only in the bottom sheet opened from the dashboard.
- The sheet contains only a title, standard 5-minute average, and precise 1-minute average.
- Each option has a radio button, title, and one-line explanation.
- Do not add a Save button. Apply immediately and close after selection.

## 8. External display areas

- Ongoing notification: the title is the app name; body text is current watts plus a short charging/discharging state.
- Status bar: use only a monochrome small plug silhouette. Do not add numbers, a complex battery outline, or a large icon.
- AOD: the app does not create a separate widget; it only relies on title/content of the ongoing notification that the system chooses to show.

## 9. Questions before changing UI

1. Which screen owns this information: dashboard, trend history, or detailed diagnostics?
2. Is extending an existing divider, row, or chart enough?
3. Is a new card, icon, tab, or color truly necessary?
4. Do spacing and line breaks remain sound on folded portrait, unfolded/landscape Fold layouts, and long translations?
5. If this is recorded data, can the user understand the timeline, recording interval, and averaging?

## 10. Prompt for future work

```text
Read Charge Monitor's UI guide at docs/05_UI_설계_기준서.md first.
Keep the current roles and order of screens. Design new features as the smallest extension of existing rows, dividers, and chart structure.
Use Slate #071525 as the base, Lime #C7F31B for emphasis, Muted #AAB7C5 for secondary text, and Divider #30445A for dividers.
Keep live watts and controls on the dashboard, time-based records and peaks in trend history, and only raw values in detailed diagnostics.
The trend screen order is battery flow → charging power changes → today's flow → charging/discharging peak records. Charts are bar charts on a fixed 00:00–24:00 axis. Never invent lines, interpolation, or estimates for missing records.
Do not add cards, icons, tabs, or filters without a clear need. Check for overlap on folded and unfolded Fold layouts and in every supported language.
```

## 11. Guide validation scope

- This guide is based on the current Compose implementation and existing Fold8 screen-validation records.
- Recheck visual fidelity on Fold8 portrait and landscape/unfolded screens with the next UI-related change.
- This guide changes no UI code; it is the design and review baseline for later work.
