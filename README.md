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

Il progetto distingue chiaramente tra configurazione applicativa e configurazione di infrastruttura:

* **`config.properties` (Applicazione / Runtime Java)**: contiene i parametri di dominio (ID della stazione, coordinate, endpoint del server). Viene letto dal codice Java sia dal client che dal server (anche dentro il container Docker).
* **`.env` (Infrastruttura / Deploy locale)**: contiene esclusivamente i parametri SSH utilizzati dallo script Bash `deploy.sh` (`DEPLOY_USER`, `DEPLOY_HOST`, porta, cartella remota) per automatizzare il deployment. Rimane solo sulla tua macchina locale e non viene incluso nell'applicazione né nel container.

### Configurazione Applicativa (`config.properties`)

Crea il file nella root del progetto o all'interno di `idrolog-server/config.properties` con i seguenti parametri:

```properties
# Indirizzi del server IdroLog
server.local=http://<IP_LOCALE_SERVER>:7000
server.tailscale=http://<IP_TAILSCALE_SERVER>:7000

# Parametri della stazione di monitoraggio
station.id=XXXXX
station.latitude=XX.X
station.longitude=XX.X
```

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

1. **Configurazione ambiente:**
   Copia il file di esempio e configura i parametri di connessione del server remoto:
   ```bash
   cp .env.example .env
   ```
   Configura i valori (`DEPLOY_USER`, `DEPLOY_HOST`, `DEPLOY_REMOTE_DIR`, ecc.).

2. **Esecuzione del deploy:**
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


