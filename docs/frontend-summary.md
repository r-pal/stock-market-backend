## Frontend integration summary

### What to store after login

- **Always store**: `accessToken` (JWT) from `POST /api/banker/auth/login` or `POST /api/house/auth/login`.
- **To determine user type**:
  - Decode the JWT claim **`role`**:
    - `BANKER` → banker UI
    - `HOUSE_PORTAL` → house UI
- **If house user**:
  - Decode the JWT claim **`houseId`** (also duplicated in the subject `house:<id>`).
  - Use it to label the session and to call `GET /api/house/account` (server also derives house id from token).

### Core screens → API calls

- **Login (banker)**:
  - `POST /api/banker/auth/login`
- **Login (house)**:
  - `POST /api/house/auth/login`
- **Public scoreboard / overview**:
  - `GET /api/public/accounts`
  - `GET /api/public/ticker`
- **Market / stock picker**:
  - `GET /api/public/stocks` (or `GET /api/house/stocks` if you prefer calling only authenticated endpoints once logged in)
- **House portfolio / positions**:
  - `GET /api/house/investments?status=OPEN`
  - `GET /api/house/investments?status=CLOSED`
- **House trade**:
  - Buy: `POST /api/house/investments/buy` with `{ "stockId": <id>, "amount": "..." }`
  - Sell: `POST /api/house/investments/{positionId}/sell`
- **Banker pawn shop (manage item stocks)**:
  - List: `GET /api/banker/pawn-shop/stocks`
  - Create item stock: `POST /api/banker/pawn-shop/stocks`
  - Reprice item stock: `PUT /api/banker/pawn-shop/stocks/{id}/price`

### Trading notes

- House-backed stocks (`stockType="HOUSE"`) are priced by the backend’s computed share-price formula.
- Item stocks (`stockType="ITEM"`) use the banker’s manual `currentPrice`.
- Positions include `stockId`, `stockType`, and `stockDisplayName` so the UI can render “Onions” vs “Laura” without extra lookups.

### Error handling UX

- Treat **401/403** as “session expired / wrong user type” → clear token, return to login.
- Treat **400** as user-facing validation/game-rule message; banker UI can show raw message, house UI may want friendlier text.

