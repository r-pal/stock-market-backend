# Public Goblin investor guide

No login is required for read-only investor APIs.

## Endpoints

| Endpoint | Purpose |
|---------|---------|
| `GET /api/public/accounts` | All active houses with balances, rates, share price, holdings, momentum |
| `GET /api/public/accounts/{id}` | One house (404 if soft-deleted) |
| `GET /api/public/ticker` | Single LED-style `message` string |
| `GET /api/public/history` | Multi-series chart data |

## Account fields

- **baseRatePerHour**: global interest base (same on every response).
- **balance**: cash in the house vault (₲ in examples).
- **accountRateAdjustmentPerHour**: house-specific add-on to the base rate.
- **effectiveRatePerHour**: base + adjustment.
- **sharePrice**: computed from the live formula (see below).
- **holdingsValue**: marked value of open positions this house holds in other houses.
- **momentum**: cash wealth momentum over the configured lookback window (transparency).

## Share price formula

\[
\text{SharePrice} = \frac{(W + H)\times(1 + k\times R)\times(1 + h\times m)}{N}
\]

- \(W\): cash balance  
- \(H\): holdings (for each open position: principal × (target wealth now ÷ target wealth at buy), summed)  
- \(R\): effective hourly rate  
- \(k\): `interestHorizonHours` (default 24)  
- \(h\): `hypeSensitivity` (default 0.5)  
- \(m\): momentum from wealth snapshots vs lookback \(L\) hours (default 2)  
- \(N\): 1000 shares outstanding  

**Worked example** (illustrative numbers): \(W=10{,}000\), \(H=0\), \(R=0.05\), \(k=24\) → interest factor \(1 + 24\times0.05 = 2.2\). If \(m=0.1\) and \(h=0.5\), hype factor \(1 + 0.5\times0.1 = 1.05\). Numerator \(10{,}000\times2.2\times1.05 = 23{,}100\); share price \(23{,}100 / 1000 = 23.10\) ₲.

Share prices may be negative per game rules.

## Ticker

Response shape: `{"message":"HOUSE1 ₲15.41 ▲ 2.3% | ..."}`.

Segments are sorted by house id. Percent compares current share price to the last synced baseline after committed events (`▲` / `▼` / `-` for flat). Near-zero prior prices use a small epsilon denominator.

## History (`GET /api/public/history`)

Query parameters (all optional):

- **fromMinutes**, **toMinutes**: game minutes elapsed since start, clamped to `0 .. gameDurationMinutes`. If both omitted, full range is used. If `fromMinutes > toMinutes`, the API returns **400**.
- **houseIds**: repeat param or comma-separated list depending on client (e.g. `houseIds=1&houseIds=2`).

Response includes:

- **intervalMinutes**: snapshot cadence (e.g. 20).
- **gameMinutesTotal**, **gameMinutesElapsed**, **gameMinutesRemaining** (before start, elapsed = 0 and remaining = total).
- **currencySymbol**: ₲  
- **series[]**: each with `houseId`, `houseName`, `points[]` of `{ minutes, balance, sharePrice, effectiveRatePerHour }`.

If the game has not started, series may be empty until snapshots run.

## Behaviour notes

- Hourly interest runs in UTC; snapshots align to UTC minutes on the configured interval once the game has started.
- Large deposits or withdrawals move cash wealth and therefore momentum (`m`), producing temporary “hype” moves in the price.
