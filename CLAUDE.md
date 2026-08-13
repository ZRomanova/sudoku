# Project rules

These rules apply by default unless explicitly told otherwise for a specific task.

1. **After implementing each new feature**: rebuild the project, install the new build on the user's connected phone via adb (`adb install -r`), then commit and push the result.
2. **Do not test via screenshots of the user's phone.** Instead, after fixing a bug or implementing a new feature, send the user a list of what to check manually.
