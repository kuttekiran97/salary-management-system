-- Replace the approximate rates from V2 with published, dated ones.
-- Source: European Central Bank euro foreign exchange reference rates for 2026-10-19
-- (https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml).
-- The ECB quotes units per EUR, so USD per unit = EUR/USD rate / EUR/<currency> rate,
-- with EUR/USD = 1.1225. Rounded to 6 decimal places.
ALTER TABLE country ADD COLUMN rate_as_of TEXT NOT NULL DEFAULT '2026-10-19';  -- yyyy-MM-dd

UPDATE country SET usd_rate = 1.0      WHERE code = 'US';
UPDATE country SET usd_rate = 0.010534 WHERE code = 'IN';  -- 1.1225 / 106.5590
UPDATE country SET usd_rate = 1.333056 WHERE code = 'GB';  -- 1.1225 / 0.84205
UPDATE country SET usd_rate = 1.1225   WHERE code = 'DE';  -- 1.1225 / 1
UPDATE country SET usd_rate = 0.710533 WHERE code = 'CA';  -- 1.1225 / 1.5798
UPDATE country SET usd_rate = 0.698593 WHERE code = 'AU';  -- 1.1225 / 1.6068
UPDATE country SET usd_rate = 0.781849 WHERE code = 'SG';  -- 1.1225 / 1.4357
