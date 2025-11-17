DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS balances;
DROP TABLE IF EXISTS accounts;

CREATE TABLE accounts (
                          id          BIGSERIAL PRIMARY KEY NOT NULL,
                          customer_id BIGSERIAL NOT NULL,
                          country     VARCHAR(2) NOT NULL,
                          created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                          updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE balances (
                          id               BIGSERIAL PRIMARY KEY NOT NULL,
                          account_id       BIGSERIAL NOT NULL REFERENCES accounts(id),
                          currency         VARCHAR(3) NOT NULL,
                          available_amount NUMERIC(20,2) NOT NULL DEFAULT 0,
                          created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
                          updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
                          CONSTRAINT unique_balances_currency_account UNIQUE (account_id, currency)
);

CREATE TABLE transactions (
                              id            BIGSERIAL PRIMARY KEY NOT NULL,
                              account_id    BIGSERIAL NOT NULL REFERENCES accounts(id),
                              currency      VARCHAR(3) NOT NULL,
                              amount        NUMERIC(20,2) NOT NULL CHECK (amount >= 0),
                              direction     VARCHAR NOT NULL,
                              description   VARCHAR(255) NOT NULL,
                              created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                              updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_account_created ON transactions(account_id, created_at DESC);
