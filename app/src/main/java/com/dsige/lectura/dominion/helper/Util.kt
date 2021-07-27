package com.dsige.lectura.dominion.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.*
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.telephony.TelephonyManager
import android.text.TextPaint
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dsige.lectura.dominion.BuildConfig
import com.dsige.lectura.dominion.R
import com.dsige.lectura.dominion.data.local.model.Photo
import com.dsige.lectura.dominion.data.workManager.BatteryWork
import com.dsige.lectura.dominion.data.workManager.GpsWork
import com.dsige.lectura.dominion.data.workManager.LecturaWork
import com.dsige.lectura.dominion.data.workManager.PhotosWork
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Completable
import io.reactivex.Observable
import java.io.*
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.*

object Util {

    const val KEY_UPDATE_ENABLE = "isUpdate"
    const val KEY_UPDATE_VERSION = "version"
    const val KEY_UPDATE_URL = "url"
    const val KEY_UPDATE_NAME = "name"
    val locale = Locale("es", "ES")

    fun getFecha(): String {
        val date = Date()
        @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat("dd/MM/yyyy")
        return format.format(date)
    }

    fun getFechaActual(): String {
        val date = Date()
        @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        return format.format(date)
    }

    private fun getHoraActual(): String {
        val date = Date()
        @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat("HH:mm:ss aaa")
        return format.format(date)
    }

    fun getDateFirmReconexiones(id: Int, tipo: Int, f: String): String {
        val date = Date()
        @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat("ddMMyyyy_HHmmssSSS")
        val fechaActual = format.format(date)
        return String.format("Firm(%s)_%s_%s_%s.jpg", f, id, tipo, fechaActual)
    }

    @Throws(IOException::class)
    fun copyFile(sourceFile: File, destFile: File) {
        if (!sourceFile.exists()) {
            return
        }
        val source: FileChannel? = FileInputStream(sourceFile).channel
        val destination: FileChannel = FileOutputStream(destFile).channel
        if (source != null) {
            destination.transferFrom(source, 0, source.size())
        }
        source?.close()
        destination.close()
    }

    fun getFolder(context: Context): File {
        val folder = File(context.getExternalFilesDir(null)!!.absolutePath)
        if (!folder.exists()) {
            val success = folder.mkdirs()
            if (!success) {
                folder.mkdir()
            }
        }
        return folder
    }

    fun generateImageAsync(context: Context, pathFile: String): Completable {
        return Completable.fromAction {
            compressImage(context, pathFile, "", "", "")
        }
    }

    fun getDateTimeFormatString(date: Date): String {
        @SuppressLint("SimpleDateFormat") val df = SimpleDateFormat("dd/MM/yyyy - hh:mm:ss a")
        return df.format(date)
    }

    fun getVersion(context: Context): String {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return pInfo.versionName
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    fun getImei(context: Context): String {
        val deviceUniqueIdentifier: String
        val telephonyManager: TelephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        deviceUniqueIdentifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            telephonyManager.imei
        } else {
            @Suppress("DEPRECATION")
            telephonyManager.deviceId
        }
        return deviceUniqueIdentifier
    }

    fun getNotificacionValid(context: Context): String? {
        return context.getSharedPreferences("TOKEN", MODE_PRIVATE).getString("update", "")
    }

    fun snackBarMensaje(view: View, mensaje: String) {
        val mSnackbar = Snackbar.make(view, mensaje, Snackbar.LENGTH_SHORT)
        mSnackbar.setAction("Ok") { mSnackbar.dismiss() }
        mSnackbar.show()
    }

    fun toastMensaje(context: Context, mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
    }

