package com.dsige.lectura.dominion.data.local.repository

import com.dsige.lectura.dominion.data.local.model.*
import com.dsige.lectura.dominion.helper.Mensaje
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @Headers(
        "Cache-Control: no-cache",
        "Content-Type: application/json"
    )
    @GET("GetLogin")
    fun getLogin(
        @Query("user") user: String,
        @Query("password") password: String,
        @Query("imei") imei: String,
        @Query("version") version: String,
        @Query("token") token: String
    ): Observable<Usuario>

    @Headers(
        "Cache-Control: no-cache",
        "Content-Type: application/json"
    )
    @GET("MigracionAll")
    fun getSync(
        @Query("operarioId") operarioId: Int,
        @Query("version") version: String
    ): Observable<Sync>

    @Headers("Cache-Control: no-cache")
    @POST("SaveRegistro")
    fun sendRegistro(@Body query: RequestBody): Observable<Mensaje>

    @POST("SaveOperarioBattery")
    fun saveOperarioBattery(@Body movil: RequestBody): Observable<Mensaje>

    @POST("SaveOperarioGps")
    fun saveOperarioGps(@Body gps: RequestBody): Observable<Mensaje>

    @Headers("Cache-Control: no-cache")
    @POST("SaveFile")
    fun sendPhotos(@Body body: RequestBody): Observable<String>

    @Headers("Cache-Control: no-cache")
    @POST("SaveCliente")
    fun sendClientes(@Body body: RequestBody): Observable<Mensaje>

    @Headers("Cache-Control: no-cache",
        "Content-Type: application/json")
    @GET("VerificateFileClienteNew")
    fun getVerificateFile(
        @Query("id") file: Int,
        @Query("fecha") fecha: String
    ): Observable<Mensaje>

    @Headers("Cache-Control: no-cache",
        "Content-Type: application/json")
    @GET("VerificarCorte")
    fun getVerificateCorte(@Query("suministro") s: String): Observable<Mensaje>

}