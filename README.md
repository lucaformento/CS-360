# CS-360 Mobile Architecture and Programming: Weight Tracking App

## Project Overview

This repository contains my final project for CS-360, a fully functional
Android weight-tracking app built in Java with a local SQLite database.

## Reflection

**App requirements, goals, and user needs**

The goal of this app was to give a user a fast, low-friction way to log their
weight each day, see that history over time, and get notified when they reach a
goal weight they set. The whole thing was built around the idea that daily
logging has to take only a few seconds, because if it feels like a chore, people
stop doing it. It was designed to address three types of users: someone actively
losing weight toward a target, someone maintaining their weight, and someone
tracking for a health reason. All three needed a simple daily log, a clear
history, and a payoff when a goal is reached.

**Screens, features, and user-centered design**

The app uses four screens: a login screen that also creates an account, a home
screen built around a grid of every weight entry, an add/edit screen for daily
entries, and a set-goal screen. I kept users in mind by keeping the interface
simple and the navigation shallow, so the most common action, adding a weight,
is always one tap away through a floating action button placed in the natural
thumb zone. I followed the Android design guidelines for visual hierarchy,
consistent color, and large touch targets. The designs were successful because
they matched how people actually use the app: quick, one-handed, a few seconds
at a time.

**Coding approach and strategies**

My main strategy was separating concerns. I kept all the database work in a
single DatabaseHelper class and left the activities thin, so each screen only
reads input and displays results rather than touching SQL directly. I also
reused one screen for both adding and editing entries to keep the code
consistent. These techniques, keeping classes small and isolating logic in one
place, are things I can apply to any future project, because they make code far
easier to read, debug, and extend.

**Testing for functionality**

I tested using the Android Emulator, checking each feature as I built it:
creating an account, logging in, adding and editing and deleting weights, setting
a goal, and both granting and denying the SMS permission. Testing incrementally
mattered because it let me catch small issues while they were still isolated,
before they could turn into bigger tangled problems. It also confirmed the most
important requirement, that the app keeps working normally even if the user
denies the SMS permission.

**Where I had to innovate**

The trickiest part was handling the SMS notification cleanly. The app had to
request permission, send an alert when the goal was reached if permission was
granted, and continue working normally if it was denied, all without crashing. I
solved this by checking the permission at the moment it was relevant, wrapping
the SMS send in error handling, and falling back to an on-screen message so the
goal-reached moment was never lost.

**Where I was particularly successful**

I was most successful in the database layer. Building the DatabaseHelper with
full create, read, update, and delete functionality across three tables, and
keeping all of it isolated from the UI, was the component I'm proudest of. It
made the rest of the app simpler and demonstrated my grasp of both persistent
data storage and clean code structure.