    fun dialogMensaje(context: Context, title: String, mensaje: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(mensaje)
            .setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun hideKeyboard(activity: Activity) {
        val view = activity.currentFocus
        if (view != null) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun showKeyboard(edit: EditText, context: Context) {
        edit.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    fun deletePhoto(photo: String, context: Context) {
        val f = File(getFolder(context), photo)
        if (f.exists()) {
            val uriSavedImage = FileProvider.getUriForFile(
                context, BuildConfig.APPLICATION_ID + ".fileprovider", f
            )
            context.contentResolver.delete(uriSavedImage, null, null)
            f.delete()
        }
    }

    fun getFolderAdjunto(
        titleImg: String, context: Context, data: Intent
    ): Observable<String> {
        return Observable.create {
            val uri: Uri? = data.data
            if (uri != null) {
                uri.let { returnUri ->
                    context.contentResolver.query(returnUri, null, null, null, null)
                }?.use { cursor ->
                    cursor.moveToFirst()
                }

                val file = getFechaForGrandesCliente(titleImg)
                val f = File(getFolder(context), file)
                val input =
                    context.contentResolver.openInputStream(uri) as FileInputStream
                val out = FileOutputStream(f)
                val inChannel = input.channel
                val outChannel = out.channel
                inChannel.transferTo(0, inChannel.size(), outChannel)
                input.close()
                out.close()

                compressImage(context, f.absolutePath, "", "", "")

                it.onNext(file)
                it.onComplete()
                return@create
            }
        }
    }

    fun generatePhoto(
        nameImg: String, context: Context, fechaAsignacion: String, direccion: String,
        latitud: String, longitud: String, receive: Int, tipo: Int
    ): Observable<Photo> {
        return Observable.create {
            val f = File(getFolder(context), "$nameImg.jpg")
            if (f.exists()) {
                val coordenadas = "Latitud : $latitud  Longitud: $longitud"
                compressImage(context, f.absolutePath, fechaAsignacion, direccion, coordenadas)
                val photo = Photo()
                photo.iD_Suministro = receive
                photo.rutaFoto = "$nameImg.jpg"
                photo.fecha_Sincronizacion_Android = getFechaActual()
                photo.tipo = tipo
                photo.estado = 1
                photo.latitud = latitud
                photo.longitud = longitud
                photo.fecha = getFecha()
                it.onNext(photo)
                it.onComplete()
                return@create
            }
            it.onError(Throwable("No se encontro la foto fisica favor de volver a tomar foto"))
            it.onComplete()
        }
    }

    // execute services
    fun executeLecturaWork(context: Context) {
//        val downloadConstraints = Constraints.Builder()
//            .setRequiresCharging(true)
//            .setRequiredNetworkType(NetworkType.CONNECTED)
//            .build()
        // Define the input data for work manager
//        val data = Data.Builder()
//        data.putInt("tipo", tipo)

        // Create an one time work request
        val downloadImageWork = OneTimeWorkRequest
            .Builder(LecturaWork::class.java)
//          .setInputData(data.build())
//            .setConstraints(downloadConstraints)
            .build()
        WorkManager.getInstance(context).enqueue((downloadImageWork))
    }

    fun executeGpsWork(context: Context) {
//        val downloadConstraints = Constraints.Builder()
//            .setRequiresCharging(true)
//            .setRequiredNetworkType(NetworkType.CONNECTED)
//            .build()
        val locationWorker =
            PeriodicWorkRequestBuilder<GpsWork>(15, TimeUnit.MINUTES)
//                .setConstraints(downloadConstraints)
                .build()
        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(
                "Gps-Work",
                ExistingPeriodicWorkPolicy.REPLACE,
                locationWorker
            )
        toastMensaje(context, "Servicio Gps Activado")
    }

    fun closeGpsWork(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("Gps-Work")
    }

    fun executePhotosWork(context: Context) {
        val locationWorker =
            PeriodicWorkRequestBuilder<PhotosWork>(2, TimeUnit.HOURS)
                .build()
        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(
                "Photos-Work",
                ExistingPeriodicWorkPolicy.REPLACE,
                locationWorker
            )
    }

    fun closePhotosWork(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("Photos-Work")
    }

    fun executeBatteryWork(context: Context) {
        val locationWorker =
            PeriodicWorkRequestBuilder<BatteryWork>(15, TimeUnit.MINUTES)
                .build()
        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(
                "Battery-Work",
                ExistingPeriodicWorkPolicy.REPLACE,
                locationWorker
            )
    }

    fun closeBatteryWork(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("Battery-Work")
    }

    fun getFechaForGrandesCliente(code: String): String {
        val date = Date()
        @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat("ddMMyyyy_HHmmssSSSS")
        return String.format("%s_%s.jpg", code, format.format(date))
    }


    fun getFechaSuministro(id: Int, tipo: Int, fecha: String): String {
        val date = Date()
        @SuppressLint("SimpleDateFormat") val format = when (tipo) {
            1, 10 -> SimpleDateFormat("_HHmmssSSSS")
            else -> SimpleDateFormat("ddMMyyyy_HHmmssSSSS")
        }
        val fechaActual = format.format(date)
        return when (tipo) {
            1, 10 -> String.format("%s_%s_%s%s", id, tipo, fecha.replace("/", ""), fechaActual)
            else -> String.format("%s_%s_%s", id, tipo, fechaActual)
        }
    }

    fun createImageFile(name: String, context: Context): File {

        return File(getFolder(context), "$name.jpg").apply {
            absolutePath
        }
    }

    fun getMobileDataState(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val cmClass = Class.forName(cm.javaClass.name)
        val method = cmClass.getDeclaredMethod("getMobileDataEnabled")
        method.isAccessible = true // Make the method callable
        // get the setting for "mobile data"
        return method.invoke(cm) as Boolean
    }

    private fun compressImage(
        context: Context,
        filePath: String,
        fecha: String,
        direccion: String,
        coordenadas: String
    ) {
        var scaledBitmap: Bitmap?

        val options = BitmapFactory.Options()

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(filePath, options)
        var actualHeight = options.outHeight
        var actualWidth = options.outWidth

//      max Height and width values of the compressed image is taken as 816x612
        val maxHeight = 816.0f
        val maxWidth = 612.0f
        var imgRatio = (actualWidth / actualHeight).toFloat()
        val maxRatio = maxWidth / maxHeight

//      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            when {
                imgRatio < maxRatio -> {
                    imgRatio = maxHeight / actualHeight
                    actualWidth = (imgRatio * actualWidth).toInt()
                    actualHeight = maxHeight.toInt()
                }
                imgRatio > maxRatio -> {
                    imgRatio = maxWidth / actualWidth
                    actualHeight = (imgRatio * actualHeight).toInt()
                    actualWidth = maxWidth.toInt()
                }
                else -> {
                    actualHeight = maxHeight.toInt()
                    actualWidth = maxWidth.toInt()
                }
            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false

//      this options allow Android to claim the bitmap memory if it runs low on memory
//        options.inPurgeable = true
//        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }

        scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)

        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)

        canvas.drawBitmap(
            bmp,
            middleX - bmp.width / 2,
            middleY - bmp.height / 2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        )

        // check the rotation of the image and display it properly
        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, 0
            )
//            Log.d("EXIF", "Exif: $orientation")
            val matrix = Matrix()
            when (orientation) {
                6 -> matrix.postRotate(90f)
                3 -> matrix.postRotate(180f)
                8 -> matrix.postRotate(270f)
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap, 0, 0,
                scaledBitmap.width, scaledBitmap.height, matrix,
                true
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val canvasPaint = Canvas(scaledBitmap!!)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE)

        val gText = if (direccion.isEmpty() && coordenadas.isEmpty()) {
            String.format(
                "%s",
                if (fecha.isEmpty())
                    getDateTimeFormatString(Date(File(filePath).lastModified()))
                else
                    String.format("%s %s", fecha, getHoraActual())
            )
        } else {
            String.format(
                "%s\n%s\n%s",
                if (fecha.isEmpty())
                    getDateTimeFormatString(Date(File(filePath).lastModified()))
                else
                    String.format("%s %s", fecha, getHoraActual()),
                direccion, coordenadas
            )
        }

        val bounds = Rect()
        var noOfLines = 0
        for (line in gText.split("\n").toTypedArray()) {
            noOfLines++
        }

        paint.getTextBounds(gText, 0, gText.length, bounds)
        val x = 10f
        var y: Float = (scaledBitmap.height - bounds.height() * noOfLines + 2).toFloat()

        // Fondo
        val mPaint = Paint()
        mPaint.color = ContextCompat.getColor(context, R.color.transparentBlack)

        // Tamaño del Fondo
        val top = scaledBitmap.height - bounds.height() * (noOfLines + 1.5)
        canvasPaint.drawRect(
            0f,
            top.toFloat(),
            scaledBitmap.width.toFloat(),
            scaledBitmap.height.toFloat(),
            mPaint
        )

        // Agregando texto
        for (line in gText.split("\n").toTypedArray()) {
            val txt =
                TextUtils.ellipsize(
                    line,
                    TextPaint(),
                    (scaledBitmap.width * 0.95).toFloat(),
                    TextUtils.TruncateAt.END
                )
            canvasPaint.drawText(txt.toString(), x, y, paint)
            y += paint.descent() - paint.ascent()
        }


        val out: FileOutputStream?
        try {
            out = FileOutputStream(filePath)
            //write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = (height.toFloat() / reqHeight.toFloat()).roundToInt()
            val widthRatio = (width.toFloat() / reqWidth.toFloat()).roundToInt()
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }
}