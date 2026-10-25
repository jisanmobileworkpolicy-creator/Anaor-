# JISAN MOBILE backend starter

## What this provides
- `GET /health`: confirms the server can query Supabase.
- `GET /api/products`: public product listing/search.
- `POST /api/admin/products`: create/update a product (requires `x-admin-token`).
- `DELETE /api/admin/products/:id`: delete a product (requires `x-admin-token`).
- `POST /api/orders`: validates product prices/stock on the server, stores the order and order items, and reduces stock atomically.

## Setup
1. In Supabase SQL Editor, run all of `schema.sql`.
2. In Render, set environment variables:
   - `SUPABASE_URL`
   - `SUPABASE_SERVICE_ROLE_KEY` (server only; never put it in the APK or GitHub)
   - `ADMIN_TOKEN` (long random secret)
3. Set build command to `npm install`, start command to `npm start`, and runtime to Node.
4. Deploy and test `/health`; it must say `{"status":"ok","database":"supabase"}`.
5. Add products through the admin API. Example:
   `curl -X POST https://YOUR-SERVICE.onrender.com/api/admin/products -H 'content-type: application/json' -H 'x-admin-token: YOUR_ADMIN_TOKEN' -d '{"id":"demo-1","name":"Demo Charger","price":250,"stock":10,"category":"Accessories"}'`

## Important
This is a backend starter, not a verified patch to the existing APK or GitHub repository. The existing app frontend must call these API endpoints and be tested separately. Never expose the Supabase service-role key or admin token in client-side code.
