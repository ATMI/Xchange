CREATE TYPE "demand" AS
(
	"id"     BIGINT,
	"client" BIGINT,
	"amount" BIGINT,
	"rate"   BIGINT
);


CREATE OR REPLACE FUNCTION "order_buy"("base_id" BIGINT, "quote_id" BIGINT)
	RETURNS SETOF "demand"
AS
$$
BEGIN
	RETURN QUERY
		SELECT "id", "client", "amount" - "order_satisfied"("order") AS "amount", "rate"
		FROM "order"
		WHERE "base" = "base_id"
		  AND "quote" = "quote_id"
		  AND "amount" - "order_satisfied"("order") > 0
		ORDER BY "rate" DESC;
END ;
$$ LANGUAGE "plpgsql";


CREATE OR REPLACE FUNCTION "order_satisfied"(o "order")
	RETURNS BIGINT
AS
$$
DECLARE
	"amount" BIGINT;
BEGIN
	SELECT COALESCE(SUM("t"."amount"), 0)
	INTO "amount"
	FROM "order_transaction" "ot"
			 JOIN "transaction" "t" ON "ot"."transaction" = "t"."id"
	WHERE "ot"."order" = "o"."id"
	  AND "t"."recipient" = "o"."client";
	RETURN "amount";
END;
$$ LANGUAGE "plpgsql";


SELECT "order"."id", "order"."amount", "order"."rate", "order_satisfied"("order")
FROM "order";
