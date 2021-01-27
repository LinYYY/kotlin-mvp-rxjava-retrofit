package com.tech.mvpframework.utils.security

import java.nio.charset.Charset
import java.util.*


/**
 *  create by Myking
 *  date : 2020/5/7 15:46
 *  description :
 */
class Base64 protected constructor(
    unencodedBlockSize: Int,
    encodedBlockSize: Int,
    lineLength: Int,
    chunkSeparatorLength: Int
) {
    private lateinit var encodeTable: ByteArray
    private lateinit var decodeTable: ByteArray
    private var lineSeparator: ByteArray? = null
    private var decodeSize: Int = 0
    private var encodeSize = 0
    private val unencodedBlockSize: Int
    private val encodedBlockSize: Int
    protected var lineLength: Int
    private val chunkSeparatorLength: Int

    constructor(urlSafe: Boolean) : this(76, CHUNK_SEPARATOR, urlSafe) {}

    @JvmOverloads
    constructor(
        lineLength: Int = 0,
        lineSeparator: ByteArray? = CHUNK_SEPARATOR,
        urlSafe: Boolean = false
    ) : this(3, 4, lineLength, lineSeparator?.size ?: 0) {
        decodeTable = DECODE_TABLE
        if (lineSeparator != null) {
            if (containsAlphabetOrPad(lineSeparator)) {
                val sep = newStringUtf8(lineSeparator)
                throw IllegalArgumentException("lineSeparator must not contain base64 characters: [$sep]")
            }
            if (lineLength > 0) {
                encodeSize = 4 + lineSeparator.size
                this.lineSeparator = ByteArray(lineSeparator.size)
                System.arraycopy(
                    lineSeparator,
                    0,
                    this.lineSeparator,
                    0,
                    lineSeparator.size
                )
            } else {
                encodeSize = 4
                this.lineSeparator = null
            }
        } else {
            encodeSize = 4
            this.lineSeparator = null
        }
        decodeSize = encodeSize - 1
        encodeTable =
            if (urlSafe) URL_SAFE_ENCODE_TABLE else STANDARD_ENCODE_TABLE
    }

    protected fun containsAlphabetOrPad(arrayOctet: ByteArray?): Boolean {
        return if (arrayOctet == null) {
            false
        } else {
            val `arr$`: ByteArray = arrayOctet
            val `len$` = arrayOctet.size
            for (`i$` in 0 until `len$`) {
                val element = `arr$`[`i$`]
                if (61 == element.toInt() || isInAlphabet(element)) {
                    return true
                }
            }
            false
        }
    }

    protected fun isInAlphabet(octet: Byte): Boolean {
        return octet >= 0 && octet < decodeTable.size && decodeTable[octet.toInt()].toInt() != -1
    }

    protected fun ensureBufferSize(size: Int, context: Context): ByteArray? {
        return if (context.buffer != null && context.buffer!!.size >= context.pos + size) context.buffer else resizeBuffer(
            context
        )
    }

    private fun resizeBuffer(context: Context): ByteArray? {
        if (context.buffer == null) {
            context.buffer = ByteArray(getDefaultBufferSize())
            context.pos = 0
            context.readPos = 0
        } else {
            val b = ByteArray(context.buffer!!.size * 2)
            System.arraycopy(context.buffer!!, 0, b, 0, context.buffer!!.size)
            context.buffer = b
        }
        return context.buffer
    }

    protected fun getDefaultBufferSize(): Int {
        return 8192
    }

    fun encode(
        `in`: ByteArray,
        inPos: Int,
        inAvail: Int,
        context: Context
    ) {
        var inPos = inPos
        if (!context.eof) {
            if (inAvail < 0) {
                context.eof = true
                if (0 == context.modulus && lineLength == 0) {
                    return
                }
                val i = ensureBufferSize(encodeSize, context)
                val buffer = context.pos
                when (context.modulus) {
                    0 -> {
                    }
                    1 -> {
                        i!![context.pos++] =
                            encodeTable[context.ibitWorkArea shr 2 and 63]
                        i[context.pos++] =
                            encodeTable[context.ibitWorkArea shl 4 and 63]
                        if (encodeTable == STANDARD_ENCODE_TABLE) {
                            i[context.pos++] = 61
                            i[context.pos++] = 61
                        }
                    }
                    2 -> {
                        i!![context.pos++] =
                            encodeTable[context.ibitWorkArea shr 10 and 63]
                        i[context.pos++] =
                            encodeTable[context.ibitWorkArea shr 4 and 63]
                        i[context.pos++] =
                            encodeTable[context.ibitWorkArea shl 2 and 63]
                        if (encodeTable == STANDARD_ENCODE_TABLE) {
                            i[context.pos++] = 61
                        }
                    }
                    else -> throw IllegalStateException("Impossible modulus " + context.modulus)
                }
                context.currentLinePos += context.pos - buffer
                if (lineLength > 0 && context.currentLinePos > 0) {
                    System.arraycopy(
                        lineSeparator!!,
                        0,
                        i,
                        context.pos,
                        lineSeparator!!.size
                    )
                    context.pos += lineSeparator!!.size
                }
            } else {
                for (var8 in 0 until inAvail) {
                    val var9 = ensureBufferSize(encodeSize, context)
                    context.modulus = (context.modulus + 1) % 3
                    var b = `in`[inPos++].toInt()
                    if (b < 0) {
                        b += 256
                    }
                    context.ibitWorkArea = (context.ibitWorkArea shl 8) + b
                    if (0 == context.modulus) {
                        var9!![context.pos++] =
                            encodeTable[context.ibitWorkArea shr 18 and 63]
                        var9[context.pos++] =
                            encodeTable[context.ibitWorkArea shr 12 and 63]
                        var9[context.pos++] =
                            encodeTable[context.ibitWorkArea shr 6 and 63]
                        var9[context.pos++] = encodeTable[context.ibitWorkArea and 63]
                        context.currentLinePos += 4
                        if (lineLength > 0 && lineLength <= context.currentLinePos) {
                            System.arraycopy(
                                lineSeparator!!,
                                0,
                                var9,
                                context.pos,
                                lineSeparator!!.size
                            )
                            context.pos += lineSeparator!!.size
                            context.currentLinePos = 0
                        }
                    }
                }
            }
        }
    }

    fun encode(pArray: ByteArray?): ByteArray? {
        return if (pArray != null && pArray.size != 0) {
            val context = Context()
            this.encode(pArray, 0, pArray.size, context)
            this.encode(pArray, 0, -1, context)
            val buf = ByteArray(context.pos - context.readPos)
            readResults(buf, 0, buf.size, context)
            buf
        } else {
            pArray
        }
    }

    fun available(context: Context): Int {
        return if (context.buffer != null) context.pos - context.readPos else 0
    }

    fun readResults(b: ByteArray?, bPos: Int, bAvail: Int, context: Context): Int {
        return if (context.buffer != null) {
            val len = Math.min(available(context), bAvail)
            System.arraycopy(context.buffer!!, context.readPos, b, bPos, len)
            context.readPos += len
            if (context.readPos >= context.pos) {
                context.buffer = null
            }
            len
        } else {
            if (context.eof) -1 else 0
        }
    }

    fun getEncodedLength(pArray: ByteArray): Long {
        var len =
            ((pArray.size + unencodedBlockSize - 1) / unencodedBlockSize).toLong() * encodedBlockSize.toLong()
        if (lineLength > 0) {
            len += (len + lineLength.toLong() - 1L) / lineLength.toLong() * chunkSeparatorLength.toLong()
        }
        return len
    }

    class Context {
        var ibitWorkArea = 0
        var lbitWorkArea: Long = 0
        var buffer: ByteArray? = null
        var pos = 0
        var readPos = 0
        var eof = false
        var currentLinePos = 0
        var modulus = 0
        override fun toString(): String {
            return String.format(
                "%s[buffer=%s, currentLinePos=%s, eof=%s, ibitWorkArea=%s, lbitWorkArea=%s, modulus=%s, pos=%s, readPos=%s]",
                *arrayOf<Any>(
                    this.javaClass.simpleName,
                    Arrays.toString(buffer),
                    Integer.valueOf(currentLinePos),
                    java.lang.Boolean.valueOf(eof),
                    Integer.valueOf(ibitWorkArea),
                    java.lang.Long.valueOf(lbitWorkArea),
                    Integer.valueOf(modulus),
                    Integer.valueOf(pos),
                    Integer.valueOf(readPos)
                )
            )
        }
    }

    companion object {
        val CHUNK_SEPARATOR = byteArrayOf(13.toByte(), 10.toByte())
        private val STANDARD_ENCODE_TABLE = byteArrayOf(
            65.toByte(),
            66.toByte(),
            67.toByte(),
            68.toByte(),
            69.toByte(),
            70.toByte(),
            71.toByte(),
            72.toByte(),
            73.toByte(),
            74.toByte(),
            75.toByte(),
            76.toByte(),
            77.toByte(),
            78.toByte(),
            79.toByte(),
            80.toByte(),
            81.toByte(),
            82.toByte(),
            83.toByte(),
            84.toByte(),
            85.toByte(),
            86.toByte(),
            87.toByte(),
            88.toByte(),
            89.toByte(),
            90.toByte(),
            97.toByte(),
            98.toByte(),
            99.toByte(),
            100.toByte(),
            101.toByte(),
            102.toByte(),
            103.toByte(),
            104.toByte(),
            105.toByte(),
            106.toByte(),
            107.toByte(),
            108.toByte(),
            109.toByte(),
            110.toByte(),
            111.toByte(),
            112.toByte(),
            113.toByte(),
            114.toByte(),
            115.toByte(),
            116.toByte(),
            117.toByte(),
            118.toByte(),
            119.toByte(),
            120.toByte(),
            121.toByte(),
            122.toByte(),
            48.toByte(),
            49.toByte(),
            50.toByte(),
            51.toByte(),
            52.toByte(),
            53.toByte(),
            54.toByte(),
            55.toByte(),
            56.toByte(),
            57.toByte(),
            43.toByte(),
            47.toByte()
        )
        private val URL_SAFE_ENCODE_TABLE = byteArrayOf(
            65.toByte(),
            66.toByte(),
            67.toByte(),
            68.toByte(),
            69.toByte(),
            70.toByte(),
            71.toByte(),
            72.toByte(),
            73.toByte(),
            74.toByte(),
            75.toByte(),
            76.toByte(),
            77.toByte(),
            78.toByte(),
            79.toByte(),
            80.toByte(),
            81.toByte(),
            82.toByte(),
            83.toByte(),
            84.toByte(),
            85.toByte(),
            86.toByte(),
            87.toByte(),
            88.toByte(),
            89.toByte(),
            90.toByte(),
            97.toByte(),
            98.toByte(),
            99.toByte(),
            100.toByte(),
            101.toByte(),
            102.toByte(),
            103.toByte(),
            104.toByte(),
            105.toByte(),
            106.toByte(),
            107.toByte(),
            108.toByte(),
            109.toByte(),
            110.toByte(),
            111.toByte(),
            112.toByte(),
            113.toByte(),
            114.toByte(),
            115.toByte(),
            116.toByte(),
            117.toByte(),
            118.toByte(),
            119.toByte(),
            120.toByte(),
            121.toByte(),
            122.toByte(),
            48.toByte(),
            49.toByte(),
            50.toByte(),
            51.toByte(),
            52.toByte(),
            53.toByte(),
            54.toByte(),
            55.toByte(),
            56.toByte(),
            57.toByte(),
            45.toByte(),
            95.toByte()
        )
        private val DECODE_TABLE = byteArrayOf(
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            62.toByte(),
            (-1).toByte(),
            62.toByte(),
            (-1).toByte(),
            63.toByte(),
            52.toByte(),
            53.toByte(),
            54.toByte(),
            55.toByte(),
            56.toByte(),
            57.toByte(),
            58.toByte(),
            59.toByte(),
            60.toByte(),
            61.toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            0.toByte(),
            1.toByte(),
            2.toByte(),
            3.toByte(),
            4.toByte(),
            5.toByte(),
            6.toByte(),
            7.toByte(),
            8.toByte(),
            9.toByte(),
            10.toByte(),
            11.toByte(),
            12.toByte(),
            13.toByte(),
            14.toByte(),
            15.toByte(),
            16.toByte(),
            17.toByte(),
            18.toByte(),
            19.toByte(),
            20.toByte(),
            21.toByte(),
            22.toByte(),
            23.toByte(),
            24.toByte(),
            25.toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            (-1).toByte(),
            63.toByte(),
            (-1).toByte(),
            26.toByte(),
            27.toByte(),
            28.toByte(),
            29.toByte(),
            30.toByte(),
            31.toByte(),
            32.toByte(),
            33.toByte(),
            34.toByte(),
            35.toByte(),
            36.toByte(),
            37.toByte(),
            38.toByte(),
            39.toByte(),
            40.toByte(),
            41.toByte(),
            42.toByte(),
            43.toByte(),
            44.toByte(),
            45.toByte(),
            46.toByte(),
            47.toByte(),
            48.toByte(),
            49.toByte(),
            50.toByte(),
            51.toByte()
        )
        val UTF_8 = Charset.forName("UTF-8")
        fun newStringUtf8(bytes: ByteArray?): String? {
            return bytes?.let { String(it, UTF_8) }
        }

        fun encodeBase64URLSafeString(binaryData: ByteArray?): String? {
            return newStringUtf8(encodeBase64(binaryData, false, true))
        }

        @JvmOverloads
        fun encodeBase64(
            binaryData: ByteArray?,
            isChunked: Boolean,
            urlSafe: Boolean,
            maxResultSize: Int = 2147483647
        ): ByteArray? {
            return if (binaryData != null && binaryData.size != 0) {
                val b64 = if (isChunked) Base64(urlSafe) else Base64(
                    0,
                    CHUNK_SEPARATOR,
                    urlSafe
                )
                val len = b64.getEncodedLength(binaryData)
                if (len > maxResultSize.toLong()) {
                    throw IllegalArgumentException("Input array too big, the output array would be bigger ($len) than the specified maximum size of $maxResultSize")
                } else {
                    b64.encode(binaryData)
                }
            } else {
                binaryData
            }
        }
    }

    init {
        this.unencodedBlockSize = unencodedBlockSize
        this.encodedBlockSize = encodedBlockSize
        val useChunking = lineLength > 0 && chunkSeparatorLength > 0
        this.lineLength = if (useChunking) lineLength / encodedBlockSize * encodedBlockSize else 0
        this.chunkSeparatorLength = chunkSeparatorLength
    }
}
