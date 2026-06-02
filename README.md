# MotoTracker

App per registrare i tuoi giri in moto direttamente dal **Samsung Galaxy Watch 5**, con storico consultabile sull'app Android.

## Cosa registra

- Tempo di percorrenza
- Velocità attuale, massima e media (km/h)
- Km percorsi
- **Percorso GPS completo** — ogni punto aggiornato ogni 2 secondi
- **Inclinazione massima** della moto (gradi °), misurata dal sensore di gravità del watch

---

## Schermata dettaglio giro

Aprendo un giro registrato, l'app mostra:

### Mappa del percorso
Il tracciato viene visualizzato su mappa **OpenStreetMap** con i tratti colorati in base alla velocità o all'inclinazione (selezionabile con il toggle **Velocità / Inclinazione**).

**Scala colori — Velocità:**
| Colore | Velocità |
|--------|----------|
| 🟢 Verde | < 40 km/h |
| 🟡 Giallo | 40 – 80 km/h |
| 🟠 Arancione | 80 – 120 km/h |
| 🔴 Rosso | > 120 km/h |

**Scala colori — Inclinazione:**
| Colore | Angolo |
|--------|--------|
| 🟢 Verde | < 15° |
| 🟡 Giallo | 15 – 30° |
| 🟠 Arancione | 30 – 45° |
| 🔴 Rosso | > 45° |

**Interazione con la mappa:**
- Tocca un tratto del percorso → appare un chip con la **velocità** e l'**inclinazione** di quel momento
- Tocca la mappa vuota → chiude il chip
- Pinch per zoomare, oppure usa i bottoni **+** / **−** in alto a destra

### Statistiche
- Distanza percorsa (km)
- Velocità massima e durata
- Velocità media
- **Inclinazione massima** raggiunta

---

## Requisiti

- Samsung Galaxy Watch 5 (Wear OS 3.5+)
- Telefono Android (API 26+)
- Android Studio (solo per la prima installazione)
- ADB installato (incluso nell'Android SDK)
- Watch e telefono sulla stessa rete Wi-Fi

---

## Installazione

### 1. Clona il repository

```bash
git clone https://github.com/toccas/moto_app.git
cd moto_app
```

### 2. Abilita la modalità sviluppatore

**Sul Galaxy Watch 5:**
1. Impostazioni → Info orologio → Informazioni software
2. Tocca **Versione software** 7 volte → compare "Sei uno sviluppatore"
3. Impostazioni → Opzioni sviluppatore → **Debug ADB** ON
4. Impostazioni → Opzioni sviluppatore → **Debug wireless** ON
5. Annota l'indirizzo IP e la porta mostrati (es. `192.168.1.6:37321`)

**Sul telefono Android:**
1. Impostazioni → Info sul telefono → tocca **Numero build** 7 volte
2. Impostazioni → Opzioni sviluppatore → **Debug USB** ON
3. Collega il telefono al PC con un cavo USB
4. Accetta il popup "Consenti debug USB" sul telefono

### 3. Prima installazione tramite Android Studio

1. Apri la cartella `moto_app` in Android Studio
2. Attendi la sincronizzazione Gradle
3. Seleziona il modulo **`wear`** → seleziona il Galaxy Watch → Run ▶
4. Seleziona il modulo **`app`** → seleziona il telefono → Run ▶

### 4. Installazioni successive

Dopo la prima installazione puoi usare Gradle direttamente da terminale:

```powershell
# Connetti il watch (se non già connesso)
adb pair 192.168.1.6:PORTA_PAIRING CODICE
adb connect 192.168.1.6:PORTA_PRINCIPALE

# Installa
.\gradlew :app:installDebug
.\gradlew :wear:installDebug
```

> La porta principale del watch si trova in: Impostazioni → Opzioni sviluppatore → Debug wireless.
> Il pairing va rifatto solo la prima volta (o se cambia rete). La `adb connect` va rifatta ad ogni riavvio del watch.

---

## Utilizzo

### Sul Watch

1. Apri l'app **Moto Tracker** dal menu del watch
2. Premi **START** per iniziare la registrazione
3. Lo schermo mostra in tempo reale:
   - Velocità attuale
   - Velocità massima
   - Distanza percorsa
   - Tempo trascorso
4. Premi **STOP** al termine del giro
5. Il giro viene inviato automaticamente al telefono via Bluetooth/Wi-Fi

> Il GPS potrebbe impiegare qualche secondo per agganciarsi al segnale, specialmente al chiuso.

### Sul Telefono

1. Apri l'app **Moto Tracker**
2. Visualizza la lista di tutti i giri registrati
3. Tocca un giro per vedere la mappa e le statistiche complete
4. Usa il toggle **Velocità / Inclinazione** per cambiare la colorazione della mappa
5. Tocca un tratto del percorso per vedere velocità e inclinazione in quel punto
6. Tocca l'icona 🐛 in alto a destra per inserire un **giro di test** (utile in sviluppo)

---

## Struttura del progetto

```
moto_app/
├── shared/     → Modelli dati condivisi (RideData, GpsPoint)
├── wear/       → App Wear OS per Galaxy Watch 5
│   ├── TrackingService.kt    → Foreground service GPS + sensore gravità
│   ├── TrackingViewModel.kt  → Logica + invio dati compressi (Gzip)
│   └── ui/TrackingScreen.kt  → UI Compose
├── app/        → Companion app Android
│   ├── WearDataListenerService.kt  → Riceve dati dal watch
│   ├── data/                       → Room database (sessioni + punti GPS)
│   └── ui/                         → Lista giri e dettaglio con mappa
└── gradle/libs.versions.toml → Catalogo dipendenze centralizzato
```

---

## Risoluzione problemi

**Il watch non compare in `adb devices`**
```powershell
adb pair 192.168.1.6:PORTA_PAIRING CODICE
adb connect 192.168.1.6:PORTA_PRINCIPALE
```

**ADB non trovato**
```powershell
$env:Path += ";$env:LOCALAPPDATA\Android\Sdk\platform-tools"
```

**Il giro non arriva sul telefono**
- Verifica che watch e telefono siano accoppiati via Bluetooth
- Riapri l'app sul telefono e attendi qualche secondo
- Controlla che l'app companion sia installata sul telefono

**La mappa non mostra il percorso**
- Il giro deve essere stato registrato con la nuova versione dell'app (i giri vecchi non hanno dati GPS)
- Usa il pulsante 🐛 per generare un giro di test con percorso GPS incluso
