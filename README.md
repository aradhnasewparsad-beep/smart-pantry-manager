# Smart Pantry Manager

An Android app (Java) that helps reduce food waste by tracking the ingredients a user actually has at home and suggesting recipes they can cook using **strictly** those leftover ingredients — no shopping trip required, and no recipe is suggested unless the user genuinely has everything it needs.

## Features

- Add, edit, and delete pantry items (name, quantity, unit, expiry date)
- Pantry list screen backed by a RecyclerView and a local database
- A seeded collection of 18 recipes, each with ingredients and preparation steps
- A **Recipes** screen that applies a strict-matching rule: a recipe only appears if every single ingredient it needs is currently in the pantry, in at least the required quantity — partial matches are excluded
- An **Almost There** section showing recipes missing exactly one ingredient
- Matching is tolerant of simple real-world messiness (e.g. "tomato" vs "tomatoes", and compatible unit differences like grams vs kilograms)
- A Recipe Detail screen showing full ingredients and method
- A Settings screen with persisted toggles (expiry alerts, metric units)
- Bottom navigation bar connecting all screens
- Clear empty-state feedback when no recipes match the current pantry

## Database

**SQLite**, implemented locally on-device using `SQLiteOpenHelper`.

This was chosen over Firebase or PostgreSQL because Smart Pantry Manager is a single-user, offline-first app — there's no need for cloud sync or a backend server, and SQLite keeps the app fully functional without an internet connection, which fits a "check what's in my kitchen" use case. It also avoids any external account setup (Firebase) or hosting a separate server (PostgreSQL), keeping the project self-contained and easy to run.

All pantry data and the recipe collection persist across app restarts, and are created automatically on first launch.

## Setup & Run Instructions

1. Clone this repository:
