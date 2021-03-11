package com.app.githubuserrepo.utils

import android.util.Log
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.HttpURLConnection
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class FileDownloader(okHttpClient: OkHttpClient) {

    companion object {

        private const val BUFFER_LENGTH_BYTES = 1024 * 3
        private const val HTTP_TIMEOUT = 60

        private val algorithm = "AES"


        @Throws(NoSuchAlgorithmException::class)
        fun generateKey(): SecretKey? {
            // Generate a 256-bit key
            val outputKeyLength = 256
            val secureRandom = SecureRandom()
            // Do *not* seed secureRandom! Automatically seeded from system entropy.
            val keyGenerator: KeyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(outputKeyLength, secureRandom)
            val yourKey = keyGenerator.generateKey()
            return yourKey
        }

        @Throws(Exception::class)
        fun encodeFile(yourKey: SecretKey?, fileData: ByteArray?): ByteArray? {
            var encrypted: ByteArray? = null
            val data: ByteArray = yourKey?.getEncoded()!!
            val skeySpec = SecretKeySpec(data, 0, data.size, algorithm)
            val cipher: Cipher = Cipher.getInstance(algorithm)
            cipher.init(
                Cipher.ENCRYPT_MODE,
                skeySpec,
                IvParameterSpec(ByteArray(cipher.getBlockSize()))
            )
            encrypted = cipher.doFinal(fileData)
            return encrypted
        }

        @Throws(Exception::class)
        fun decodeFile(yourKey: SecretKey?, fileData: ByteArray?): ByteArray? {
            return try {

                var decrypted: ByteArray? = null
                val cipher: Cipher = Cipher.getInstance(algorithm)
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    yourKey,
                    IvParameterSpec(ByteArray(cipher.blockSize))
                )
                decrypted = cipher.doFinal(fileData)
                decrypted
            }catch (e:Exception){
                null
            }
        }
    }


    private var okHttpClient: OkHttpClient

    init {
        val okHttpBuilder = okHttpClient.newBuilder()
            .connectTimeout(HTTP_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(HTTP_TIMEOUT.toLong(), TimeUnit.SECONDS)
        this.okHttpClient = okHttpBuilder.build()
    }

    fun download(url: String, file: File, yourKey: SecretKey?): Observable<Int> {
        return Observable.create<Int> { emitter ->
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body()
            val responseCode = response.code()
            if (responseCode >= HttpURLConnection.HTTP_OK &&
                responseCode < HttpURLConnection.HTTP_MULT_CHOICE &&
                body != null
            ) {
                val length = body.contentLength()
                body.byteStream().apply {
                    file.outputStream().use { fileOut ->
                        var bytesCopied = 0
                        val buffer = ByteArray(BUFFER_LENGTH_BYTES)
                        var bytes = read(buffer)
                        while (bytes >= 0) {
                            fileOut.write(buffer, 0, bytes)
                            bytesCopied += bytes
                            bytes = read(buffer)
                            if(length > 0) {
                                val dl_progress: Int = (bytesCopied * 1f / length * 100).toInt()
//                                emitter.onNext(((bytesCopied * 100) / length).toInt())
                                emitter.onNext(dl_progress)
                                Log.e("====", "download: " + ((bytesCopied * 100) / length).toInt())
                            }
                        }
//                        fileOut.flush()
//                        fileOut.close()
                    }

                    emitter.onComplete()

                }
            } else {
                throw IllegalArgumentException("Error occurred when do http get $url")
            }
        }
    }
}
