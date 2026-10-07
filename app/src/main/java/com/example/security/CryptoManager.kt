package com.example.security

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    // 256-bit AES master key derived for device-secured environmental audit records
    private val AES_KEY_BYTES = byteArrayOf(
        0x1B, 0x4D, 0x3E, 0x55, 0x7A, 0x22, 0x19, 0x64,
        0x3C, 0x7E, 0x01, 0x48, 0x59, 0x6F, 0x33, 0x1A,
        0x2B, 0x18, 0x49, 0x73, 0x52, 0x65, 0x63, 0x6F,
        0x72, 0x64, 0x5F, 0x4D, 0x54, 0x5F, 0x32, 0x36
    )
    private val FIXED_IV = byteArrayOf(
        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
        0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10
    )

    private val secretKey = SecretKeySpec(AES_KEY_BYTES, "AES")
    private val ivSpec = IvParameterSpec(FIXED_IV)

    /**
     * Encrypts bitmap bytes and saves to an encrypted file (.enc) in internal storage.
     * Returns the absolute file path and SHA-256 hash of original bytes.
     */
    fun encryptAndSaveBitmap(context: Context, bitmap: Bitmap, filenamePrefix: String): Pair<String, String> {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val originalBytes = stream.toByteArray()
        val sha256 = calculateSha256(originalBytes)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        val encryptedBytes = cipher.doFinal(originalBytes)

        val secureDir = File(context.filesDir, "encrypted_inspections")
        if (!secureDir.exists()) {
            secureDir.mkdirs()
        }

        val targetFile = File(secureDir, "${filenamePrefix}_${System.currentTimeMillis()}.enc")
        FileOutputStream(targetFile).use { it.write(encryptedBytes) }

        return Pair(targetFile.absolutePath, sha256)
    }

    /**
     * Decrypts an encrypted photo file back to a Bitmap for display or PDF generation.
     */
    fun decryptBitmap(encryptedFilePath: String): Bitmap? {
        return try {
            val file = File(encryptedFilePath)
            if (!file.exists()) return null

            val encryptedBytes = FileInputStream(file).use { it.readBytes() }
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            val decryptedBytes = cipher.doFinal(encryptedBytes)

            BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Calculates SHA-256 hexadecimal hash string for chain-of-custody integrity verification.
     */
    fun calculateSha256(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Calculates SHA-256 for a string payload.
     */
    fun calculateStringSha256(content: String): String {
        return calculateSha256(content.toByteArray(Charsets.UTF_8))
    }
}
