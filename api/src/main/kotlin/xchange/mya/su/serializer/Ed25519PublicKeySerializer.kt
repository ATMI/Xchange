package xchange.mya.su.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.serializer
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters

object Ed25519PublicKeySerializer : KSerializer<Ed25519PublicKeyParameters> {
	override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Ed25519PublicKeyParameters") {
		element<ByteArray>("encoded")
	}

	override fun serialize(encoder: Encoder, value: Ed25519PublicKeyParameters) {
		encoder.encodeStructure(descriptor) {
			encodeSerializableElement(descriptor, 0, serializer<ByteArray>(), value.encoded)
		}
	}

	override fun deserialize(decoder: Decoder): Ed25519PublicKeyParameters {
		return decoder.decodeStructure(descriptor) {
			val encoded: ByteArray
			when (val index = decodeElementIndex(descriptor)) {
				0 -> encoded = decodeSerializableElement(descriptor, 0, serializer<ByteArray>())
				else -> throw SerializationException("Unknown index $index")
			}
			Ed25519PublicKeyParameters(encoded)
		}
	}
}
