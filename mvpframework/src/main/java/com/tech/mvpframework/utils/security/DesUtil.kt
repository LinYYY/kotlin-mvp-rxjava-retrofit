package com.tech.mvpframework.utils.security

import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec

/**
 *  create by Myking
 *  date : 2020/4/26 14:12
 *  description :
 */
class DesUtil {

    companion object {

        /**
         * Des加密
         * @return base64 encode String
         */
        fun encrypt(data: ByteArray, key: String): String {
            val desKey = DESKeySpec(key.toByteArray())
            val keyFactory = SecretKeyFactory.getInstance("DES")
            val secureKey = keyFactory.generateSecret(desKey)
            val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secureKey)
            return Base64.encodeBase64URLSafeString(cipher.doFinal(data)).orEmpty()
        }

        /**
         * Des解密
         */
        fun decrypt(data: ByteArray, key: String): String {
            val desKey = DESKeySpec(key.toByteArray())
            val keyFactory = SecretKeyFactory.getInstance("DES")
            val secureKey = keyFactory.generateSecret(desKey)
            val cipher = Cipher.getInstance("DES")
            cipher.init(Cipher.DECRYPT_MODE, secureKey)
            return String(
                cipher.doFinal(
                    android.util.Base64.decode(
                        data,
                        android.util.Base64.DEFAULT
                    )
                )
            )
        }


        /**
         * Des加密
         *
         * @return original encrypted data
         */
        @JvmStatic
        fun encrypt(data: String, key: ByteArray): ByteArray {
            val desKey = DESKeySpec(key.copyOf(8))
            val keyFactory = SecretKeyFactory.getInstance("DES")
            val secureKey = keyFactory.generateSecret(desKey)
            val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secureKey)
            return cipher.doFinal(data.toByteArray())
        }

        /**
         * Des解密
         */
        @JvmStatic
        fun decrypt(src: ByteArray, key: ByteArray): String {
            val desKey = DESKeySpec(key.copyOf(8))
            val keyFactory = SecretKeyFactory.getInstance("DES")
            val secureKey = keyFactory.generateSecret(desKey)
            val cipher = Cipher.getInstance("DES")
            cipher.init(Cipher.DECRYPT_MODE, secureKey)
            return String(cipher.doFinal(src))
        }
    }
}