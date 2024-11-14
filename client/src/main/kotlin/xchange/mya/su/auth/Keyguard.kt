package xchange.mya.su.auth

import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemReader
import org.bouncycastle.util.io.pem.PemWriter
import java.io.File
import java.security.SecureRandom

object Keyguard {
	private const val PRIVATE_KEY = "PRIVATE_KEY"

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


	fun load(): Pair<Ed25519PrivateKeyParameters, Ed25519PublicKeyParameters>? {
		val keyFile = keyFile()
		if (!keyFile.exists()) {
			return null
		}

		val pemReader = PemReader(keyFile.reader())
		val pemObject = pemReader.readPemObject()
		pemReader.close()

		if (pemObject == null) {
			return null
		}

		if (pemObject.type != PRIVATE_KEY) {
			throw IllegalArgumentException("Invalid PEM type: ${pemObject.type}")
		}

		val private = Ed25519PrivateKeyParameters(pemObject.content)
		val public = private.generatePublicKey()

		return private to public
	}

	fun save(privateKey: Ed25519PrivateKeyParameters) {
		val pemObject = PemObject(PRIVATE_KEY, privateKey.encoded)
		val keyFile = keyFile()

		val pemWriter = PemWriter(keyFile.writer())
		pemWriter.writeObject(pemObject)
		pemWriter.close()
	}

	fun createPair(): Pair<Ed25519PrivateKeyParameters, Ed25519PublicKeyParameters> {
		val random = SecureRandom()
		val generator = Ed25519KeyPairGenerator()
		val parameters = Ed25519KeyGenerationParameters(random)
		generator.init(parameters)

		val pair = generator.generateKeyPair()
		val private = pair.private as Ed25519PrivateKeyParameters
		val public = pair.public as Ed25519PublicKeyParameters

		return private to public
	}
}