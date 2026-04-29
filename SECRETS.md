## Local secrets

This project expects Firebase config to stay local and out of git.

Use this path for your downloaded Firebase config:

- `app/google-services.json`

Do not commit these files:

- `app/google-services.json`
- `app/src/google-services.json`
- `local.properties`

If a Firebase config was pushed to GitHub by mistake:

1. Remove the tracked file from git and commit that removal.
2. Download a fresh `google-services.json` from Firebase if you rotate or reconfigure the app.
3. Restrict or rotate the related Google API key in Google Cloud Console if needed.
4. Rewrite git history before force-pushing if the file already exists in older commits.
