# Changelog

All notable changes to the AeroJudge App will be documented in this file.

---

## [2.0] - 2025-11-27

### Added

#### Core Features

- **Per-Type Round Numbering**: Sequential numbering by round type (e.g., Known 3, Unknown 1, Freestyle 1)
  - Updated XML output to Score to reflect per-type numbering
  - Round number used for `sequences.dat` lookup and folder resolution [Issue 70]
  - Supports multiple known/unknown sequences in one competition
  - Score sequences.dat controls std vs alt sequence option
- **Incomplete KNOWN Round Warning**: Detects when pilot completed Seq 1 but not Seq 2
  - Triggers when: 2-sequence competition AND pilot at sequence 2 AND KNOWN button clicked
  - Options: Continue Seq 2 | Start New Round | Exit for Review
  - API: `POST /api/pilot/{pilotId}/advance-round?type=KNOWN`
- **Freestyle Support**: Added Freestyle as valid round type [Issue 103]
  - Complete scoring functionality implemented and tested
  - Output includes `duration_seconds` (default 240, editable in Score)
  - Restored Freestyle button on judge popup
- **Sequence Validation with Error Handling** [Issue 66]
  - Added error checking for new/update contest and pilot sync [Issue 66]
  - Validates sequence availability with graceful fallbacks (no white screen crashes)
  - Always-Show Validation Page after sync with three states:
    - Success (green): "All Sequences Validated!" with Continue button
    - Warnings (orange): Warning list with Re-Sync + Continue buttons
    - Errors (red): Error list with Re-Sync + Continue Anyway buttons
  - Auto-advances after 10 seconds if not manually confirmed
- **Score Mismatch Resolution:** Detect and fix scores assigned to wrong pilots
  - Scans for mismatches between judge device and Score server
- **Unknown Figures sync from Score** Ability to pull unknown figures package from Score (4.71 or newer required)

#### Admin Pages & Settings

- **Admin Pages Redesign**: Compact layout with header + nav on single line for mobile optimization
  - **`/admin/comp`** - Scorekeeper Admin: Known Sequence Count, Default Sequence Type, Refresh/Load Event
  - **`/admin/device`** - Device Settings: Scoring Mode, Device Identity (Line/Judge), System Update
  - **`/admin/scores`** - Score Admin: Score Mismatch Resolution with detection and fixing UI at `/admin/scores/resolve`
  - **`/newcomp`** - First Boot Wizard: 3-step setup (Event Settings → Device Identity → Load Event)
- **System Update Revamp**: Complete overhaul of the judge update system
  - New `/api/system/check-update` endpoint: Queries GitHub releases API to check for available updates
  - Version comparison: Shows current vs available version before updating
  - Confirmation modal: Requires user confirmation with internet connectivity warning before updating
  - Distinct exit codes: Update script returns 0 (no update), 1 (error), or 2 (update applied)
  - `fetch_update.sh`: New lightweight fetcher script installed on devices that fetches and runs main update script from GitHub
- **Lightweight Battery API**: New `/api/battery` endpoint for fast battery percentage checks
  - Returns only `{"percent": 85}` - single I2C read operation
- **Local Settings API**: New `POST /api/comp/local` endpoint for updating settings without Score server contact
  - Used by judge modal battery warnings for responsive UI
- **Configuration Settings**: Added configuration options
  - `score_poll_timeout` (default 2s) and `score_timeout` (default 10s) for Score communication timeouts
  - `language` setting added for future ability to change device language (prompts, audio, etc.)

#### UI Improvements

- **Judge Popup**: Always shows all three round type buttons; availability based on `sequences.dat`
- **Contest Setup**: Added confirmation prompt to prevent accidental edits [Issue 62 & 70]
- **Judging Page Display**:
  - KNOWN: "Round X Seq Y Dir Z"
  - UNKNOWN: "Round X Dir Z"
  - FREESTYLE: "Round X" (figure numbers hidden, shows description only)
- **FREESTYLE Filter**: Added FREESTYLE option to pilot class filters (appears when any pilot has freestyle=true)
- **Pilot Next Round Cards**: Minimal underline style (transparent background, bottom border only)
- **Score Summary**: Dynamic modal supports varied figure counts
- **Audio**: Removed hard-coded volume limits; caller button stops current audio when pressed [Issue 69]
- **Instructions Audio**: Numpad 0 plays `instructions.mp3` on pilot list pages
- **Battery Alerts**: Orange warning at <30%, red warning at <20% on judge popup (uses Materialize onOpenStart callback for reliable timing)
- **Info Icon**: Enlarged to 2.2rem for better visibility
- **Status Popup**: Auto-closes when carousel is rotated

#### Error Handling & Stability

- **Global Error Page (`error.html`)**: Catches unhandled 500 errors with user-friendly display
  - Red warning: "Please give this device to the Contest Director or Scorekeeper"
  - Admin button with confirmation modal (same warning as pilot list pages)
  - Prevents judges from being stuck on white screen errors
- **Modal Cleanup on Navigation**: All open modals (except summary) are now properly closed when navigating carousel
- **Summary Modal Lock**: Carousel navigation blocked while score summary modal is open
- **Code Refactoring**: Navigation logic consolidated into `carouselPrev()` and `carouselNext()` functions
  - Applies to: `judge.html`, `pilot-list-global.html`, `pilot-list-round.html`

### Changed

- **newcomp.html**: Complete redesign as 3-step first boot wizard (Event Settings → Device Identity → Load Event)
- **Admin navigation**: Links changed from /newcomp to /admin/comp throughout app
- **adminComp.html**: Labels updated ("Known Sequences" → "Known Sequence Count", "Fallback Type" → "Default Sequence Type")
- **adminDevice.html**: Now preloads current Line/Judge numbers from settings; compact warning box
- **Confirmation modals**: Condensed to single-line descriptions (no scrolling required)
- **Battery warning**: Threshold changed from 40% to 30%
- **Folder Structure**: Flattened `figures/en/` structure and created 2026 sequence structure [Issue 94]
- **Audio folder structure**: Updated for new organization
- **Update Architecture**: Single entry point for both Admin menu and SSH updates
  - Admin menu and SSH now both use `/home/judge/fetch_update.sh`
  - Main update logic (`judge_update.sh`) stays on GitHub and can be updated remotely
  - `judge_setup.sh` updated to install `fetch_update.sh` instead of full update script
- **Pilot sync**: Renamed and now properly refreshes sequence cache

### Fixed

- **Admin page 404 errors**: Fixed resource paths (relative → absolute) for nested routes like /admin/comp
- **Sequence validation**: Fixed error checking for new/update contest and pilot sync [Issue 66]
  - Graceful fallbacks eliminate white screen crashes

### Technical Notes

- All admin pages use consistent compact styling for device screens
- API: `GET /api/system/check-update` - Returns version comparison JSON
- API: `POST /api/system/update` - Calls local `fetch_update.sh` script
- API: `POST /api/comp/local` - Updates settings without Score contact
- API: `GET /api/battery` - Returns battery percentage
- API: `GET /api/scores/mismatches` - Scans for score mismatches
- API: `POST /api/scores/resolve` - Fixes mismatched scores
- Project targets Java 17 (compatible with Java 17-21)
- All deprecated API warnings resolved
- Removed unused imports
