#!/bin/bash
    set -e

    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

    # Check if .env exists
    if [ -f "$SCRIPT_DIR/.env" ]; then
        # Export variables from .env
        set -a
        source "$SCRIPT_DIR/.env"
        set +a
    else
        echo "Errore: file .env non trovato!"
        echo "Copia .env.example in .env e configura i tuoi parametri:"
        echo "  cp .env.example .env"
        exit 1
    fi

    # Validation
    : "${DEPLOY_USER:?Parametro DEPLOY_USER mancante nel file .env}"
    : "${DEPLOY_HOST:?Parametro DEPLOY_HOST mancante nel file .env}"
    : "${DEPLOY_REMOTE_DIR:?Parametro DEPLOY_REMOTE_DIR mancante nel file .env}"
    DEPLOY_PORT="${DEPLOY_PORT:-22}"
    CONFIG_FILE="${CONFIG_FILE:-config.properties}"

    # SSH & SCP options
    SSH_OPTS=(-p "$DEPLOY_PORT")
    SCP_OPTS=(-P "$DEPLOY_PORT")

    if [ -n "$DEPLOY_KEY" ]; then
        SSH_OPTS+=(-i "$DEPLOY_KEY")
        SCP_OPTS+=(-i "$DEPLOY_KEY")
    fi


    echo "==> Compilazione progetto..."
    "$SCRIPT_DIR/mvnw" clean package -pl idrolog-server -am

    echo "==> Preparazione directory sul server remoto..."
    ssh "${SSH_OPTS[@]}" "${DEPLOY_USER}@${DEPLOY_HOST}" "mkdir -p \"${DEPLOY_REMOTE_DIR}/idrolog-server/target\""

    echo "==> Copia artefatti sul server remoto (${DEPLOY_USER}@${DEPLOY_HOST})..."
    scp "${SCP_OPTS[@]}" "$SCRIPT_DIR/idrolog-server/target/app-shaded.jar" "${DEPLOY_USER}@${DEPLOY_HOST}:${DEPLOY_REMOTE_DIR}/idrolog-server/target/app-shaded.jar"

    if [ -f "$SCRIPT_DIR/$CONFIG_FILE" ]; then
        scp "${SCP_OPTS[@]}" "$SCRIPT_DIR/$CONFIG_FILE" "${DEPLOY_USER}@${DEPLOY_HOST}:${DEPLOY_REMOTE_DIR}/config.properties"
    fi

    echo "==> Riavvio container Docker..."
    ssh "${SSH_OPTS[@]}" "${DEPLOY_USER}@${DEPLOY_HOST}" bash -c "'
        if [ -f \"${DEPLOY_REMOTE_DIR}/docker-compose.yaml\" ]; then
            cd \"${DEPLOY_REMOTE_DIR}\"
        else
            cd \"${DEPLOY_REMOTE_DIR}/idrolog-server\"
        fi
        docker compose down && docker compose up -d --build
    '"

    echo "Deploy completato con successo."