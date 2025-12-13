## Empfohlene Reihenfolge

Backend-Logik & Endpoints:

POST /habits (anlegen), DELETE /habits/{id} (löschen), POST /habits/{id}/check (erledigt), optional Filter-Endpoint.

Frontend-Logik in HabitList:

Eingabefeld + Button „Habit hinzufügen“ (per Axios POST).

Button „Löschen“ pro Habit.

Button „Erledigt“ pro Habit, der später Streaks anstößt.

Wenn CRUD stabil läuft:

Tailwind-Klassen ins Template einbauen (Abstände, Farben).

Flowbite-/Flowbite-Vue-Komponenten ersetzen nach und nach plain HTML (Buttons, Cards).