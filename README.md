# Weight Tracker for Android

A Java Android application for recording daily weight, managing a goal, and reviewing a user's history from a local SQLite database.

## Features

- Create an account and sign in locally.
- Add, edit, and delete dated weight entries.
- Store separate histories and goals for each user.
- Display entries in a RecyclerView-based history screen.
- Request SMS permission only when alerts are enabled.
- Continue working when SMS permission is denied or message delivery fails.

## Technical design

| Area | Implementation |
|---|---|
| Language | Java 17 |
| Platform | Android, minimum SDK 24 |
| Persistence | SQLite through `SQLiteOpenHelper` |
| UI | Android views, Material Components, RecyclerView |
| Security | PBKDF2 password hashing with a per-user random salt |

All SQL operations are isolated in `DatabaseHelper`; the activities handle input, navigation, and presentation. One entry screen supports both create and edit flows, reducing duplicated UI logic.

## Source map

```text
app/src/main/
|-- AndroidManifest.xml
|-- java/com/example/weighttracker/
|   |-- MainActivity.java
|   |-- GridActivity.java
|   |-- AddWeightActivity.java
|   |-- SetGoalActivity.java
|   |-- DatabaseHelper.java
|   |-- Weight.java
|   `-- WeightAdapter.java
`-- res/layout/
    |-- activity_main.xml
    |-- activity_grid.xml
    |-- activity_add_weight.xml
    |-- activity_set_goal.xml
    `-- row_weight.xml
```

## Run it

1. Open the repository in Android Studio.
2. Allow Gradle to sync the Android dependencies.
3. Run the `app` configuration on an Android emulator or device running API 24 or newer.

SMS delivery depends on device support and permission. The rest of the application does not require SMS access.

## Testing approach

I exercised account creation and login, full weight-entry CRUD, goal updates, activity navigation, database persistence, and both outcomes of the runtime SMS-permission flow in the Android emulator.

The original submitted archive remains in `archive/`; the complete source is also exposed directly so it can be reviewed without downloading a ZIP.

---

Built for CS-360 Mobile Architecture and Programming at Southern New Hampshire University.
