-- AS SUPERUSER
CREATE EXTENSION "plpython3u";

CREATE OR REPLACE FUNCTION "ed25519_verify"("data" BYTEA, "signature" BYTEA, "public_key" BYTEA)
	RETURNS BOOLEAN AS
$$
	from cryptography.hazmat.primitives.asymmetric import ed25519
	try:
		k = ed25519.Ed25519PublicKey.from_public_bytes(public_key)
		k.verify(signature, data)
		return True
	except:
		return False
$$ LANGUAGE "plpython3u";