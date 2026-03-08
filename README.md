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

Il sistema richiede un file `config.properties` per funzionare. Crea il file nella root del progetto (per il client) o in `~/.idrolog/config.properties` (per il server) con i seguenti parametri:

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

Il server è predisposto per il deployment automatizzato su ambiente Docker remoto. È disponibile uno script di automazione che esegue il build, il trasferimento dell'artefatto e il riavvio dei container.

### Script di Deploy (`deploy.sh`)

Assicurati di aver configurato le chiavi SSH per l'accesso senza password verso il server di destinazione:

```bash
#!/bin/bash
set -e

# Configurazione parametri remoti
REMOTE_USER="<USER>"
REMOTE_HOST="<IP_OR_HOSTNAME>"
REMOTE_TARGET_DIR="<REMOTE_PROJECT_PATH>"

# 1. Compilazione del modulo server e dipendenze
mvn clean package -pl idrolog-server -am

# 2. Trasferimento del file JAR al server remoto
scp idrolog-server/target/app-shaded.jar ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_TARGET_DIR}/idrolog-server/target/app-shaded.jar

# 3. Riavvio dei container via SSH
ssh ${REMOTE_USER}@${REMOTE_HOST} "cd ${REMOTE_TARGET_DIR}/idrolog-server && docker compose down && docker compose up -d --build"

echo "Deploy completato."

```

