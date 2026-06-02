# MotoTracker

App per registrare i tuoi giri in moto direttamente dal **Samsung Galaxy Watch 5**, con storico consultabile sull'app Android.

## Cosa registra

- Tempo di percorrenza
- Velocità massima (km/h)
- Velocità media (km/h)
- Km percorsi

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

### 4. Installazioni successive (automatiche)

Dopo la prima installazione, usa lo script PowerShell incluso:

```powershell
.\install.ps1
```

Lo script:
- Connette automaticamente il watch via Wi-Fi
- Builda entrambe le app
- Le installa su watch e telefono
- Le avvia

> Prima di eseguire lo script, apri `install.ps1` e verifica che `$WATCH_IP` e `$WATCH_PORT` corrispondano a quelli del tuo watch.

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
5. Il giro viene inviato automaticamente al telefono via Bluetooth

> Il GPS potrebbe impiegare qualche secondo per agganciarsi al segnale, specialmente al chiuso. La velocità si aggiornerà appena disponibile.

### Sul Telefono

1. Apri l'app **Moto Tracker**
2. Visualizza la lista di tutti i giri registrati
3. Tocca un giro per vedere il dettaglio completo
4. Scorri a sinistra su un giro per eliminarlo

---

## Struttura del progetto

```
moto_app/
├── shared/     → Modelli dati condivisi (RideData)
├── wear/       → App Wear OS per Galaxy Watch 5
│   ├── TrackingService.kt    → Foreground service GPS
│   ├── TrackingViewModel.kt  → Logica + invio dati
│   └── ui/TrackingScreen.kt  → UI Compose
├── app/        → Companion app Android
│   ├── WearDataListenerService.kt  → Riceve dati dal watch
│   ├── data/                       → Room database
│   └── ui/                         → Lista e dettaglio giri
└── install.ps1 → Script build + install automatico
```

---

## Risoluzione problemi

**Il watch non compare in `adb devices`**
```powershell
# Fai il pairing prima della connessione
adb pair 192.168.1.6:PORTA_PAIRING CODICE
adb connect 192.168.1.6:37321
```

**Lo script `install.ps1` non trova ADB**
```powershell
$env:Path += ";$env:LOCALAPPDATA\Android\Sdk\platform-tools"
```

**Il giro non arriva sul telefono**
- Verifica che watch e telefono siano accoppiati via Bluetooth
- Riapri l'app sul telefono e attendi qualche secondo
- Controlla che l'app companion sia installata sul telefono
