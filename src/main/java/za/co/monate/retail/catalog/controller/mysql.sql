WITH RankedTransactions AS (
    SELECT
        CL.first_name,
        TRANS.amount,
        TRANS.transaction_date,
        -- THE MAGIC HAPPENS HERE:
        ROW_NUMBER() OVER(PARTITION BY  client_id ORDER BY transaction_date DESC) as rank
    FROM clients AS CL
             INNER JOIN transactions AS TRANS
                        ON CL.client_id = TRANS.client_id
)
-- Now we query our temporary "RankedTransactions" table
SELECT
    first_name,
    amount,
    transaction_date
FROM RankedTransactions
WHERE rank = 1;