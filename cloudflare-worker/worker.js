const TGJU_ENDPOINTS = [
  "https://call.tgju.org/ajax.json",
  "https://call1.tgju.org/ajax.json",
  "https://call2.tgju.org/ajax.json",
  "https://call3.tgju.org/ajax.json",
];

const ASSETS = [
  { id: "usd", key: "price_dollar_rl" },
  { id: "usdt", key: "crypto-tether-irr" },
  { id: "gold18", key: "geram18" },
];

function priceInTomans(value, assetId) {
  const rials = Number(String(value ?? "").replace(/[\s,]/g, ""));
  if (!Number.isSafeInteger(rials) || rials < 10) {
    throw new Error(`Invalid ${assetId} price from source`);
  }
  return Math.floor(rials / 10);
}

async function fetchPrices() {
  let lastError;
  for (const endpoint of TGJU_ENDPOINTS) {
    try {
      const url = new URL(endpoint);
      url.searchParams.set("_", String(Date.now()));
      const response = await fetch(url, {
        headers: {
          Accept: "application/json",
          Referer: "https://www.tgju.org/",
          "User-Agent": "Mozilla/5.0 (compatible; ChandPrices/1.0)",
        },
      });
      if (!response.ok) throw new Error(`Source HTTP ${response.status}`);
      const data = await response.json();
      const current = data?.current;
      if (!current || typeof current !== "object") {
        throw new Error("Source did not include current prices");
      }
      return ASSETS.map(({ id, key }) => ({
        id,
        price_toman: priceInTomans(current[key]?.p, id),
      }));
    } catch (error) {
      lastError = error;
      console.warn(`Price source ${endpoint} failed: ${error.message}`);
    }
  }
  throw lastError ?? new Error("All price sources failed");
}

async function updatePrices(env) {
  const prices = await fetchPrices();
  const fetchedAt = new Date().toISOString();
  const statement = `
    INSERT INTO latest_prices (id, price_toman, source, fetched_at, source_time)
    VALUES (?, ?, 'TGJU', ?, NULL)
    ON CONFLICT(id) DO UPDATE SET
      price_toman = excluded.price_toman,
      source = excluded.source,
      fetched_at = excluded.fetched_at,
      source_time = excluded.source_time
  `;
  await env.DB.batch(
    prices.map(({ id, price_toman }) =>
      env.DB.prepare(statement).bind(id, price_toman, fetchedAt),
    ),
  );
  console.log(`Stored ${prices.length} prices at ${fetchedAt}`);
}

export default {
  async fetch(request, env) {
    if (new URL(request.url).pathname !== "/api/prices") {
      return new Response("Not found", { status: 404 });
    }
    if (request.method !== "GET") {
      return new Response("Method not allowed", { status: 405 });
    }
    const result = await env.DB.prepare(
      "SELECT id, price_toman, source, fetched_at, source_time FROM latest_prices ORDER BY id",
    ).all();
    return Response.json(
      { prices: result.results },
      { headers: { "Cache-Control": "no-store" } },
    );
  },

  async scheduled(_controller, env) {
    await updatePrices(env);
  },
};
