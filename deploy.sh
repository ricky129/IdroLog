#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Funzione per leggere una proprietà da file .properties Java
get_prop() {
    local key="$1"
    local file="$2"
    grep -E "^[[:space:]]*${key}[[:space:]]*=" "$file" 2>/dev/null | head -n 1 | cut -d'=' -f2- | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//' | tr -d '\r'
}

# Ricerca del file config.properties
CONFIG_PATH=""
if [ -f "$SCRIPT_DIR/config.properties" ]; then
    CONFIG_PATH="$SCRIPT_DIR/config.properties"
elif [ -f "$SCRIPT_DIR/idrolog-server/config.properties" ]; then
    CONFIG_PATH="$SCRIPT_DIR/idrolog-server/config.properties"
else
    echo "Errore: file config.properties non trovato!"
    echo "Copia config.properties.example in config.properties e configura i tuoi parametri:"
    echo "  cp config.properties.example config.properties"
    exit 1
fi

# Lettura parametri di configurazione
DEPLOY_USER="$(get_prop "deploy.user" "$CONFIG_PATH")"
DEPLOY_REMOTE_DIR="$(get_prop "deploy.remote_dir" "$CONFIG_PATH")"
DEPLOY_PORT="$(get_prop "deploy.port" "$CONFIG_PATH")"
DEPLOY_PORT="${DEPLOY_PORT:-22}"
DEPLOY_KEY="$(get_prop "deploy.key" "$CONFIG_PATH")"

# Risoluzione host: deploy.host > server.tailscale > server.local
DEPLOY_HOST="$(get_prop "deploy.host" "$CONFIG_PATH")"
if [ -z "$DEPLOY_HOST" ]; then
    SERVER_TAILSCALE="$(get_prop "server.tailscale" "$CONFIG_PATH")"
    if [ -n "$SERVER_TAILSCALE" ]; then
        DEPLOY_HOST="$(echo "$SERVER_TAILSCALE" | sed -E 's|^https?://([^:/]+).*|\1|')"
    fi
fi
if [ -z "$DEPLOY_HOST" ]; then
    SERVER_LOCAL="$(get_prop "server.local" "$CONFIG_PATH")"
    if [ -n "$SERVER_LOCAL" ]; then
        DEPLOY_HOST="$(echo "$SERVER_LOCAL" | sed -E 's|^https?://([^:/]+).*|\1|')"
    fi
fi

# Validazione parametri obbligatori
if [ -z "$DEPLOY_USER" ]; then
    echo "Errore: parametro 'deploy.user' mancante in $CONFIG_PATH"
    exit 1
fi
if [ -z "$DEPLOY_HOST" ]; then
    echo "Errore: host non trovato. Configura 'deploy.host' o 'server.tailscale' in $CONFIG_PATH"
    exit 1
fi
if [ -z "$DEPLOY_REMOTE_DIR" ]; then
    echo "Errore: parametro 'deploy.remote_dir' mancante in $CONFIG_PATH"
    exit 1
fi

# Opzioni SSH & SCP
SSH_OPTS=(-p "$DEPLOY_PORT")
SCP_OPTS=(-P "$DEPLOY_PORT")

if [ -n "$DEPLOY_KEY" ]; then
    SSH_OPTS+=(-i "$DEPLOY_KEY")
    SCP_OPTS+=(-i "$DEPLOY_KEY")
fi

echo "==> Target deploy: ${DEPLOY_USER}@${DEPLOY_HOST}:${DEPLOY_REMOTE_DIR}"

echo "==> Compilazione progetto..."
"$SCRIPT_DIR/mvnw" clean package -pl idrolog-server -am

echo "==> Preparazione directory sul server remoto..."
ssh "${SSH_OPTS[@]}" "${DEPLOY_USER}@${DEPLOY_HOST}" "mkdir -p \"${DEPLOY_REMOTE_DIR}/idrolog-server/target\""

echo "==> Copia artefatti sul server remoto..."
scp "${SCP_OPTS[@]}" "$SCRIPT_DIR/idrolog-server/target/app-shaded.jar" "${DEPLOY_USER}@${DEPLOY_HOST}:${DEPLOY_REMOTE_DIR}/idrolog-server/target/app-shaded.jar"
scp "${SCP_OPTS[@]}" "$SCRIPT_DIR/idrolog-server/docker-compose.yaml" "$SCRIPT_DIR/idrolog-server/Dockerfile" "${DEPLOY_USER}@${DEPLOY_HOST}:${DEPLOY_REMOTE_DIR}/idrolog-server/"
scp "${SCP_OPTS[@]}" "$CONFIG_PATH" "${DEPLOY_USER}@${DEPLOY_HOST}:${DEPLOY_REMOTE_DIR}/idrolog-server/config.properties"

echo "==> Riavvio container Docker..."
ssh "${SSH_OPTS[@]}" "${DEPLOY_USER}@${DEPLOY_HOST}" bash -c "'
    if [ -f \"${DEPLOY_REMOTE_DIR}/docker-compose.yaml\" ]; then
        cd \"${DEPLOY_REMOTE_DIR}\" && docker compose down 2>/dev/null || true
        rm -f \"${DEPLOY_REMOTE_DIR}/docker-compose.yaml\" \"${DEPLOY_REMOTE_DIR}/Dockerfile\" 2>/dev/null || true
    fi
    docker rm -f idrolog-server 2>/dev/null || true
    cd \"${DEPLOY_REMOTE_DIR}/idrolog-server\" && docker compose down && docker compose up -d --build
'"

echo "Deploy completato con successo."