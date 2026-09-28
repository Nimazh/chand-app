import assert from "node:assert/strict";
import { afterEach, test } from "node:test";
import worker from "./worker.js";

const originalFetch = globalThis.fetch;
afterEach(() => {
  globalThis.fetch = originalFetch;
});

function fakeDatabase() {
  const writes = [];
  return {
    writes,
    prepare(sql) {
      return {
        bind(...values) {
          return { sql, values };
        },
        async all() {
          return { results: writes.map(({ values }) => ({ id: values[0], price_toman: values[1] })) };
        },
      };
    },
    async batch(statements) {
      writes.push(...statements);
    },
  };
}

test("scheduled refresh saves all three rial quotes as toman and the API serves them", async () => {
  globalThis.fetch = async () => Response.json({
    current: {
      price_dollar_rl: { p: "1,000,000" },
      "crypto-tether-irr": { p: "980,000" },
      geram18: { p: "75,000,000" },
    },
  });
  const DB = fakeDatabase();

  await worker.scheduled(null, { DB });
  const response = await worker.fetch(new Request("https://example.test/api/prices"), { DB });

  assert.equal(response.status, 200);
  assert.deepEqual((await response.json()).prices, [
    { id: "usd", price_toman: 100000 },
    { id: "usdt", price_toman: 98000 },
    { id: "gold18", price_toman: 7500000 },
  ]);
});

test("invalid source data never partially overwrites saved prices", async () => {
  globalThis.fetch = async () => Response.json({ current: { price_dollar_rl: { p: "1,000,000" } } });
  const DB = fakeDatabase();

  await assert.rejects(() => worker.scheduled(null, { DB }), /Invalid usdt price/);
  assert.equal(DB.writes.length, 0);
});
