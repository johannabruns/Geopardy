# GEOPARDY
> Because Ignorance Deserves a Stage.

Eine satirische Neuinterpretation des GeoGuessr-Konzepts als native Android-App, entwickelt mit Kotlin und Jetpack Compose.

[⬇️ **Lade die aktuelle .apk hier herunter**](https://github.com/johannabruns/Geopardy/releases/download/v1.0/Geopardy_v1.0.apk)

---
## 📱 Screenshots

| Personalisierter Homescreen | Satirisches Runden-Feedback |Profil mit "Hall of Shame" |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/be31c46c-233d-4a32-95f8-bc0debdd52ce" alt="Personalisierter Homescreen" width="250"/> | <img src="https://github.com/user-attachments/assets/e8d483e3-35f8-41b3-b195-be5820a78f78" alt="Satirisches Runden-Feedback" width="250"/> | <img src="https://github.com/user-attachments/assets/a93774af-91f6-430e-a2d8-e4090db6a328" alt="Profil mit Hall of Shame" width="250"/> |
| **Globales "Loserboard"**| **Spielergebnisse mit Badge-Feedback** | **Badge-Overlay** |
| <img src="https://github.com/user-attachments/assets/41a4798c-4ab1-45b2-af6b-b78721e3cc23" alt="Globales Loserboard" width="250"/> | <img src="https://github.com/user-attachments/assets/44a48ec3-72f8-4646-981d-46a4bf859a72" alt="Spielergebnisse mit Badge" width="250"/> | <img src="https://github.com/user-attachments/assets/a0aa9e0a-2de4-4267-a2ee-c103bea5a31a" alt="Badge-Overlay" width="250"/> |


## 🧐 Projektübersicht

`GEOPARDY` ist ein Geolocation-Spiel für Android. Nutzer werden an einem zufälligen Google Street View-Standort platziert und müssen ihren Standort auf einer Weltkarte erraten.

Das Kernkonzept ist die **ironische Gamification**: Anstelle von Belohnungen für Genauigkeit sammelt der Spieler "ShamePoints" für möglichst große Abweichungen. Dies schafft eine humorvolle User Experience, die bewusst mit gängigen Erwartungen bricht und Fehler zelebriert.

## ✨ Kernfunktionen

* **Vielfältige Spielmodi:** Bietet einen klassischen Einzelspielermodus, einen lokalen Multiplayer und thematisch kuratierte Karten (z.B. "Tourist Traps", "Popculture Hotspots") sowie einen "Challenge Mode" mit besonders abgelegenen Orten.

* **Alternatives Ranglisten-System:** Spieler konkurrieren auf einem globalen "Loserboard", auf dem der höchste Strafpunktestand gewinnt.

* **Dynamisches Feedback-System:** Ein großer Pool an zufälligen, sarkastischen "Roasts" sorgt für unterhaltsames Feedback und hohen Wiederspielwert.

* **Fortschritt & Personalisierung:** Ein Profilbereich bündelt persönliche Statistiken ("Loser Metrics") und über 20 freischaltbare Badges in der "Hall of Shame", die spezifische Fehler belohnen.

## 🚀 Technische Implementierung

#### Architektur & Navigation
* Die App ist als **Single-Activity-Architektur** unter Nutzung von **Jetpack Navigation Compose** implementiert, was eine robuste und zustandssichere Navigation gewährleistet.
* Die Architektur folgt dem **MVVM-Muster**. ViewModels sind an Navigationsgraphen gekoppelt, um den UI-Zustand konsistent über zusammengehörige Screens hinweg zu verwalten.

#### Datenmanagement & Persistenz
* Nutzerdaten, Badges und Statistiken werden mittels **SharedPreferences** persistent gespeichert. Zur Serialisierung von Kotlin-Objekten wird **Gson** eingesetzt.
* Die Logik zum Zurücksetzen der Nutzerdaten ist in einem dedizierten `DataResetManager` gekapselt.

#### Standortdaten & Performance-Optimierung
* Die App nutzt einen **vorgefilterten Datensatz** von ca. 22.000 Street-View-fähigen Koordinaten (aus Kaggle-Daten mit einem **eigenen Java-Skript** aufbereitet). Dies reduziert unnötige API-Anfragen und verbessert die Ladezeiten.
* Der **Android Geocoder** wird zur Umwandlung von Koordinaten in Länder- und Kontinentinformationen verwendet, um kontextbezogene Badges zu ermöglichen.

#### UI/UX-Design
* Das User Interface basiert auf **Material Design 3** mit vollständigem **Light- und Dark-Mode-Support**.
* Eine **Edge-to-Edge-Implementierung** und ein kartenbasiertes Design schaffen eine immersive und moderne Nutzererfahrung.

## 🛠️ Tech-Stack

* **Sprache:** Kotlin
* **UI-Toolkit:** Jetpack Compose
* **Architektur:** MVVM, Single-Activity
* **Navigation:** Jetpack Navigation Compose
* **Asynchronität:** Kotlin Coroutines
* **Datenspeicherung:** SharedPreferences, Gson
* **APIs:** Google Maps SDK, Street View API
* **Design:** Material Design 3
