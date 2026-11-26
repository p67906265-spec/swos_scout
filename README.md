# SWOS Scout

App Android per cercare e filtrare i giocatori di Sensible World of Soccer a partire da un file CSV.

- I dati stanno in `app/src/main/assets/players.csv` (colonne: country, club, number, player, position, P, V, H, T, C, S, F, key_skills, price, value_k, nationality, nationality_code).
- Per aggiornare il database basta sostituire quel file e ricompilare.
- Build: GitHub Actions (`.github/workflows/build.yml`) produce l'APK debug come artifact.
