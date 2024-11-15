package xchange.mya.su

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import xchange.mya.su.entity.Client
import java.io.File
import java.security.SecureRandom

object Keyguard {
	private const val PRIVATE_KEY = "PRIVATE_KEY"

	data class KeyPair(
		val private: Ed25519PrivateKeyParameters,
		val public: Ed25519PublicKeyParameters,
	)

	private fun keyStorage(): File {
		val userHome = System.getProperty("user.home")
		val appDir = File(userHome, ".xchange")

		if (!appDir.exists()) {
			appDir.mkdirs()
		}

		return appDir
	}

	private fun loadKey(file: File) = runCatching {
		val id = file.nameWithoutExtension.toLong()
		val bytes = ByteArray(32)

		file.inputStream().use { stream ->
			stream.read(bytes, 0, bytes.size)
		}

		val key = Ed25519PrivateKeyParameters(bytes)
		id to key
	}

	suspend fun list(): List<Client> = withContext(Dispatchers.IO) {
		val storage = keyStorage()
		if (!storage.exists()) {
			return@withContext emptyList()
		}

		val files = storage
			.listFiles { path ->
				path.extension == "xusr"
			}
			?.take(10)
			?: return@withContext emptyList()

		files
			.mapNotNull { file ->
				loadKey(file).getOrNull()
			}
			.map { (id, privateKey) ->
				val publicKey = privateKey.generatePublicKey()
				Client(
					id = id,
					privateKey = privateKey,
					publicKey = publicKey,
				)
			}
	}

	suspend fun save(id: Long, key: Ed25519PrivateKeyParameters) = withContext(Dispatchers.IO) {
		val storage = keyStorage()
		val file = File(storage, "$id.xusr")

		val bytes = key.encoded
		file.writeBytes(bytes)
	}

	suspend fun createPair(): KeyPair = withContext(Dispatchers.IO) {
		val random = SecureRandom()
		val generator = Ed25519KeyPairGenerator()

		generator.init(Ed25519KeyGenerationParameters(random))
		val pair = generator.generateKeyPair()

		val private = pair.private as Ed25519PrivateKeyParameters
		val public = pair.public as Ed25519PublicKeyParameters

		KeyPair(private, public)
	}
}