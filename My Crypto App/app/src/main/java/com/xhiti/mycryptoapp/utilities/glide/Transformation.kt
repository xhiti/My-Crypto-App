package com.baruckis.kriptofolio.utilities.glide

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import androidx.annotation.NonNull
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.util.Synthetic
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class Transformation {

    companion object {

        private val MODELS_REQUIRING_BITMAP_LOCK = HashSet(
                Arrays.asList(
                        "XT1085",
                        "XT1092",
                        "XT1093",
                        "XT1094",
                        "XT1095",
                        "XT1096",
                        "XT1097",
                        "XT1098",
                        "XT1031",
                        "XT1028",
                        "XT937C",
                        "XT1032",
                        "XT1008",
                        "XT1033",
                        "XT1035",
                        "XT1034",
                        "XT939G",
                        "XT1039",
                        "XT1040",
                        "XT1042",
                        "XT1045",
                        "XT1063",
                        "XT1064",
                        "XT1068",
                        "XT1069",
                        "XT1072",
                        "XT1077",
                        "XT1078",
                        "XT1079"
                )
        )

        private val BITMAP_DRAWABLE_LOCK = if (MODELS_REQUIRING_BITMAP_LOCK.contains(Build.MODEL))
            ReentrantLock()
        else
            NoLock()

        fun whiteBackground(@NonNull pool: BitmapPool, @NonNull inBitmap: Bitmap, destWidth: Int, destHeight: Int): Bitmap {

            val config = getNonNullConfig(inBitmap)
            val result = pool.get(destWidth, destHeight, config)

            BITMAP_DRAWABLE_LOCK.lock()
            try {
                val canvas = Canvas(result)
                canvas.drawColor(Color.WHITE)
                canvas.drawBitmap(inBitmap, 0f, 0f, null)
                clear(canvas)
            } finally {
                BITMAP_DRAWABLE_LOCK.unlock()
            }

            return result
        }

        @NonNull
        private fun getNonNullConfig(@NonNull bitmap: Bitmap): Bitmap.Config {
            return if (bitmap.config != null) bitmap.config else Bitmap.Config.ARGB_8888
        }

        private fun clear(canvas: Canvas) {
            canvas.setBitmap(null)
        }

    }

    private class NoLock @Synthetic
    internal constructor() : Lock {

        override fun lock() {

        }

        @Throws(InterruptedException::class)
        override fun lockInterruptibly() {

        }

        override fun tryLock(): Boolean {
            return true
        }

        @Throws(InterruptedException::class)
        override fun tryLock(time: Long, @NonNull unit: TimeUnit): Boolean {
            return true
        }

        override fun unlock() {

        }

        @NonNull
        override fun newCondition(): Condition {
            throw UnsupportedOperationException("Should not be called")
        }
    }

}