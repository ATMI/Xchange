CREATE OR REPLACE FUNCTION "transaction_verify_signature"(
	"transaction" "transaction"
)
	RETURNS BOOLEAN AS
$$
DECLARE
	"key"  BYTEA;
	"data" BYTEA;
BEGIN
	"key" := (SELECT "public_key" FROM "client" WHERE "id" = "transaction"."sender");
	IF "key" IS NULL THEN
		RETURN FALSE;
	END IF;

	"data" := format(
		'[%s, %s] %s -> %s: %s $%s',
		"transaction"."id",
		(EXTRACT(EPOCH FROM "transaction"."timestamp") * 1000)::bigint,
		"transaction"."sender",
		"transaction"."recipient",
		"transaction"."amount",
		"transaction"."currency"
			  )::bytea;
	RETURN "ed25519_verify"("data", "transaction"."signature", "key");
END;
$$ LANGUAGE "plpgsql";

CREATE OR REPLACE FUNCTION "transaction_verify_signature_trig_fn"()
	RETURNS TRIGGER AS
$$
BEGIN
	IF "transaction_verify_signature"("new") THEN
		RETURN "new";
	END IF;
	RAISE EXCEPTION 'Insertion would lead to unverified transaction';
END;
$$ LANGUAGE "plpgsql";

CREATE OR REPLACE TRIGGER "transaction_verify_signature_trig"
	BEFORE INSERT
	ON "transaction"
	FOR EACH ROW
EXECUTE FUNCTION "transaction_verify_signature_trig_fn"();
