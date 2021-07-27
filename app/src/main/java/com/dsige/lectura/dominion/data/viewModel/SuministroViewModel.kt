package com.dsige.lectura.dominion.data.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dsige.lectura.dominion.data.local.model.*
import com.dsige.lectura.dominion.data.local.repository.ApiError
import com.dsige.lectura.dominion.data.local.repository.AppRepository
import com.dsige.lectura.dominion.helper.Mensaje
import com.dsige.lectura.dominion.helper.Util
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import io.reactivex.CompletableObserver
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import javax.inject.Inject

class SuministroViewModel @Inject
internal constructor(private val roomRepository: AppRepository, private val retrofit: ApiError) :
    ViewModel() {

    val mensajeAlert = MutableLiveData<String>()
    val mensajeError = MutableLiveData<String>()
    val mensajeSuccess = MutableLiveData<String>()
    val servicios: MutableLiveData<List<Servicio>> = MutableLiveData()
    val lecturas: MutableLiveData<IntArray> = MutableLiveData()
    val recoveredPhotos: MutableLiveData<List<Photo>> = MutableLiveData()

    val user: LiveData<Usuario>
        get() = roomRepository.getUsuario()

    fun setError(s: String) {
        mensajeError.value = s
    }

    fun setAlert(s: String) {
        mensajeAlert.value = s
    }

    fun getServices() {
        roomRepository.getServices()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<List<Servicio>> {
                override fun onSubscribe(d: Disposable) {}
                override fun onError(e: Throwable) {}
                override fun onComplete() {}
                override fun onNext(t: List<Servicio>) {
                    servicios.value = t
                }
            })
    }

    fun getServicios(): LiveData<List<Servicio>> {
        return servicios
    }

    fun getRecoveredPhotos() {
        roomRepository.getRecoveredPhotos()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<List<Photo>> {
                override fun onSubscribe(d: Disposable) {}
                override fun onError(e: Throwable) {}
                override fun onComplete() {}
                override fun onNext(t: List<Photo>) {
                    recoveredPhotos.value = t
                }
            })
    }

    fun getPhotosRecuperadas(): LiveData<List<Photo>> {
        return recoveredPhotos
    }

    fun getTipoLectura() {
        roomRepository.getTipoLectura()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<IntArray> {
                override fun onSubscribe(d: Disposable) {}
                override fun onError(e: Throwable) {}
                override fun onComplete() {}
                override fun onNext(t: IntArray) {
                    lecturas.value = t
                }
            })
    }

    fun getLecturas(): LiveData<IntArray> {
        return lecturas
    }

    fun getSuministroLectura(
        estado: Int,
        activo: Int,
        observadas: Int
    ): LiveData<List<SuministroLectura>> {
        return roomRepository.getSuministroLectura(estado, activo, observadas)
    }

    fun getSuministroCortes(estado: Int, i: Int): LiveData<List<SuministroCortes>> {
        return roomRepository.getSuministroCortes(estado, i)
    }

    fun getSuministroReconexion(estado: Int, i: Int): LiveData<List<SuministroReconexion>> {
        return roomRepository.getSuministroReconexion(estado, i)
    }

    fun getSuministroReclamos(e: String, i: Int): LiveData<List<SuministroLectura>> {
        return roomRepository.getSuministroReclamos(e, i)
    }

    fun getRegistro(orden: Int, tipo: Int, recuperada: Int): LiveData<Registro> {
        return roomRepository.getRegistro(orden, tipo, recuperada)
    }

    fun getMotivos(): LiveData<List<Motivo>> {
        return roomRepository.getMotivos()
    }

    fun getDetalleGrupoByLectura(lecturaEstado: Int): LiveData<List<DetalleGrupo>> {
        return roomRepository.getDetalleGrupoByLectura(lecturaEstado)
    }

    fun getDetalleGrupoByFirstLectura(lecturaEstado: Int): Observable<DetalleGrupo> {
        return roomRepository.getDetalleGrupoByFirstLectura(lecturaEstado)
    }

    fun getDetalleGrupoByMotivo(estado: Int, s: String): LiveData<List<DetalleGrupo>> {
        return roomRepository.getDetalleGrupoByMotivo(estado, s)
    }

    fun getDetalleGrupoByMotivoTask(estado: Int, s: String): Observable<DetalleGrupo> {
        return roomRepository.getDetalleGrupoByMotivoTask(estado, s)
    }

    fun getDetalleGrupoByParentId(i: Int): LiveData<List<DetalleGrupo>> {
        return roomRepository.getDetalleGrupoByParentId(i)
    }

    fun getDetalleGrupoById(id: Int): Observable<DetalleGrupo> {
        return roomRepository.getDetalleGrupoById(id)
    }

    fun getGrandesClientes(): LiveData<List<GrandesClientes>> {
        return roomRepository.getGrandesClientes()
    }

    fun getRegistroBySuministroTask(id: Int): Observable<Registro> {
        return roomRepository.getRegistroBySuministroTask(id)
    }

    fun getSuministroLecturaByOrdenTask(number: Int): Observable<SuministroLectura> {
        return roomRepository.suministroLecturaByOrden(number)
    }

    fun getSuministroCorteByOrdenTask(number: Int): Observable<SuministroCortes> {
        return roomRepository.getSuministroCorteByOrdenTask(number)
    }

    fun getSuministroReconexionByOrdenTask(number: Int): Observable<SuministroReconexion> {
        return roomRepository.getSuministroReconexionByOrdenTask(number)
    }

    fun getSuministroLeft(estado: Int, orden: Int, suministroOrden: Int): Observable<Int> {
        return roomRepository.getSuministroLeft(estado, orden, suministroOrden)
    }

    fun getSuministroRight(estado: Int, orden: Int, suministroOrden: Int): Observable<Int> {
        return roomRepository.getSuministroRight(estado, orden, suministroOrden)
    }

    fun insertRegistro(r: Registro) {
        roomRepository.insertRegistro(r)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {}
                override fun onComplete() {
                    if (r.registro_TieneFoto == "1") {
                        mensajeSuccess.value = "photo"
                    } else {
                        mensajeSuccess.value = "Registro Guardado"
                    }
                }

                override fun onError(e: Throwable) {}
            })
    }

    fun getPhotoAllBySuministro(id: Int, tipo: Int, i: Int): LiveData<List<Photo>> {
        return roomRepository.getPhotoAllBySuministro(id, tipo, i)
    }

    fun getRegistroBySuministro(id: Int): LiveData<Registro> {
        return roomRepository.getRegistroBySuministro(id)
    }

    fun deletePhoto(p: Photo, context: Context) {
        roomRepository.deletePhoto(p, context)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {}
                override fun onComplete() {}
                override fun onError(e: Throwable) {}
            })
    }

    fun generatePhoto(
        nameImg: String,
        context: Context,
        fechaAsignacion: String,
        direccion: String,
        latitud: String,
        longitud: String,
        receive: Int,
        tipo: Int
    ) {
        Util.generatePhoto(
            nameImg, context, fechaAsignacion, direccion, latitud, longitud, receive, tipo
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Photo> {
                override fun onSubscribe(d: Disposable) {}
                override fun onComplete() {}
                override fun onNext(t: Photo) {
                    insertPhoto(t)
                }

                override fun onError(e: Throwable) {
                    mensajeError.value = e.message
                }

            })
    }


    fun insertPhoto(p: Photo) {
        roomRepository.insertPhoto(p)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {}
                override fun onComplete() {
                    if (p.firm == 1) {
                        mensajeSuccess.value = "Firma Guardado"
                    }
                }

                override fun onError(e: Throwable) {}
            })
    }

