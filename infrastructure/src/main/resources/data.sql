INSERT INTO account_status (account_status_id, name, description)
VALUES(1,'PENDING_ACTIVATION', 'The client has not activated the account.')
      , (2,'ACTIVE', '')
      , (3,'BLOCKED', '')
      , (4,'DORMANT', 'This account has not being used in a period of time')
      , (5,'CLOSED', '')
ON CONFLICT(name) DO NOTHING;@@

INSERT INTO account_type (account_type_id, name, description)
VALUES(1,'SAVINGS', '')
      , (2,'CHECKING', '')
      , (3,'TIME_DEPOSIT', '')
ON CONFLICT(name) DO NOTHING;@@

INSERT INTO transaction_type (transaction_type_id, name, description)
VALUES(1,'CASH_DEPOSIT', '')
     , (2,'TRANSFER_INBOUND', '')
     , (3,'CASH_WITHDRAWAL', '')
     , (4,'TRANSFER_OUTBOUND', '')
ON CONFLICT(name) DO NOTHING;@@





