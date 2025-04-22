# Minecraft Duell Plugin

Ein leichtgewichtiges und performantes 1v1 Duell-Plugin für Minecraft 1.21.4.

## Features

*   Fordere andere Spieler zu 1v1 Duellen heraus.
*   Optionale Wetteinsätze über **Vault**.
*   Konfigurierbarer Countdown vor dem Duellstart.
*   Anpassbare Nachrichten über `messages.yml`.
*   Konfigurierbare Einstellungen (Einsatzlimits, Timeout, etc.) über `config.yml`.
*   Sperrt Weltinteraktionen und Befehle während eines Duells.
*   Optionale **WorldGuard**-Integration, um PvP in geschützten Regionen während eines Duells zu ermöglichen.
*   Berechtigungssystem (`duel.request`, `duel.accept`, `duel.admin`).
*   Admin-Befehl zum Neuladen der Konfiguration (`/duel reload`).

## Abhängigkeiten

*   **Vault**: Benötigt für die optionale Wettfunktion.
*   **WorldGuard** (Optional): Benötigt für die automatische Überschreibung des PvP-Flags in Regionen während Duellen.

## Installation

1.  Stelle sicher, dass dein Server PaperMC 1.21.4 verwendet.
2.  Installiere Vault (und ein kompatibles Economy-Plugin, falls du Wetten nutzen möchtest).
3.  (Optional) Installiere WorldGuard.
4.  Lade die `DuellSystem.jar`-Datei herunter und platziere sie in deinem `plugins`-Ordner.
5.  Starte oder lade deinen Server neu.
6.  Passe bei Bedarf die `config.yml` und `messages.yml` im `plugins/Duell`-Ordner an.

## Befehle

*   `/duel <Spieler>`: Fordere einen Spieler zu einem Duell ohne Einsatz heraus.
*   `/duel <Spieler> <Betrag>`: Fordere einen Spieler zu einem Duell mit einem Wetteinsatz heraus.
*   `/duel accept <Spieler>`: Nimm eine ausstehende Duell-Anfrage an.
*   `/duel reload`: Lädt die Konfiguration und Nachrichten neu (Admin-Berechtigung benötigt).

## Berechtigungen

*   `duel.request`: Erlaubt das Senden von Duell-Anfragen. (Standard: true)
*   `duel.accept`: Erlaubt das Annehmen von Duell-Anfragen. (Standard: true)
*   `duel.admin`: Erlaubt die Nutzung des `/duel reload`-Befehls und umgeht die Befehlssperre während Duellen. (Standard: op) 