//    fun updateArchivo(
//        nameImg: String,
//        id: Context
//    ) {
//        Util.getPhotoAdjunto(
//            nameImg, context, fechaAsignacion, direccion,
//            latitud, longitud, receive, tipo
//        )
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(object : Observer<Photo> {
//                override fun onSubscribe(d: Disposable) {}
//                override fun onNext(t: Photo) {
//                    insertPhoto(t)
//                }
//
//                override fun onError(e: Throwable) {}
//                override fun onComplete() {}
//            })
//    }


    fun verificateCorte(s: String, id: Int, context: Context) {
        roomRepository.getVerificateCorte(s)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Mensaje> {
                override fun onSubscribe(d: Disposable) {}
                override fun onError(e: Throwable) {
                    mensajeError.value = e.message.toString()
                }

                override fun onComplete() {}
                override fun onNext(t: Mensaje) {
                    if (t.codigo == 0) {
                        sendFiles(id, context)
                    } else {
                        mensajeError.value = t.mensaje
                    }
                }
            })
    }

    fun sendFiles(id: Int, context: Context) {
        val files = roomRepository.getPhotoTaskFile(id)
        files.flatMap { observable ->
            Observable.fromIterable(observable).flatMap { a ->
                val b = MultipartBody.Builder()
                b.setType(MultipartBody.FORM)
                val file = File(Util.getFolder(context), a.rutaFoto)
                if (file.exists()) {
                    b.addFormDataPart(
                        "files", file.name,
                        RequestBody.create(
                            MediaType.parse("multipart/form-data"), file
                        )
                    )
                }
                val body = b.build()
                Observable.zip(
                    Observable.just(a), roomRepository.sendPhotos(body),
                    { _, t ->
                        t
                    })

            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<String> {
                override fun onSubscribe(d: Disposable) {}
                override fun onComplete() {
                    sendSuministro(id)
                }

                override fun onNext(t: String) {}
                override fun onError(t: Throwable) {
                    if (t is HttpException) {
                        val body = t.response().errorBody()
                        try {
                            val error = retrofit.errorConverter.convert(body!!)
                            mensajeError.postValue(error!!.Message)
                        } catch (e1: IOException) {
                            e1.printStackTrace()
                        }
                    } else {
                        mensajeError.postValue(t.message)
                    }
                }
            })
    }

    private fun sendSuministro(id: Int) {
        val register: Observable<Registro> = roomRepository.getRegistroByIdTask(id)
        register.flatMap { a ->
            val json = Gson().toJson(a)
//            Log.i("TAG", json)
            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
            Observable.zip(
                Observable.just(a),
                roomRepository.sendRegistro(body), { _, m -> m })
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Mensaje> {
                override fun onSubscribe(d: Disposable) {}
                override fun onComplete() {}
                override fun onNext(t: Mensaje) {
                    updateEnableRegistro(t)
                }

                override fun onError(t: Throwable) {
                    if (t is HttpException) {
                        val body = t.response().errorBody()
                        try {
                            val error = retrofit.errorConverter.convert(body!!)
                            mensajeError.postValue(error!!.Message)
                        } catch (e1: IOException) {
                            e1.printStackTrace()
                        }
                    } else {
                        mensajeError.postValue(t.message)
                    }
                }
            })
    }

    private fun updateEnableRegistro(t: Mensaje) {
        roomRepository.updateEnableRegistro(t)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {}
                override fun onError(e: Throwable) {}
                override fun onComplete() {
                    mensajeSuccess.value = t.mensaje
                }
            })
    }


    fun updateRegistro(id: Int, tipo: Int, estado: Int) {
        roomRepository.updateRegistro(id, tipo, estado)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {}
                override fun onComplete() {
                    if (estado == 2) {
                        mensajeSuccess.value = "firma"
                    } else {
                        mensajeSuccess.value = "siguiente"
                    }
                }

                override fun onError(e: Throwable) {}
            })
    }

    fun getPhotoFirm(id: Int): LiveData<List<Photo>> {
        return roomRepository.getPhotoFirm(id)
    }

    fun getRegistros(): LiveData<List<Registro>> {
        return roomRepository.getRegistros()
    }

    fun getSuministroLecturaById(id: Int): LiveData<SuministroLectura> {
        return roomRepository.getSuministroLecturaById(id)
    }

    fun getSuministroCorteById(id: Int): LiveData<SuministroCortes> {
        return roomRepository.getSuministroCorteById(id)
    }

    fun getSuministroReconexionById(id: Int): LiveData<SuministroReconexion> {
        return roomRepository.getSuministroReconexionById(id)
    }
}