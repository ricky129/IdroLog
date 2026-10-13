# IdroLog 🌊

IdroLog è un'applicazione Java per monitorare il livello dei fiumi e le piogge. Raccoglie i dati da sensori esterni, li salva in un database e li mostra su un grafico semplice da consultare.

## 🚀 Caratteristiche principali

* **Monitoraggio Real-time:** Visualizzazione dei livelli fluviali e delle precipitazioni totali.
* **Dashboard Desktop:** UI realizzata in JavaFX.
* **Gestione Temporale Avanzata:** Utilizzo di `java.time.Instant` per una sincronizzazione perfetta tra Database (UTC) e visualizzazione locale (Europe/Rome).
* **Analisi dei Trend:** Grafici a linee con scale dinamiche e filtri temporali selezionabili (6h, 12h, 24h, 48h).
* **Suddivisione dei ruoli:** Suddivisione in moduli Maven per separare core logic, server e client.

## 🏗️ Struttura del Progetto

* `idrolog-core`: Modello dati condiviso (`WeatherSnapshot`) e logiche di processamento comuni.
* `idrolog-server`: Backend basato su **Javalin 7** con scheduler per il recupero dati e persistence su **SQLite**. Supporta il deployment tramite **Docker**.
* `idrolog-client`: Applicazione desktop **JavaFX 25** con stili CSS personalizzati per un'esperienza utente moderna.

## 🛠️ Requisiti

* **Java 25** (Oracle JDK o OpenJDK)
* **Maven 3.9.6+**, incluso tramite Maven Wrapper
* **Docker & Docker Compose** per il deployment del server

## ⚙️ Configurazione

Tutte le configurazioni del progetto sono centralizzate in un unico file: `config.properties`.
Per iniziare, crea la tua configurazione copiando il file di esempio:

```bash
cp config.properties.example config.properties
```

Il file può essere posizionato nella radice del progetto o all'interno di `idrolog-server/config.properties` e contiene i seguenti parametri:

```properties
# --- Connessione Server (usato dal Client JavaFX) ---
# server.tailscale: OBBLIGATORIO (provato per primo dal Client e usato come default da deploy.sh)
server.tailscale=http://<IP_O_MAGICDNS_TAILSCALE>:7000

# server.local: OPZIONALE (usato come fallback se Tailscale non è raggiungibile)
server.local=http://<IP_LOCALE_SERVER>:7000

# --- Parametri Stazione di Monitoraggio (usato dal Backend Server) ---
station.id=XXXXX
station.latitude=XX.X
station.longitude=XX.X

# --- Deployment Remoto SSH (usato da deploy.sh) ---
deploy.user=myserver
deploy.remote_dir=~/docker/idrolog
# deploy.host=          # Opzionale: se omesso, viene estratto automaticamente da server.tailscale
# deploy.port=22        # Opzionale: default 22
# deploy.key=           # Opzionale: percorso della chiave privata SSH
```

> [!NOTE]
> Il Client desktop JavaFX prova sempre prima l'indirizzo **Tailscale** (`server.tailscale`). Se non risponde entro 2 secondi e `server.local` è configurato, esegue il fallback automatico sull'indirizzo locale.

### Come ottenere l'ID della stazione (`station.id`)
Il sistema utilizza i dati pubblici della rete di monitoraggio della **Regione Emilia-Romagna**:
1. Accedi al portale [Allerta Meteo Emilia-Romagna - Dati in tempo reale](https://allertameteo.regione.emilia-romagna.it/monitoraggio-sensori).
2. Individua sulla mappa la stazione idrometrica (fiume) che desideri monitorare.
3. Clicca sulla stazione per aprire i dettagli: l'ID è il codice numerico identificativo della stazione.
4. Copia l'ID nel file `config.properties` alla voce `station.id`.

Le coordinate (`latitude` e `longitude`) servono invece per il recupero dei dati pluviometrici tramite Open-Meteo e dovrebbero corrispondere alla posizione della stazione o del comune di interesse.

## 📦 Installazione e Avvio Locale

### 1. Clonare la repository

```bash
git clone https://github.com/ricky129/IdroLog.git
cd IdroLog

```

### 2. Avviare il Server

```bash
mvn clean install
cd idrolog-server
mvn exec:java -Dexec.mainClass="org.example.idrolog.server.ServerMain"

```

### 3. Avviare il Client (Uber-JAR)

Genera il pacchetto eseguibile che contiene tutte le dipendenze e avvialo:
```bash
./mvnw clean package -pl idrolog-client -am
java -jar idrolog-client/target/idrolog-client-1.0-SNAPSHOT.jar
```

*Nota: Se il comando `java` punta a una versione precedente, usa il percorso completo del JDK 25.*

## 🚢 Deployment (Server)

Il server è predisposto per il deployment su ambiente Docker. I file di configurazione Docker ([Dockerfile](idrolog-server/Dockerfile) e [docker-compose.yaml](idrolog-server/docker-compose.yaml)) si trovano all'interno del modulo `idrolog-server/`.

### Deployment automatizzato (`deploy.sh`)

È disponibile lo script `deploy.sh` che esegue automaticamente la compilazione del server, il trasferimento degli artefatti e delle configurazioni via SSH/SCP e il riavvio dei container sul server remoto.

I parametri di connessione (`deploy.user`, `deploy.remote_dir`, porta e chiave SSH) vengono letti direttamente da `config.properties`. L'host di destinazione viene ricavato automaticamente da `server.tailscale` (oppure sovrascritto impostando `deploy.host`).

Per avviare il deploy:
```bash
./deploy.sh
```

### Avvio manuale con Docker Compose

Se desideri eseguire il server con Docker localmente o gestirlo manualmente:

1. Compila il modulo server:
   ```bash
   ./mvnw clean package -pl idrolog-server -am
   ```

2. Assicurati che `config.properties` sia presente in `idrolog-server/` (o montato nel container).

3. Avvia i container:
   ```bash
   cd idrolog-server
   docker compose up -d --build
   ```


