# TODO List

## High Priority (New - 2025-11)

### 1. Create Admin Pages (replace newcomp)
- Build proper admin interface to replace the current newcomp page
- Consolidate competition setup and device configuration

### 2. Refactor Package Names
- Replace all `co.za.imac` references with `imac`
- Update package declarations, imports, and any hardcoded references

### 3. Code Cleanup Review
- Review all `REVIEWED-UNUSED` commented imports - decide to keep or delete
- Review all `BACKLOG` comments (e.g., PilotService:225 "update existing score")
- Review `@Deprecated` methods (e.g., ScheduleService.getAllSequences_old())
- Clean up commented-out code blocks throughout codebase

---

## Technical Debt

- [ ] Remove or clean up commented imports (currently tagged REVIEWED-UNUSED)
- [ ] Remove deprecated `getAllSequences_old()` method after confirming unused
- [ ] Review try-catch blocks around logger calls (redundant in some places)
- [ ] Consider try-with-resources for FileOutputStream in CompService

---

## Completed (v1.2)

- [x] Fix deprecated `new URL()` → `URI.create().toURL()`
- [x] Fix deprecated `Runtime.exec()` → `ProcessBuilder`
- [x] Add freestyle caller audio files
- [x] Update figure audio files
- [x] Per-type round numbering (KNOWN, UNKNOWN, FREESTYLE independent)
- [x] Add Java 17+ requirement to README
- [x] Create CHANGELOG.md

---

## Legacy Items (Need Review)

*The following items were in the original TODO.md and need to be reviewed for current relevance:*

### 1. Round Extension Issue
If we set the comp to 3 rounds total and some pilots have completed 3 rounds and we decide to extend it to 4 rounds, then those pilots who have already completed 3 their status does not change from completed and we can't judge their round 4.
- Option A: Fix the status update logic
- Option B: Remove max rounds option entirely - make it open ended. PI-Score does not care and we can sync scores to Score at any point.

### 2. Pilot Ordering
Pilots are currently ordered by Name, I'd like to have them ordered by Comp_ID, the least completed rounds. Right now every time you complete scoring a Pilots round the system defaults back to the first pilot by name and you need to scroll through the the next Pilot, ordering by Comp_ID allows us to the have the order of the pilots predefined based on the Comp_ID, and the secondary by least completed round, this will mean once you have finished scoring a pilot it will default to the next pilot in the queue. We will still have the ability to change scroll through the pilots and select another pilot if we have to skip a pilot due to technical reasons.

### 3. Review Page (Judge Training)
This is more for when we are doing Pilot Judge Training, we should allow for this feature to be enable in settings, so that post a pilots round the instructor judge can as the pilot judges to open/review the score allowing them to discuss. The review would be on the Pi's. Making them editable or not is just something we need to think about, for now just the ability to see all the last rounds scores would be great.

### 4. MultiChannel Audio
Currently we call audio files (wav) for the scores and figures, we are using a single audio channel and so any button press overwrites the current audio and starts the new audio. We want to have the audio of the full maneuver and the option to pause, eg. (Double Humpty Bump. Push to vertical upline, 4 of 8 point roll on upline, pull 1/2 inside loop to vertical downline, 1 1/4 positive snap on downline, pull 1/2 inside loop to vertical upline, 1 full roll on upline, pull to exit inverted.) On Activation the audio should start reading this description, with the same activation button acting as a pause. All other buttons should not have an effect other than say (Break, Zero, N/O). We are using WAV files because TTS is terrible - very robotic, and a little slow to initialize. Chromium that comes with the rPI is compiled for ARM does not have the full google TTS API.

### 5. Move WAV and SVGs to SD
Move the WAV and SVG's to the SD out of the APP, this will allow for customization of the files and ability to support unknowns better.

### 6. Error Handling
Some better error handling for when we can't connect to Score.

### 7. Auto Discovery of Score
Right now we are hardcoding it.

### 8. Hardware Button Options
Add button options in place of the touch screen (start, sync, pilot refresh), this will open the ability to use potentially E-Ink screens.

### 9. Special Character Handling
Special char handling needs review.

### 10. Bulk Audio Generation
Tool/script to generate audio files in bulk.
