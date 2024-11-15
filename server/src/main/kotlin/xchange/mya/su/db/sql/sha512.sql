-- AS SUPERUSER
CREATE EXTENSION "plpython3u";

CREATE OR REPLACE FUNCTION "sha512hash"("data" BYTEA)
	RETURNS BYTEA AS
$$
	from cryptography.hazmat.primitives import hashes
	digest = hashes.Hash(hashes.SHA512())
	digest.update(data)
	return digest.finalize()
$$ LANGUAGE "plpython3u";
