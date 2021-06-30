package com.baruckis.kriptofolio.utilities.glide

import android.graphics.Bitmap
import androidx.annotation.NonNull
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class WhiteBackground : BitmapTransformation() {

    override fun transform(
            @NonNull pool: BitmapPool, @NonNull toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        return Transformation.whiteBackground(pool, toTransform, outWidth, outHeight)
    }

    override fun equals(other: Any?): Boolean {
        return other is WhiteBackground
    }

    override fun hashCode(): Int {
        return ID.hashCode()
    }

    override fun updateDiskCacheKey(@NonNull messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

    companion object {
        private val VERSION = 1
        private val ID = "com.baruckis.kriptofolio.utilities.glide.WhiteBackground.$VERSION"
        private val ID_BYTES = ID.toByteArray(Key.CHARSET)
    }
}