# Development and verification loop

> [한국어 원문 / Korean original](../04_개발_및_검증_루프.md)

This project uses Graph Engineering. Here, a graph is not a screen graphic: it is a way to manage each feature by connecting the states **requirement → design → implementation → verification → review → complete**.

## Core loop

```text
Confirm requirement
    ↓
Design impact scope and file boundaries
    ↓
Implement the smallest viable change
    ↓
Run real build, tests, and device validation
    ↓
Independent review
    ├─ Pass → record completion
    └─ Issue found → reproduce → analyze cause → minimal fix → re-verify and re-review
```

## Feature management units

Manage each feature as a connection between its implementation and verification evidence.

| Feature node | Verification node | Completion evidence |
|---|---|---|
| Read voltage/current | Check Fold8 raw readings | Logcat or diagnostic-screen record for charging and unplugged states |
| Calculate watts | Unit-calculation test | Unit test matches expected watts for µA and mV inputs |
| Stabilize values | Fluctuation-input test | Short spikes do not excessively move the displayed value |
| Ongoing notification | Android 14+ runtime check | Results for notification permission allowed/denied and charger connect/disconnect |
| Automatic monitoring | Background and battery-saver check | Real-device result updated after charger connection without an extra button |
| AOD notification | Check each Fold8 AOD setting | Photo or record distinguishing icon-only from content-visible states |
| Fold UI | Check folded and unfolded layouts | Screenshots with no overlap or clipping at both widths |

## Completion criteria

Record a feature as complete only when every condition below is met.

- The requirement and affected files are clearly linked in the documentation.
- Build, unit tests, and device tests when needed were actually run.
- Any unrun validation is marked `Not verified`, never treated as PASS.
- Requirement coverage, regression risk, and omissions were reviewed separately.
- There are zero Critical or High severity issues.

## Change limits

- Make a small wording or typo fix after an appropriate check.
- Before changing several modules or a behavioral flow, show the user the scope and plan and obtain approval.
- Limit speculative fixes for the same failure to about five attempts. If the failure repeats, summarize symptoms, reproduction, confirmed facts, attempts, and next options with the user.

## Change-log rule

Add important loop results to [the decision and change log](03_decision_and_change_log.md). Record at least:

- Feature name and requirement
- Changed files
- Validation performed and actual result
- Unverified items and why
- Review result and discovered issues
- Next step or deferred decision
