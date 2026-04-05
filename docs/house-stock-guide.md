# House stock portal guide

House players manage investments only for **their** house using a portal password and JWT.

## Getting started

1. The banker must set your portal password: `PUT /api/banker/accounts/{houseId}/portal-password`.
2. Login: `POST /api/house/auth/login` with `{"houseId":1,"password":"..."}`.
3. Send `Authorization: Bearer <accessToken>` on subsequent `/api/house/**` calls (except login).

If login fails (wrong password, deleted house, or password not set yet), the API returns a **generic** error (`400` with a simple message).

## Change your password

While logged in: `PUT /api/house/auth/password` with:

```json
{"currentPassword":"...","newPassword":"..."}
```

If you are locked out, ask the banker to reset the portal password.

## Read your account

`GET /api/house/account` returns the same investor-style fields as the public API (`balance`, rates, `sharePrice`, `holdingsValue`, `momentum`) for **your** house only.

## Trading

- **Buy shares in another house** (you are always the buyer):  
  `POST /api/house/investments/buy` with `{"targetHouseId":2,"amount":"500.00"}`.  
  You cannot target your own house. Buying may push your cash negative (overdraft allowed).

- **Sell a position**: `POST /api/house/investments/{positionId}/sell`.

- **List positions**: `GET /api/house/investments?status=`  
  Omit `status` for all positions; use `OPEN` or `CLOSED` to filter.

Settlement uses target share prices at buy and sell: payout = principal × (price at sell ÷ price at buy). Losses can produce a negative payout.

## Tips

- Share price and holdings update as other houses move cash and as interest accrues.
- The public ticker and history endpoints help compare houses without logging in.
