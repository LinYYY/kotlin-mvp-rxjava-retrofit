package com.tech.mvpframework.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.bitmap.VideoDecoder.FRAME_OPTION
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import jp.wasabeef.glide.transformations.BlurTransformation


/**
 *  create by Myking
 *  date : 2020/5/14 19:38
 *  description :
 */
class ImageLoader {

    companion object {
        @SuppressLint("CheckResult")
        @JvmStatic
        fun load(
            context: Context,
            url: Any,
            iv: ImageView,
            @DrawableRes placeholder: Int? = null,
            width: Int = -1,
            height: Int = -1,
            userCenterCrop: Boolean = true,
            userCache: Boolean = true
        ) {
            val options =
                RequestOptions().diskCacheStrategy(if (userCache) DiskCacheStrategy.DATA else DiskCacheStrategy.NONE)

            placeholder?.let {
                options.placeholder(placeholder).centerInside()
            }
            if (width != -1 && height != -1) {
                options.override(width, height)
            }
            if (userCenterCrop) {
                options.centerCrop()
            }

            val startTime = System.currentTimeMillis()
            Glide.with(context)
                .load(url)
                .dontAnimate()
                .apply(options)
                .into(iv)
        }

        @JvmStatic
        fun loadCircleImage(
            context: Context,
            url: Any,
            iv: ImageView,
            @DrawableRes placeholder: Int? = null
        ) {
            val options =
                RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA).circleCrop()

            placeholder?.let {
                options.placeholder(placeholder)
            }

            iv?.let {
                Glide.with(context)
                    .load(url)
                    .dontAnimate()
                    .apply(options)
                    .into(iv)
            }
        }

