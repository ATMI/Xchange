package xchange.mya.su

import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemReader
import org.bouncycastle.util.io.pem.PemWriter
import java.io.File
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

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

	private fun keyFile(): File {
		val storage = keyStorage()
		val keyFile = File(storage, "key.pem")
		return keyFile
	}


	fun load(): AsymmetricCipherKeyPair? {
		val keyFile = keyFile()
		if (!keyFile.exists()) {
			return null
		}
		return null
	}

	fun save(privateKey: Ed25519PrivateKeyParameters) {
		val pemObject = PemObject(PRIVATE_KEY, privateKey.encoded)
		val keyFile = keyFile()

		val pemWriter = PemWriter(keyFile.writer())
		pemWriter.writeObject(pemObject)
		pemWriter.close()
	}

	fun createPair(): KeyPair {
		val random = SecureRandom()
		val generator = Ed25519KeyPairGenerator()

		generator.init(Ed25519KeyGenerationParameters(random))
		val pair = generator.generateKeyPair()

		val private = pair.private as Ed25519PrivateKeyParameters
		val public = pair.public as Ed25519PublicKeyParameters

		return KeyPair(private, public)
	}
}