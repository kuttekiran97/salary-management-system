-- Fixed exchange rates (approximate). Fixed rates keep insights reproducible;
-- live rates are deliberately out of scope (see docs/requirements.md).
INSERT INTO country (code, name, currency_code, usd_rate) VALUES
    ('US', 'United States',  'USD', 1.0),
    ('IN', 'India',          'INR', 0.012),
    ('GB', 'United Kingdom', 'GBP', 1.27),
    ('DE', 'Germany',        'EUR', 1.08),
    ('CA', 'Canada',         'CAD', 0.73),
    ('AU', 'Australia',      'AUD', 0.66),
    ('SG', 'Singapore',      'SGD', 0.74);
