package com.east.ndklibjpeg

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.math.BigDecimal

class MainActivity : AppCompatActivity() {

    private val PICK_IMAGE = 1
    private val PICK_IMAGE_KITKAT = 2

    private var resultPath: String? = null
    private var file: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!PermissionUtil.isNeedRequestPermission(this)) {
        }
        file = File(Environment.getExternalStorageDirectory(), "ssssss.jpg")
        resultPath = file!!.absolutePath
    }


    fun pickImage(view: View?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(intent, PICK_IMAGE)
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_KITKAT)
        }
    }

    fun deleteImage(view: View?) {
        val file = File(resultPath)
        if (file.exists()) {
            file.delete()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        //获取图片路径
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE -> if (data != null) {
                    val uri = data.data
                    compressBitmap(uri)
                }
                PICK_IMAGE_KITKAT -> if (data != null) {
                    val uri = ensureUriPermition(this, data)
                    compressBitmap(uri)
                }
            }
        }
    }

    fun ensureUriPermition(
        context: Context,
        intent: Intent
    ): Uri? {
        val uri = intent.data
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val takeFlags = (intent.flags
                    and (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
            context.contentResolver.takePersistableUriPermission(uri!!, takeFlags)
        }
        return uri
    }

    private fun compressBitmap(uri: Uri?) {
        var bitmap: Bitmap? = null
        try {
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val path = Uri2PathUtil.getRealPathFromUri(this, uri)
            //            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true;
//            BitmapFactory.decodeFile(path, options);
//            String mimeType = options.outMimeType;

            // Log.d("dds_test", "图片类型：" + mimeType);
            Log.d(
                "dds_test",
                "压缩前大小：" + getFormatSize(File(path).length().toDouble())
            )

            // 读取图片的方向
            val degree = readPictureDegree(path)
            Log.d("dds_test", "path:$path")
            Log.d("dds_test", "\n 图片方向:$degree")

//            int result = NativeImageUtils.zoomCompress(path, resultPath, 65);
//            if (result > 0) {
//
//                Log.d("dds_test", "压缩完大小：" + getFormatSize(file.length()));
//                // 设置图片的方向
//                setPictureDegreeZero(resultPath, degree);
//                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
//            } else {
//                Log.e("dds_test", "压缩失败！");
//            }
            NativeImageUtils.compressBitmap(bitmap, 80, resultPath)
            Log.d("dds_test", "压缩完大小：" + getFormatSize(file!!.length().toDouble()))
            // 设置图片的方向
            setPictureDegreeZero(resultPath, degree)
            sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://" + file!!.absolutePath)
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // 读取文件的方向
    fun readPictureDegree(path: String?): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(path)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: IOException) {
            Log.e("dds_test", e.toString())
            e.printStackTrace()
        }
        return degree
    }

    // 设置文件的方向
    fun setPictureDegreeZero(path: String?, degree: Int) {
        try {
            Log.d("dds_test", "设置压缩完图片的方向:$degree")
            val exifInterface = ExifInterface(path)
            if (degree == 90) {
                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, "6")
            } else if (degree == 180) {
                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, "3")
            } else if (degree == 270) {
                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, "8")
            } else {
                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, "1")
            }
            exifInterface.saveAttributes()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // 格式化单位
    private fun getFormatSize(size: Double): String {
        val kiloByte = size / 1024
        if (kiloByte < 1) {
            return size.toString() + "Byte"
        }
        val megaByte = kiloByte / 1024
        if (megaByte < 1) {
            val result1 =
                BigDecimal(java.lang.Double.toString(kiloByte))
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB"
        }
        val gigaByte = megaByte / 1024
        if (gigaByte < 1) {
            val result2 =
                BigDecimal(java.lang.Double.toString(megaByte))
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB"
        }
        val teraBytes = gigaByte / 1024
        if (teraBytes < 1) {
            val result3 =
                BigDecimal(java.lang.Double.toString(gigaByte))
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB"
        }
        val result4 = BigDecimal(teraBytes)
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB"
    }

    // 根据路径获取bitmap对象
    private fun getBitmap(path: String): Bitmap? {
        val f = File(path)
        if (f.exists()) {
            try {
                return BitmapFactory.decodeFile(path)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    /**
     * 一种挺有效的方法，规避BitmapFactory.decodeStream或者decodeFile函数，使用BitmapFactory.decodeFileDescriptor
     *
     * @param path
     * @return
     */
    fun readBitmapByPath(path: String?): Bitmap? {
        val bfOptions = BitmapFactory.Options()
        bfOptions.inDither = false
        bfOptions.inPurgeable = true
        bfOptions.inInputShareable = true
        bfOptions.inTempStorage = ByteArray(32 * 1024)
        val file = File(path)
        var fs: FileInputStream? = null
        try {
            fs = FileInputStream(file)
            return BitmapFactory.decodeFileDescriptor(fs.fd, null, bfOptions)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (fs != null) {
                try {
                    fs.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }


    private fun compressImage(image: Bitmap): Bitmap? {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        var options = 100
        //循环判断如果压缩后图片是否大于100kb,大于继续压缩
        while (baos.toByteArray().size / 1024 > 100) {
            baos.reset()
            image.compress(Bitmap.CompressFormat.JPEG, options, baos)
            //每次都减少10
            options -= 10
        }
        val isBm = ByteArrayInputStream(baos.toByteArray())
        return BitmapFactory.decodeStream(isBm, null, null)
    }
}
