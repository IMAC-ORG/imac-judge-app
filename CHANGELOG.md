# Changelog

All notable changes to the AeroJudge App will be documented in this file.

---

## [Unreleased]

### Added
- **System Update Revamp**: Complete overhaul of the judge update system
  - New `/api/system/check-update` endpoint: Queries GitHub releases API to check for available updates
  - Version comparison: Shows current vs available version before updating
  - Confirmation modal: Requires user confirmation with internet connectivity warning before updating
  - Distinct exit codes: Update script now returns 0 (no update), 1 (error), or 2 (update applied)
  - `fetch_update.sh`: New lightweight fetcher script installed on devices that fetches and runs the main update script from GitHub
- **Lightweight Battery API**: New `/api/battery` endpoint for fast battery percentage checks
  - Returns only `{"percent": 85}` - single I2C read operation
  - ~100x faster than `/api/getinfo` which runs 4 expensive operations
  - Used by judge modal battery warnings for responsive UI

### Changed
- **Code Cleanup**: Removed all `REVIEWED-UNUSED` commented imports/fields (28 items across 11 files)
  - Previously tagged as unused in 2025-11, now permanently deleted
  - Git history preserves all removed code for reference
- **Update Architecture**: Single entry point for both Admin menu and SSH updates
  - Admin menu and SSH now both use `/home/judge/fetch_update.sh`
  - Main update logic (`judge_update.sh`) stays on GitHub and can be updated remotely
  - Removed fallback mirror URL (now uses GitHub exclusively)
  - `judge_setup.sh` updated to install `fetch_update.sh` instead of full update script
- **Update Feedback**: Improved user messaging throughout update process
  - "Checking for updates..." → "Already up to date (v2.0)" or shows update modal
  - "Downloading and installing update..." during installation
  - "Update applied successfully - restarting..." on completion
  - Clear error messages for network failures

### Technical Notes
- API: `GET /api/system/check-update` - Returns version comparison JSON
- API: `POST /api/system/update` - Now calls local `fetch_update.sh` script
- Script exit codes: 0=no update needed, 1=error, 2=update applied
- SSH usage: `ssh judge@device "/home/judge/fetch_update.sh"`

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
