# finanzas-api

Backend del plan financiero personal. Reemplaza al Excel
`plan_compra_apartamento_con_gastos.xlsm`: guarda sólo los datos que cargás y
deriva todo lo demás con la misma lógica que tenían las fórmulas.

## Idea central

El Excel guardaba resultados; acá **no se persiste ningún valor calculado**.
`PlanCalculator` recibe los datos crudos de un mes y devuelve el plan resuelto,
así que cambiar el dólar de referencia recalcula gastos, tarjetas y proyección
de una sola vez, sin celdas desincronizadas.

Cada mes es un `FinancialPeriod` independiente con sus propios supuestos. Eso es
lo que hace posible el histórico: editar el mes actual nunca reescribe el pasado.

## Correspondencia con las hojas del Excel

| Hoja original | Dónde vive ahora |
|---|---|
| Ingresos | `financial_periods` (salary_*, reference_rate, conservative_base_usd) |
| Plan mensual | `plan_allocations` (una fila por concepto y etapa) |
| Gastos mensuales | `expense_items` |
| Tarjetas | `credit_cards` |
| Apartamento | `financial_periods` (apartment_*) + proyección calculada |
| Resumen | `GET /api/periods/{id}` — se arma al vuelo |

Las referencias fijas del Excel (`'Plan mensual'!C4`, `C13`) se volvieron
explícitas: la columna `allocation_role` marca qué línea del plan actúa como
presupuesto de gastos y cuál como meta de ahorro. Renombrar un concepto ya no
rompe ningún cálculo.

## Correr

```bash
mvn spring-boot:run
```

Arranca en `http://localhost:8080` con H2 en archivo (`./data/finanzas.mv.db`).
No hace falta Docker. Con la base vacía, `InitialPlanSeeder` carga el mes actual
con los datos del Excel original.

Para usar MySQL en vez de H2:

```bash
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

## Variables

| Variable | Default | Para qué |
|---|---|---|
| `SERVER_PORT` | `8080` | Puerto |
| `SPRING_DATASOURCE_URL` | H2 en `./data` | Base de datos |
| `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Origen del frontend |
| `APP_SEED_ENABLED` | `true` | Cargar el plan inicial si la base está vacía |

## API

Toda mutación devuelve el periodo entero ya recalculado, para que el cliente
nunca tenga que replicar una fórmula.

| Método | Ruta | Qué hace |
|---|---|---|
| `GET` | `/api/periods` | Lista de meses |
| `GET` | `/api/periods/latest` | Mes más reciente, resuelto |
| `GET` | `/api/periods/history` | Serie histórica para graficar |
| `GET` | `/api/periods/{id}` | Un mes, resuelto |
| `POST` | `/api/periods` | Crea un mes (opcionalmente clonando otro) |
| `PUT` | `/api/periods/{id}/income` | Ingresos y dólar de referencia |
| `PUT` | `/api/periods/{id}/apartment-goal` | Meta del apartamento |
| `PUT` | `/api/periods/{id}/notes` | Reglas del plan |
| `DELETE` | `/api/periods/{id}` | Borra el mes y todo su contenido |
| `POST/PUT/DELETE` | `/api/periods/{id}/expenses[/{id}]` | Gastos |
| `POST/PUT/DELETE` | `/api/periods/{id}/cards[/{id}]` | Tarjetas |
| `POST/PUT/DELETE` | `/api/periods/{id}/allocations[/{id}]` | Líneas del plan |

## Estructura

Package por feature, igual que el resto de los proyectos: `model/` con records,
`repository/`, `service/`, `controller/`, `dto/`. Migraciones en
`resources/db/migration`, en DDL que corre tanto en H2 (modo MySQL) como en MySQL.

## Persistencia: Spring Data JDBC

Los repositorios son interfaces; las consultas se derivan del nombre del método.
Los modelos siguen siendo `record`s inmutables — Spring Data JDBC los soporta de
forma nativa, sin constructor vacío ni setters.

`Income` y `ApartmentGoal` van `@Embedded` dentro de `financial_periods`; la meta
usa `prefix = "apartment_"` para mapear a `apartment_target_price_usd` y compañía.
Las marcas de tiempo las completa `@EnableJdbcAuditing` vía `@CreatedDate` y
`@LastModifiedDate`.

### Por qué los hijos no son `@MappedCollection`

Modelar el periodo como un agregado con sus gastos, tarjetas y líneas de plan
adentro sería lo natural en Spring Data JDBC, pero al guardar el agregado **borra
todas las filas hijas y las vuelve a insertar**. Eso le cambiaría el id a cada
gasto en cada edición, y la API los expone (`PUT /periods/{id}/expenses/{id}`).

Por eso los tres son agregados propios que referencian el periodo por `periodId`,
que es además el patrón que recomienda Spring Data para referencias entre
agregados. El borrado en cascada lo cubre el `ON DELETE CASCADE` de las
migraciones.

### Identificadores en H2

La URL de H2 incluye `CASE_INSENSITIVE_IDENTIFIERS=TRUE`. Flyway crea las columnas
en minúscula, pero Spring Data JDBC las cita en mayúscula porque H2 usa
identificadores ANSI; sin ese flag, todo `INSERT` falla con *column not found*.
