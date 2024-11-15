CREATE OR REPLACE FUNCTION "select_balance"("client_id" INTEGER, "currency_id" INTEGER)
	RETURNS INTEGER AS
$$
DECLARE
	"result" INTEGER;
BEGIN
	SELECT COALESCE(SUM(CASE WHEN "recipient" = "client_id" THEN "amount" ELSE 0 END), 0) -
		   COALESCE(SUM(CASE WHEN "sender" = "client_id" THEN "amount" ELSE 0 END), 0)
	INTO "result"
	FROM "transaction"
	WHERE ("recipient" = "client_id" OR "sender" = "client_id")
	  AND "currency" = "currency_id";
	RETURN "result";
END;
$$ LANGUAGE "plpgsql";

CREATE OR REPLACE FUNCTION "transaction_positive_balance_trig_fn"()
	RETURNS TRIGGER AS
$$
DECLARE
	"transaction_balance" INTEGER;
BEGIN
	IF "new"."sender" != 0 THEN
		"transaction_balance" := "select_balance"("new"."sender", "new"."currency");
		IF "transaction_balance" - "new"."amount" < 0
		THEN
			RAISE EXCEPTION 'Insert operation would lead to a negative balance';
		END IF;
	END IF;

	RETURN "new";
END;
$$ LANGUAGE "plpgsql";

CREATE OR REPLACE TRIGGER "transaction_positive_balance_trig"
	BEFORE INSERT
	ON "transaction"
	FOR EACH ROW
EXECUTE FUNCTION "transaction_positive_balance_trig_fn"();
