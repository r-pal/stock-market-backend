# Banker guide

## Authentication

1. By default, a banker user is created on first startup (`goblin.banker.username` / `goblin.banker.password` in `application.properties`; defaults are `banker` / `banker`).
2. Login: `POST /api/banker/auth/login` with JSON `{"username":"...","password":"..."}`.
3. Use the returned `accessToken` as `Authorization: Bearer <token>` on all `/api/banker/**` requests (except login).

## Houses

- **Create**: `POST /api/banker/accounts` with `{"name":"Hufflepuff","initialBalance":"1000.00"}`. Names must be unique among active (non-deleted) houses (case-insensitive).
- **Delete**: `DELETE /api/banker/accounts/{id}`. Allowed only if balance is **exactly zero** and there are **no open** investment positions involving that house.
- **Deposit / withdraw**: `POST /api/banker/accounts/{id}/deposit` or `.../withdraw` with `{"amount":"100.00"}`. Amounts must be positive; overdrafts are allowed on withdraw.
- **Rename**: `PUT /api/banker/accounts/{id}/name` with `{"name":"New Name"}`. Changes are written to `house_rename_audit`.

## Portal passwords

- **Set or reset**: `PUT /api/banker/accounts/{id}/portal-password` with `{"newPassword":"..."}` (BCrypt hash stored only).
- Houses cannot log in until a password is set. Initial login errors are intentionally generic.

### Seeded “Goblin Market” houses (Christina Rossetti)

After migrations, the six starter accounts use names from *Goblin Market*. On first startup, portal passwords are set automatically (one each) to **fruits named in the poem** (lowercase). If a house already has a portal hash (e.g. you set one manually), it is left unchanged.

| House (name) | Fruit password (house portal) |
|--------------|------------------------------|
| Laura | `cherries` |
| Lizzie | `quinces` |
| Jeanie | `mulberries` |
| Golden Head | `peaches` |
| Brookside | `figs` |
| Moonlight | `pomegranates` |

Use `POST /api/house/auth/login` with `houseId` (usually 1–6 on a fresh database) and the matching password.

## Interest

- **Global base rate** (per hour, decimal fraction): `PUT /api/banker/interest/global` with `{"baseRatePerHour":"0.05"}`.
- **Per-house adjustment**: `PUT /api/banker/accounts/{id}/interest-adjustment` with `{"accountRateAdjustmentPerHour":"0.01"}`.
- Effective rate = base + adjustment. Bounds are enforced via `goblin.rates.min-hourly` and `goblin.rates.max-hourly` (defaults `-1` and `1`). Global base changes must keep every house’s effective rate inside the bounds.

## Investments (admin)

- **Buy on behalf of a house**: `POST /api/banker/investments/buy` with `{"buyerHouseId":1,"targetHouseId":2,"amount":"500.00"}`.
- **Sell**: `POST /api/banker/investments/{positionId}/sell` with optional body `{"buyerHouseId":1}` if you need to disambiguate; otherwise the position’s buyer is used.
- **List**: `GET /api/banker/investments?houseId=&status=` (`status` e.g. `OPEN` or `CLOSED`).

Payout on sell: `principal * (targetSharePriceAtSell / targetSharePriceAtBuy)` (can be negative).

## Share price tuning

- **Combined patch**: `PUT /api/banker/share-price-config` with any of `hypeSensitivity`, `interestHorizonHours`, `momentumLookbackHours` (omit fields you do not want to change).
- Focused endpoints: `PUT /api/banker/share-price-config/hype`, `PUT /api/banker/share-price-config/momentum-lookback-hours`.

## History snapshots

- **Interval** (minutes, must divide 60 evenly): `PUT /api/banker/history-config` with `{"snapshotIntervalMinutes":20}`.

## Game clock

- **Duration** (minutes, only before start): `PUT /api/banker/game/config` with `{"gameDurationMinutes":1440}`.
- **Start** (idempotent): `POST /api/banker/game/start` sets `gameStartAt` once.
- **Status**: `GET /api/banker/game/status`.

## Ledger

- **Audit**: `GET /api/banker/ledger?accountId=&page=0` (page size 100).

## Common errors

- **400**: validation messages in `{"error":"..."}` (e.g. duplicate house name, bounds, bad investment).
- **401/403**: missing or wrong JWT / role.
- **409**: `IllegalStateException` (e.g. changing game duration after start).

Swagger UI: `/swagger-ui.html`; OpenAPI JSON: `/v3/api-docs`.
