# Changelog

All notable changes to the AeroJudge App will be documented in this file.

---

## [2.0] - 2025-11-27

### Added
- **Admin Pages Redesign**: Compact layout with header + nav on single line for mobile optimization
- **Local Settings API**: New `POST /api/comp/local` endpoint for updating settings without Score server contact
- **FREESTYLE Filter**: Added FREESTYLE option to pilot class filters (appears when any pilot has freestyle=true)
- **Configurable Timeouts**: `score_poll_timeout` and `score_timeout` settings for Score server communication
- **Auto-advance**: Sequence validation page auto-advances after 10 seconds if not manually confirmed

### Changed
- **newcomp.html**: Complete redesign as 3-step first boot wizard (Event Settings → Device Identity → Load Event)
- **Admin navigation**: Links changed from /newcomp to /admin/comp throughout app
- **adminComp.html**: Labels updated ("Known Sequences" → "Known Sequence Count", "Fallback Type" → "Default Sequence Type")
- **adminDevice.html**: Now preloads current Line/Judge numbers from settings; compact warning box
- **Confirmation modals**: Condensed to single-line descriptions (no scrolling required)
- **Battery warning**: Threshold changed from 40% to 30%
- **Info icon**: Enlarged to 2.2rem for better visibility
- **Status popup**: Auto-closes when carousel is rotated

### Fixed
- **Admin page 404 errors**: Fixed resource paths (relative → absolute) for nested routes like /admin/comp
- **Device identity preload**: adminDevice page now correctly loads current line/judge values

### Technical Notes
- Version bumped to 2.0
- All admin pages use consistent compact styling for device screens

---

## [1.2] - 2025-11-26

### Added
- Freestyle caller audio files (call1-5.mp3)
- Favicon for browser tab
- CHANGELOG.md for tracking changes

### Changed
- Updated freestyle figure audio files (fig1-5.mp3)
- Updated instructions audio

### Fixed
- **Code Cleanup**: Fixed all deprecated API warnings
  - Replaced `new URL(String)` with `URI.create(String).toURL()` (Java 20+ compliant)
  - Replaced `Runtime.exec(String)` with `ProcessBuilder` in InfoCollectorService
- Commented out unused imports with `REVIEWED-UNUSED 2025-11 DPG` tags for traceability
- Commented out unused fields (SettingUtils.logger, RootController.sequenceService)

### Technical Notes
- Project targets Java 17 (compatible with Java 17-21)
- All deprecated API warnings resolved
- Build produces clean compile with no warnings

---

## [1.1] - Previous Release

### Added
- Per-type round numbering (KNOWN, UNKNOWN, FREESTYLE each have independent counters)
- Admin pages for competition and device settings
- Freestyle figure display improvements

### Fixed
- Battery warning timing and validation flow
- Incomplete round modal and UI improvements
- Pilot selection and judging page UI refinements
