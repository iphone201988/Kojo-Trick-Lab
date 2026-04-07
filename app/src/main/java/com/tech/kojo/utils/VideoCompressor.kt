package com.tech.kojo.utils

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import java.io.File

object VideoCompressor {

    enum class Quality {
        LOW,    // 500 kbps
        MEDIUM, // 1000 kbps
        HIGH    // 2000 kbps
    }

    fun compressVideo(
        context: Context,
        videoFile: File,
        destinationFile: File,
        quality: Quality = Quality.MEDIUM,
        minBitrate: Int = 500_000,
        maxBitrate: Int = 2_000_000
    ): File? {
        return try {
            val bitrate = when (quality) {
                Quality.LOW -> minBitrate
                Quality.MEDIUM -> (minBitrate + maxBitrate) / 2
                Quality.HIGH -> maxBitrate
            }

            // For now, return original file if compression fails
            // You can integrate FFmpeg or other compression libraries here

            // If original file is already small enough
            if (videoFile.length() <= 20 * 1024 * 1024) {
                videoFile.copyTo(destinationFile, overwrite = true)
                return destinationFile
            }

            // TODO: Implement actual compression using:
            // 1. FFmpegAndroid library
            // 2. TranscodeVideo with MediaCodec
            // 3. Use external service

            // Fallback: return original
            videoFile.copyTo(destinationFile, overwrite = true)
            destinationFile

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Alternative compression using MediaCodec (advanced)
    private fun transcodeVideo(
        inputPath: String,
        outputPath: String,
        bitrate: Int
    ): Boolean {
        return try {
            val extractor = MediaExtractor()
            extractor.setDataSource(inputPath)

            val trackCount = extractor.trackCount
            var videoTrackIndex = -1
            var videoFormat: MediaFormat? = null

            for (i in 0 until trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("video/") == true) {
                    videoTrackIndex = i
                    videoFormat = format
                    break
                }
            }

            if (videoTrackIndex == -1) {
                return false
            }

            extractor.selectTrack(videoTrackIndex)

            val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val outputFormat = MediaFormat.createVideoFormat("video/avc",
                videoFormat?.getInteger(MediaFormat.KEY_WIDTH) ?: 640,
                videoFormat?.getInteger(MediaFormat.KEY_HEIGHT) ?: 480)
            outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
            outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)

            val codec = MediaCodec.createEncoderByType("video/avc")
            codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()

            // Transcoding logic here...

            muxer.release()
            codec.stop()
            codec.release()
            extractor.release()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}