        @JvmStatic
        fun loadRoundImage(
            context: Context,
            url: String,
            iv: ImageView,
            radius: Int = 5,
            @DrawableRes placeholder: Int? = null,
            userCenterCrop: Boolean = true
        ) {
            val options = if (userCenterCrop)
                RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA)
                    .transform(
                        MultiTransformation(
                            CenterCrop(),
                            RoundedCorners(radius)
                        )
                    ) else RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA)
                .transform(RoundedCorners(radius))

            placeholder?.let {
                options.placeholder(placeholder)
            }

            Glide.with(context)
                .load(url)
                .dontAnimate()
                .centerCrop()
                .apply(options)
                .into(object : CustomViewTarget<ImageView, Drawable>(iv) {

                    override fun onResourceLoading(placeholder: Drawable?) {
                        view.scaleType = ImageView.ScaleType.CENTER
                        view.setImageDrawable(placeholder)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        view.scaleType = ImageView.ScaleType.CENTER
                        view.setImageDrawable(errorDrawable)
                    }

                    override fun onResourceCleared(placeholder: Drawable?) {
                        view.scaleType = ImageView.ScaleType.CENTER
                        view.setImageDrawable(placeholder)
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        view.scaleType = ImageView.ScaleType.CENTER_CROP
                        view.setImageDrawable(resource)
                    }

                })

        }

        @SuppressLint("CheckResult")
        @JvmStatic
        fun loadRoundImage(
            context: Context,
            url: String,
            iv: ImageView,
            radius: Int = 5,
            @DrawableRes placeholder: Int? = null,
            @DrawableRes error: Int? = null,
            width: Int = -1,
            height: Int = -1,
            userCenterCrop: Boolean = true,
            onStart: (() -> Unit)? = null,
            onProgress: ((Float) -> Unit)? = null,
            onFinish: (() -> Unit)? = null
        ) {
            val options = if (userCenterCrop)
                RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA)
                    .transform(
                        MultiTransformation(
                            CenterCrop(),
                            RoundedCorners(radius)
                        )
                    ) else RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA)
                .transform(RoundedCorners(radius))

            placeholder?.let {
                options.placeholder(placeholder)
            }
            error?.let {
                options.error(error)
            }
            if (width != -1 && height != -1) {
                options.override(width, height)
            }
            onStart?.invoke()
            ProgressAppGlideModule.expect(url,
                object : ProgressAppGlideModule.UIonProgressListener {
                    override fun onProgress(bytesRead: Long, expectedLength: Long) {
                        onProgress?.invoke((100f * bytesRead / expectedLength))
                    }

                    override val granualityPercentage: Float
                        get() = 1.0f

                })

            GlideApp.with(context)
                .load(url)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        onFinish?.invoke()
                        ProgressAppGlideModule.forget(url)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        onFinish?.invoke()
                        ProgressAppGlideModule.forget(url)
                        return false
                    }
                })
                .dontAnimate()
                .centerCrop()
                .apply(options)
                .into(object : CustomViewTarget<ImageView, Drawable>(iv) {

                    override fun onResourceLoading(placeholder: Drawable?) {
                        view.scaleType = ImageView.ScaleType.CENTER
                        view.setImageDrawable(placeholder)
                        ProgressAppGlideModule.forget(url)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        view.scaleType = ImageView.ScaleType.CENTER
                        view.setImageDrawable(errorDrawable)
                        ProgressAppGlideModule.forget(url)
                    }

                    override fun onResourceCleared(placeholder: Drawable?) {
                        view.scaleType = ImageView.ScaleType.CENTER
                        view.setImageDrawable(placeholder)
                        ProgressAppGlideModule.forget(url)
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        view.scaleType = ImageView.ScaleType.CENTER_CROP
                        view.setImageDrawable(resource)
                        ProgressAppGlideModule.forget(url)
                    }

                    override fun onDestroy() {
                        super.onDestroy()
                        ProgressAppGlideModule.forget(url)
                    }

                })

        }

        /**
         * 毛玻璃
         */
        fun loadGlassImage(
            context: Context,
            url: Any,
            iv: ImageView?,
            width: Int = -1,
            height: Int = -1,
            @DrawableRes placeholder: Int? = null,
            radius: Int = 20,
            sampling: Int = 1
        ) {
            iv?.let {
                val option =
                    bitmapTransform((BlurTransformation(radius, sampling))).diskCacheStrategy(
                        DiskCacheStrategy.ALL
                    )
                if (width != -1 && height != -1) {
                    option.override(width, height)
                }
                Glide.with(context)
                    .load(url)
                    .apply(option)
                    .dontAnimate()
                    .into(iv)
            }
        }

        @SuppressLint("CheckResult")
        fun loadVideoScreenshot(
            context: Context,
            url: Any,
            iv: ImageView,
            @DrawableRes placeholder: Int? = null,
            width: Int = -1,
            height: Int = -1,
            userCenterCrop: Boolean = true,
            userCache: Boolean = true,
            frameTimeMicros: Long = 0L
        ) {
            val options =
                RequestOptions.frameOf(frameTimeMicros)
            options.set(FRAME_OPTION, MediaMetadataRetriever.OPTION_CLOSEST)
            placeholder?.let {
                options.placeholder(placeholder).centerInside()
            }
            if (width != -1 && height != -1) {
                options.override(width, height)
            }
            if (userCenterCrop) {
                options.centerCrop()
            }
            Glide.with(context).load(url).apply(options).into(iv)
        }

        @SuppressLint("CheckResult")
        fun loadVideoScreenshotGlass(
            context: Context,
            url: Any,
            iv: ImageView,
            @DrawableRes placeholder: Int? = null,
            width: Int = -1,
            height: Int = -1,
            frameTimeMicros: Long = 0L,
            radius: Int = 20,
            sampling: Int = 1
        ) {
            val options =
                RequestOptions.frameOf(frameTimeMicros)
            options.set(FRAME_OPTION, MediaMetadataRetriever.OPTION_CLOSEST)
            placeholder?.let {
                options.placeholder(placeholder).centerInside()
            }
            if (width != -1 && height != -1) {
                options.override(width, height)
            }
            options.transform(BlurTransformation(radius, sampling))
            Glide.with(context).load(url).apply(options).into(iv)
        }
    }
}