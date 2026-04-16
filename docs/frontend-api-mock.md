# Frontend mock API docs (Goblin Bank)

This is a frontend-oriented summary of the backend HTTP API shapes your UI will call.

## Base URLs

- **Public**: `/api/public/*` (no auth)
- **House (portal)**: `/api/house/*` (Bearer JWT required; role `HOUSE_PORTAL`)
- **Banker**: `/api/banker/*` (Bearer JWT required; role `BANKER`)

## Authentication

### Banker login

- **POST** `/api/banker/auth/login`

Request:

```json
{ "username": "banker", "password": "banker" }
```

Response:

```json
{ "accessToken": "<jwt>" }
```

JWT notes:
- claim `role = BANKER`
- subject `banker:<username>`

### House login

- **POST** `/api/house/auth/login`

Request:

```json
{ "houseId": 1, "password": "cherries" }
```

Response:

```json
{ "accessToken": "<jwt>" }
```

JWT notes:
- claim `role = HOUSE_PORTAL`
- claim `houseId = 1`
- subject `house:1`

### Auth header

For house/banker routes (everything except login):

```http
Authorization: Bearer <accessToken>
```

## Public endpoints (no auth)

### List houses (for scoreboard / market overview)

- **GET** `/api/public/accounts`

Response:

```json
{
  "baseRatePerHour": "0.05",
  "accounts": [
    {
      "id": 1,
      "houseName": "Laura",
      "balance": "1000.00",
      "accountRateAdjustmentPerHour": "0.00000000",
      "effectiveRatePerHour": "0.05000000",
      "sharePrice": "1.234567",
      "holdingsValue": "0.00",
      "momentum": "0.000000"
    }
  ]
}
```

### List tradable stocks (houses + item stocks)

- **GET** `/api/public/stocks`

Response:

```json
[
  {
    "id": 10,
    "displayName": "Laura",
    "stockType": "HOUSE",
    "houseAccountId": 1,
    "currentPrice": "1.234567",
    "active": true
  },
  {
    "id": 200,
    "displayName": "Onions",
    "stockType": "ITEM",
    "houseAccountId": null,
    "currentPrice": "2.000000",
    "active": true
  }
]
```

### Ticker

- **GET** `/api/public/ticker`

Response:

```json
{ "message": "Laura ₲1.23 ▲ 2.3% | Lizzie ₲0.98 ▼ 1.1% | ..." }
```

### History chart data

- **GET** `/api/public/history`

Query params (optional): `fromMinutes`, `toMinutes`, `houseIds`

## House portal endpoints (auth)

### “Who am I?” account snapshot

- **GET** `/api/house/account`

Response: same shape as a single entry in `/api/public/accounts` (one `PublicAccountDto`).

### List stocks (authenticated)

- **GET** `/api/house/stocks`

Response: same as `/api/public/stocks`.

### Buy stock (house shares or item stocks)

- **POST** `/api/house/investments/buy`

Request (recommended — generalized):

```json
{ "stockId": 200, "amount": "50.00" }
```

Legacy request (house shares only; supported for backwards compatibility):

```json
{ "targetHouseId": 2, "amount": "50.00" }
```

Response (`PositionResponseDto`):

```json
{
  "id": 999,
  "buyerHouseId": 1,
  "targetHouseId": null,
  "stockId": 200,
  "stockType": "ITEM",
  "stockDisplayName": "Onions",
  "principalAmount": 50.00,
  "status": "OPEN",
  "boughtAt": "2026-04-16T10:00:00Z",
  "soldAt": null,
  "payoutAmount": null
}
```

Notes:
- For **HOUSE** stocks, `targetHouseId` will be the house id.
- For **ITEM** stocks, `targetHouseId` will be `null`.

### Sell position

- **POST** `/api/house/investments/{positionId}/sell`

Response: `PositionResponseDto` with `status="CLOSED"` and `payoutAmount` filled.

### List positions

- **GET** `/api/house/investments?status=OPEN|CLOSED`

Response: `PositionResponseDto[]`

## Banker endpoints (auth)

### Pawn shop (manage item stocks + view all stocks)

- **GET** `/api/banker/pawn-shop/stocks`
- **POST** `/api/banker/pawn-shop/stocks`

Request:

```json
{ "displayName": "Frogs", "currentPrice": "3.500000" }
```

- **PUT** `/api/banker/pawn-shop/stocks/{id}/price`

Request:

```json
{ "newPrice": "4.000000" }
```

### Buy stock on behalf of a house

- **POST** `/api/banker/investments/buy`

General request:

```json
{ "buyerHouseId": 1, "stockId": 200, "amount": "50.00" }
```

Legacy request (house shares only):

```json
{ "buyerHouseId": 1, "targetHouseId": 2, "amount": "50.00" }
```

### Sell position (banker)

- **POST** `/api/banker/investments/{positionId}/sell`

Optional body (only if you need to disambiguate buyer house):

```json
{ "buyerHouseId": 1 }
```

### Houses admin / game admin / tuning

See `docs/banker-guide.md` for the full list of banker endpoints.

## Common status codes (UI handling)

- **200**: success
- **400**: validation / game rules error → `{"error":"...message..."}` (show to banker; for houses you may want friendlier text)
- **401/403**: missing/expired token or wrong role → send user back to login
- **404**: not found (account/position/etc.)

