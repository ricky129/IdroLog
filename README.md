## Ruolo di ogni file

### `meteolino-core`

**`model/WeatherSnapshot.java`** — record immutabile che rappresenta una singola rilevazione grezza ricevuta da una delle due API. Contiene timestamp, sorgente (`API_ONE`/`API_TWO`), e i campi grezzi (temperatura, umidità, ecc.). Usato sia dal server per salvare, sia dal client per ricevere.

**`model/ProcessedData.java`** — record che rappresenta dati già elaborati (media mobile, conversioni, anomalie). Il server li pre-calcola; il client li può ricalcolare localmente.

**`dto/SnapshotResponse.java`** — quello che la REST API restituisce effettivamente al client: una lista di snapshot con metadati (range temporale, numero record). Disaccoppia il modello interno dal contratto HTTP.

**`processing/DataProcessor.java`** — logica di elaborazione stateless e pura (nessuna dipendenza da DB o rete). Metodi come `average()`, `normalize()`, `detectAnomalies()`. Essendo in `core`, sia server che client la usano con lo stesso comportamento garantito.

---

### `meteolino-server`

**`ServerMain.java`** — unico entry point. Inizializza nell'ordine: `DatabaseManager` → `DataCollectorService.start()` → `ApiServer.start()`. Non ha dipendenze da JavaFX.

**`api/ApiOneClient.java`** e **`ApiTwoClient.java`** — ciascuno incapsula una singola API esterna: URL, autenticazione, parsing della risposta in `WeatherSnapshot`. Usano `java.net.http.HttpClient` (nativo Java 11+, nessuna dipendenza).

**`scheduler/DataCollectorService.java`** — usa `ScheduledExecutorService` con virtual threads. Ogni 15 minuti chiama le due API in parallelo con Structured Concurrency (JEP 505), elabora con `DataProcessor`, salva con `SnapshotsRepository`.

**`db/DatabaseManager.java`** — apre/crea il file SQLite, esegue le migration dello schema (crea le tabelle se non esistono). Espone una `Connection` o `DataSource`.

**`db/SnapshotsRepository.java`** — CRUD: `save(WeatherSnapshot)`, `findByDateRange(from, to)`, `findLatest(n)`. Solo SQL puro con JDBC, nessun ORM.

**`http/ApiServer.java`** — Javalin che espone questi endpoint:
```
GET /api/snapshots?from=&to=     ← range temporale
GET /api/snapshots/latest?n=     ← ultimi N record
GET /api/health                  ← per Docker healthcheck
