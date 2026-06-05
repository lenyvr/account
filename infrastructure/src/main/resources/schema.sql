-- Functions
CREATE OR REPLACE FUNCTION public.fill_account_backup_fn()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
BEGIN
    INSERT INTO public.account_backup
    (account_id, account_number, initial_amount
    , account_status_name, client_id, account_type_name, created_date
    , last_status_date, last_change_date, date_inserted, expiry_deposit_date)
    VALUES( OLD.account_id, OLD.account_number, OLD.initial_amount
          , (select name from account_status where account_status_id = OLD.account_status_id)
          , OLD.client_id
          , (select name from account_type where account_type_id = OLD.account_type_id)
          , OLD.created_date, OLD.last_status_date, OLD.last_change_date, now(), OLD.expiry_deposit_date);

    RETURN NEW;
END;
$function$;

CREATE OR REPLACE FUNCTION public.update_account_dates_fn()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
BEGIN
    IF NEW.initial_amount IS DISTINCT FROM OLD.initial_amount
		OR NEW.amount IS DISTINCT FROM OLD.amount
		OR NEW.account_status_id IS DISTINCT FROM OLD.account_status_id
	THEN
        NEW.last_change_date := NOW();
    END IF;

	IF NEW.account_status_id IS DISTINCT FROM OLD.account_status_id THEN
        NEW.last_status_date := NOW();
    END IF;

    RETURN NEW;
END;
$function$;

CREATE OR REPLACE FUNCTION fill_expiry_deposit_date_fn()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
BEGIN
    IF NEW.account_type_id = 3 AND NEW.expiry_deposit_date IS NULL THEN
        NEW.expiry_deposit_date := NOW() + INTERVAL '1 month';
    ELSE
        NEW.expiry_date := NULL;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Tables
CREATE TABLE IF NOT EXISTS account_type (
     account_type_id serial4 NOT NULL,
     name varchar(20) NOT NULL,
     description varchar(100) NULL,
     CONSTRAINT account_type_pk PRIMARY KEY (account_type_id),
     CONSTRAINT account_type_unique UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS account_status (
   account_status_id serial4 NOT NULL,
   name varchar(20) NOT NULL,
   description(100) varchar NULL,
   CONSTRAINT account_status_pk PRIMARY KEY (account_status_id),
   CONSTRAINT account_status_unique UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS account_backup (
   account_backup_id serial4 NOT NULL,
   account_id int4 NOT NULL,
   account_number varchar(34) NOT NULL,
   initial_amount numeric DEFAULT 0 NOT NULL,
   amount numeric DEFAULT 0 NOT NULL,
   account_status_name varchar(20) NOT NULL,
   client_id int4 NOT NULL,
   account_type_name varchar(20) NOT NULL,
   expiry_deposit_date date NULL,
   created_date date DEFAULT now() NOT NULL,
   last_status_date date NOT NULL,
   last_change_date date NOT NULL,
   date_inserted date DEFAULT now() NOT NULL
);

CREATE TABLE IF NOT EXISTS transaction_type (
     transaction_type_id serial4 NOT NULL,
     name varchar(20) NOT NULL,
     description varchar(100) NULL,
     CONSTRAINT transaction_type_pk PRIMARY KEY (transaction_type_id),
     CONSTRAINT transaction_type_unique UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS account (
    account_id serial4 NOT NULL,
    account_number varchar(34) NOT NULL,
    initial_amount numeric DEFAULT 0 NOT NULL,
    amount numeric DEFAULT 0 NOT NULL,
    account_status_id int4 NOT NULL,
    client_id int4 NOT NULL,
    account_type_id int4 NOT NULL,
    expiry_deposit_date date NULL,
    created_date date DEFAULT now() NOT NULL,
    last_status_date date DEFAULT now() NOT NULL,
    last_change_date date DEFAULT now() NOT NULL,
    CONSTRAINT account_pk PRIMARY KEY (account_id),
    CONSTRAINT account_unique UNIQUE (account_number),
    CONSTRAINT account_account_status_fk FOREIGN KEY (account_status_id) REFERENCES public.account_status(account_status_id),
    CONSTRAINT account_account_type_fk FOREIGN KEY (account_type_id) REFERENCES public.account_type(account_type_id)
);

CREATE TABLE IF NOT EXISTS transaction (
      transaction_id serial4 NOT NULL,
      transaction_date date DEFAULT now() NOT NULL,
      amount numeric NOT NULL,
      balance numeric NOT NULL,
      transaction_type_id int4 NOT NULL,
      account_id int4 NULL,
      CONSTRAINT transaction_pk PRIMARY KEY (transaction_id),
      CONSTRAINT transaction_account_fk FOREIGN KEY (account_id) REFERENCES public.account(account_id),
      CONSTRAINT transaction_transaction_type_fk FOREIGN KEY (transaction_type_id) REFERENCES public.transaction_type(transaction_type_id)
);

-- Table Triggers
create or replace trigger update_account_date_tg before update
    on account for each row execute function update_account_dates_fn();
create or replace trigger fill_account_backup_tg before update
    on account for each row execute function fill_account_backup_fn();
create or replace trigger fill_expiry_deposit_date_tg before insert
    on account or each row execute FUNCTION fill_expiry_deposit_date_fn();

-- Stored procedure
CREATE OR REPLACE PROCEDURE check_and_set_dormant_accounts()
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE account
    SET account_status_id = 4 -- 4 = DORMANT
    WHERE account_status_id <> 4
    AND last_change_date <= NOW() - INTERVAL '6 months';
    COMMIT;
END;
$$;

