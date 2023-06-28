# TODO List

1. If we set the comp to 3 rounds total and some pilots have completed 3 rounds and we decide to extend it to 4 rounds, then those pilots who have already completed 3 their status does not change from completed and we can't judge their round 4.
	2 options here.
	- We fix it or
    - We just remove the option totally for number of rounds and make it open ended. PI-Score does not care and we can sync the scores to Score at any point.
	<br>  
2. Pilots are currently ordered by Name, I'd like to have them ordered by Comp_ID, the least completed rounds. Right now every time you complete scoring a Pilots round the system defaults back to the first pilot by name and you need to scroll through the the next Pilot, ordering by Comp_ID allows us to the have the order of the pilots predefined based on the Comp_ID, and the secondary by least completed round, this will mean once you have finished scoring a pilot it will default to the next pilot in the queue. We will still have the ability to change scroll through the pilots and select another pilot if we have to skip a pilot due to technical reasons.
	<br>  
3. Review Page, This is more for when we are doing Pilot Judge Training, we should allow for this feature to be enable in settings, so that post a pilots round the instructor judge can as the pilot judges to open/review the score allowing them to discuss. The review would be on the Pi's 9 Making them editable or not is just something we need to think about, for now just the ability to see all the last rounds scores would be great.
	<br>  
4. MultiChannel Audio - Currently we call audio files (wav) for the scores and figures, we are using a single audio channel and so any button press overwrites the current audio and starts the new audio. We want to have the audio of the full maneuver and the option to pause, eg.  ( Double Humpty Bump. Push to vertical upline, 4 of 8 point roll on upline, pull 1/2 inside loop to vertical downline, 1 1/4 positive snap on downline, pull 1/2 inside loop to vertical upline, 1 full roll on upline, pull to exit inverted.)  On Activation the audio should start reading this description, with the same activation button acting as a pause. All other buttons should not have an effect other than say ( Break, Zero, N/O) . We are using WAV files because TTS, is terrible it's very robotic, and a little slow to initialize Chromium that comes with the rPI is compiled for ARM does not have the full google TTS API
	<br>  
5. Move the WAV and SVG's to the SD out of the APP, this will allow for customization of the files and ability to support unknows better.
	<br>  
6. Some better error handling for when we can't connect to Score
	<br>  
7. Auto discovery of Score, right now we are hardcoding it,
	<br>  
8.  Add button options in place of the touch screen (start, sync, pilot refresh), this will option the ability to use potentially E-Ink screens.
	<br>  
9.  Special char handleing.
	<br>  
10. Bulk audio generation.
