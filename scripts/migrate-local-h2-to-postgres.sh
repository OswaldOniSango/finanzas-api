#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOCAL_DB_PATH="${LOCAL_DB_PATH:-$PROJECT_DIR/data/finanzas}"
TARGET_DATABASE_URL="${TARGET_DATABASE_URL:-jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:5432/postgres?sslmode=require}"
TARGET_DATABASE_USERNAME="${TARGET_DATABASE_USERNAME:-postgres.nzzeirwbskqpbucjksmw}"

echo "Migración: H2 local → Supabase PostgreSQL"
echo "Base local: $LOCAL_DB_PATH"
echo "Destino: Supabase finanzas-database"
echo

if [[ -z "${TARGET_DATABASE_PASSWORD:-}" ]]; then
  read -r -s -p "Contraseña de la base de datos de Supabase: " TARGET_DATABASE_PASSWORD
  echo
fi

if [[ -z "$TARGET_DATABASE_PASSWORD" ]]; then
  echo "La contraseña no puede estar vacía." >&2
  exit 1
fi

MODE="${1:---dry-run}"
if [[ "$MODE" != "--dry-run" && "$MODE" != "--execute" ]]; then
  echo "Uso: $0 [--dry-run|--execute]" >&2
  exit 1
fi

if [[ "$MODE" == "--dry-run" ]]; then
  echo "Modo simulación: no se guardará ningún cambio."
else
  echo "Modo migración real: los datos se guardarán en Supabase."
  read -r -p "Escribí MIGRAR para continuar: " CONFIRMATION
  if [[ "$CONFIRMATION" != "MIGRAR" ]]; then
    echo "Migración cancelada."
    exit 0
  fi
fi

cd "$PROJECT_DIR"

mvn -q -DskipTests package dependency:build-classpath \
  -Dmdep.outputFile=target/migration-classpath.txt

CLASSPATH="target/classes:$(< target/migration-classpath.txt)"

java -cp "$CLASSPATH" com.finanzas.tools.LocalDataMigration \
  "jdbc:h2:file:${LOCAL_DB_PATH};MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE" \
  "$TARGET_DATABASE_URL" \
  "$TARGET_DATABASE_USERNAME" \
  "$TARGET_DATABASE_PASSWORD" \
  "$MODE"
