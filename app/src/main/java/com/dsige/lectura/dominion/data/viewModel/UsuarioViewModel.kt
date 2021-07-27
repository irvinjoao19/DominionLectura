package com.dsige.lectura.dominion.data.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dsige.lectura.dominion.data.local.model.Registro
import com.dsige.lectura.dominion.data.local.model.Sync
import com.dsige.lectura.dominion.data.local.model.Usuario
import com.dsige.lectura.dominion.data.local.repository.ApiError
import com.dsige.lectura.dominion.data.local.repository.AppRepository
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import io.reactivex.CompletableObserver
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UsuarioViewModel @Inject
internal constructor(private val roomRepository: AppRepository, private val retrofit: ApiError) :
    ViewModel() {

    val mensajeError = MutableLiveData<String>()
    val mensajeSuccess = MutableLiveData<String>()

    val user: LiveData<Usuario>
        get() = roomRepository.getUsuario()

    fun setError(s: String) {
        mensajeError.value = s
    }

    fun getLogin(usuario: String, pass: String, imei: String, version: String, token: String) {
        roomRepository.getUsuarioService(usuario, pass, imei, version, token)
            .delay(1000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Usuario> {
                override fun onComplete() {}
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(usuario: Usuario) {
                    if (usuario.mensaje == "Pass"){
                        mensajeError.value = "Contraseña Incorrecta"
                    }else{
                        insertUsuario(usuario, version)
                    }
                }

                override fun onError(t: Throwable) {
                    mensajeError.value = t.message
                }
            })
    }

    fun insertUsuario(u: Usuario, v: String) {
        roomRepository.insertUsuario(u)
            .delay(3, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {}
                override fun onComplete() {
                    sync(u.iD_Operario, v)
                }

                override fun onError(e: Throwable) {
                    mensajeError.value = e.toString()
                }
            })
    }

    fun logout() {
        roomRepository.deleteSesion()
            .delay(2, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {}
                override fun onComplete() {
                    mensajeSuccess.value = "Close"
                }

                override fun onError(e: Throwable) {
                    mensajeError.value = e.toString()
                }
            })
    }

    fun sync(u: Int, v: String) {
        roomRepository.getRegistrosTask()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<List<Registro>> {
                override fun onSubscribe(d: Disposable) {}
                override fun onComplete() {}
                override fun onNext(t: List<Registro>) {
                    mensajeError.value = "Antes de sincronizar asegurate de enviar tus registros"
                }

                override fun onError(e: Throwable) {
                    roomRepository.deleteSync()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : CompletableObserver {
                            override fun onSubscribe(d: Disposable) {}
                            override fun onError(e: Throwable) {}
                            override fun onComplete() {
                                roomRepository.getSync(u, v)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(object : Observer<Sync> {
                                        override fun onSubscribe(d: Disposable) {}
                                        override fun onComplete() {}
                                        override fun onNext(t: Sync) {
                                            insertSync(t)
                                        }

                                        override fun onError(e: Throwable) {
                                            if (e is HttpException) {
                                                val body = e.response().errorBody()
                                                try {
                                                    val error =
                                                        retrofit.errorConverter.convert(body!!)
                                                    mensajeError.postValue(error!!.Message)
                                                } catch (e1: IOException) {
                                                    e1.printStackTrace()
//                                                    Log.i("TAG", e1.toString())
                                                }
                                            } else {
                                                mensajeError.postValue(e.toString())
                                            }
                                        }
                                    })
                            }
                        })
                }
            })

    }

    private fun insertSync(p: Sync) {
        roomRepository.saveSync(p)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {}
                override fun onError(e: Throwable) {}
                override fun onComplete() {
                    mensajeSuccess.value = "Sincronización Completa"
                }
            })
    }
}