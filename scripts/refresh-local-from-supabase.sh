#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOCAL_DB_PATH="${LOCAL_DB_PATH:-$PROJECT_DIR/data/finanzas}"
LOCAL_DB_FILE="${LOCAL_DB_PATH}.mv.db"
TARGET_DATABASE_URL="${TARGET_DATABASE_URL:-jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:5432/postgres?sslmode=require}"
TARGET_DATABASE_USERNAME="${TARGET_DATABASE_USERNAME:-postgres.nzzeirwbskqpbucjksmw}"

if [[ ! -f "$LOCAL_DB_FILE" ]]; then
  echo "No se encontró la base H2 local: $LOCAL_DB_FILE" >&2
  exit 1
fi

if command -v lsof >/dev/null && lsof "$LOCAL_DB_FILE" >/dev/null 2>&1; then
  echo "La base H2 está siendo usada. Detené la API local antes de continuar." >&2
  exit 1
fi

if [[ -z "${TARGET_DATABASE_PASSWORD:-}" ]]; then
  read -r -s -p "Contraseña de la base de datos de Supabase: " TARGET_DATABASE_PASSWORD
  echo
fi

if [[ -z "$TARGET_DATABASE_PASSWORD" ]]; then
  echo "La contraseña no puede estar vacía." >&2
  exit 1
fi

echo
echo "Esta operación reemplazará los datos de H2 con los datos actuales de Supabase."
echo "Supabase sólo se utilizará en modo lectura."
read -r -p "Escribí REFRESH para continuar: " CONFIRMATION
if [[ "$CONFIRMATION" != "REFRESH" ]]; then
  echo "Operación cancelada."
  exit 0
fi

BACKUP_DIR="$PROJECT_DIR/data/backups"
BACKUP_FILE="$BACKUP_DIR/finanzas-$(date +%Y%m%d-%H%M%S).mv.db"
mkdir -p "$BACKUP_DIR"
cp "$LOCAL_DB_FILE" "$BACKUP_FILE"
echo "Respaldo creado: $BACKUP_FILE"

cd "$PROJECT_DIR"
mvn -q -DskipTests compile dependency:build-classpath \
  -Dmdep.outputFile=target/refresh-classpath.txt

CLASSPATH="target/classes:$(< target/refresh-classpath.txt)"
H2_URL="jdbc:h2:file:${LOCAL_DB_PATH};MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE"

if ! java -cp "$CLASSPATH" com.finanzas.tools.SupabaseToH2Refresh \
  "$TARGET_DATABASE_URL" "$TARGET_DATABASE_USERNAME" "$TARGET_DATABASE_PASSWORD" "$H2_URL"; then
  cp "$BACKUP_FILE" "$LOCAL_DB_FILE"
  echo "El refresh falló. Se restauró automáticamente el respaldo." >&2
  exit 1
fi

echo "Tu H2 local ya refleja los datos de Supabase."
