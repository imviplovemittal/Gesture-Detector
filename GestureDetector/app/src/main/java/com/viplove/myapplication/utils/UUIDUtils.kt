package com.viplove.myapplication.utils

import java.lang.StringBuilder
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import kotlin.math.abs

class IDGenerator {

    companion object {
        private val ID_VALUES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray()

        private var secureRandom: SecureRandom? = null

        init {
            try {
                secureRandom = SecureRandom.getInstanceStrong();
            } catch (e: NoSuchAlgorithmException) {
                throw ExceptionInInitializerError(e);
            }
        }

        private fun generate(sb: StringBuilder): StringBuilder {
            secureRandom!!.ints(7L)
                .forEach { i -> sb.append(ID_VALUES[abs(i) % ID_VALUES.size]) }
            return sb
        }

        fun uniqueId(): String {
            val sb = StringBuilder(7)
            return generate(sb).toString()
        }
    }

}