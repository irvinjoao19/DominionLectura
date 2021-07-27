package com.dsige.lectura.dominion.data.local.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.dsige.lectura.dominion.data.local.AppDataBase
import com.dsige.lectura.dominion.data.local.model.*
import com.dsige.lectura.dominion.helper.Mensaje
import com.dsige.lectura.dominion.helper.Util
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.File

class AppRepoImp(private val apiService: ApiService, private val dataBase: AppDataBase) :
    AppRepository {


    override fun getUsuario(): LiveData<Usuario> {
        return dataBase.usuarioDao().getUsuario()
    }

    override fun getUsuarioService(
        usuario: String, password: String, imei: String, version: String, token: String
    ): Observable<Usuario> {
        return apiService.getLogin(usuario, password, imei, version, token)
    }

    override fun getUsuarioId(): Observable<Int> {
        return Observable.create {
            val id = dataBase.usuarioDao().getUsuarioIdTask()
            it.onNext(id)
            it.onComplete()
        }
    }

    override fun insertUsuario(u: Usuario): Completable {
        return Completable.fromAction {
            dataBase.usuarioDao().insertUsuarioTask(u)
        }
    }

    override fun deleteSesion(): Completable {
        return Completable.fromAction {
            dataBase.corteDao().deleteAll()
            dataBase.detalleGrupoDao().deleteAll()
            dataBase.grandesClientesDao().deleteAll()
            dataBase.lecturaDao().deleteAll()
            dataBase.marcaDao().deleteAll()
            dataBase.motivoDao().deleteAll()
            dataBase.photoDao().deleteAll()
            dataBase.reconexionDao().deleteAll()
            dataBase.registroDao().deleteAll()
            dataBase.servicioDao().deleteAll()
            dataBase.usuarioDao().deleteAll()
        }
    }

    override fun deleteSync(): Completable {
        return Completable.fromAction {
            dataBase.corteDao().deleteAll()
            dataBase.detalleGrupoDao().deleteAll()
            dataBase.grandesClientesDao().deleteAll()
            dataBase.lecturaDao().deleteAll()
            dataBase.marcaDao().deleteAll()
            dataBase.motivoDao().deleteAll()
            dataBase.photoDao().deleteAll()
            dataBase.reconexionDao().deleteAll()
            dataBase.registroDao().deleteAll()
            dataBase.servicioDao().deleteAll()
        }
    }

    override fun getSync(u: Int, v: String): Observable<Sync> {
        return apiService.getSync(u, v)
    }

    override fun saveSync(s: Sync): Completable {
        return Completable.fromAction {
            val c1: List<Servicio>? = s.servicios
            if (c1 != null) {
                dataBase.servicioDao().insertServicioListTask(c1)
            }
            val c2: List<SuministroLectura>? = s.suministroLecturas
            if (c2 != null) {
                dataBase.lecturaDao().insertSuministroLecturaListTask(c2)
            }
            val c3: List<SuministroCortes>? = s.suministroCortes
            if (c3 != null) {
                dataBase.corteDao().insertSuministroCortesListTask(c3)
            }
            val c4: List<SuministroReconexion>? = s.suministroReconexiones
            if (c4 != null) {
                dataBase.reconexionDao().insertSuministroReconexionListTask(c4)
            }
            val c5: List<DetalleGrupo>? = s.detalleGrupos
            if (c5 != null) {
                dataBase.detalleGrupoDao().insertDetalleGrupoListTask(c5)
            }
            val c6: List<Motivo>? = s.motivos
            if (c6 != null) {
                dataBase.motivoDao().insertMotivoListTask(c6)
            }

            val c11: List<GrandesClientes>? = s.clientes
            if (c11 != null) {
                dataBase.grandesClientesDao().insertGrandesClientesListTask(c11)
            }
            val c12: List<Marca>? = s.marcas
            if (c12 != null) {
                dataBase.marcaDao().insertMarcaListTask(c12)
            }

        }
    }

    override fun getServices(): Observable<List<Servicio>> {
        return Observable.create {
            val list = ArrayList<Servicio>()
            val services = dataBase.servicioDao().getServicioTask()
            for (s: Servicio in services) {
                when (s.nombre_servicio) {
                    "Lectura" -> {
                        s.size = dataBase.lecturaDao().getSuministroLecturaSize()
                        list.add(s)
                    }
                    "Relectura" -> {
                        s.size = dataBase.lecturaDao().getSuministroRelecturaSize()
                        list.add(s)
                    }
                    "Cortes" -> {
                        s.size = dataBase.corteDao().getSuministroCorteSize()
                        list.add(s)
                    }
                    "Reconexiones" -> {
                        s.size = dataBase.reconexionDao().getSuministroReconexionSize()
                        list.add(s)
                    }
                    "Grandes Clientes" -> {
                        s.size = dataBase.grandesClientesDao().getGrandesClientesSize()
                        list.add(s)
                    }
                }
            }
            it.onNext(list)
            it.onComplete()


        }
    }

    override fun getTipoLectura(): Observable<IntArray> {
        return Observable.create {
            val count = dataBase.lecturaDao().getSuministroLecturaNormalSize()
            val countObservada = dataBase.lecturaDao().getSuministroObservadaSize()
            val countReclamos = dataBase.lecturaDao().getSuministroReclamosSize()
            val data = intArrayOf(count, countObservada, countReclamos)
            it.onNext(data)
            it.onComplete()
        }
    }

    override fun getSuministroLectura(
        estado: Int, activo: Int, observadas: Int
    ): LiveData<List<SuministroLectura>> {
        return dataBase.lecturaDao()
            .getSuministroLectura(estado.toString(), estado, activo, observadas)
    }

    override fun getSuministroCortes(estado: Int, i: Int): LiveData<List<SuministroCortes>> {
        return dataBase.corteDao().getSuministroCortes(estado, i)
    }

    override fun getSuministroReconexion(
        estado: Int, i: Int
    ): LiveData<List<SuministroReconexion>> {
        return dataBase.reconexionDao().getSuministroReconexion(estado, i)
    }

    override fun getSuministroReclamos(e: String, i: Int): LiveData<List<SuministroLectura>> {
        return dataBase.lecturaDao().getSuministroReclamos(e, i)
    }

    override fun getRegistro(orden: Int, tipo: Int, recuperada: Int): LiveData<Registro> {
        return if (recuperada == 10) {
            dataBase.registroDao().getRegistroByOrden(orden, recuperada)
        } else {
            dataBase.registroDao().getRegistroByOrden(orden, tipo)
        }
    }

    override fun getMotivos(): LiveData<List<Motivo>> {
        return dataBase.motivoDao().getMotivos()
    }

    override fun getDetalleGrupoByLectura(estado: Int): LiveData<List<DetalleGrupo>> {
        return dataBase.detalleGrupoDao().getDetalleGrupoByLectura(estado)
    }

    override fun getDetalleGrupoByFirstLectura(lecturaEstado: Int): Observable<DetalleGrupo> {
        return Observable.create {
            val d = dataBase.detalleGrupoDao().getDetalleGrupoByFirstLectura(lecturaEstado)
            it.onNext(d)
            it.onComplete()
        }
    }

    override fun getDetalleGrupoByMotivo(estado: Int, s: String): LiveData<List<DetalleGrupo>> {
        return dataBase.detalleGrupoDao().getDetalleGrupoByMotivo(estado, s)
    }

    override fun getDetalleGrupoByMotivoTask(estado: Int, s: String): Observable<DetalleGrupo> {
        return Observable.create {
            val d = dataBase.detalleGrupoDao().getDetalleGrupoByMotivoTask(estado, s)
            it.onNext(d)
            it.onComplete()
        }
    }

    override fun getDetalleGrupoByParentId(i: Int): LiveData<List<DetalleGrupo>> {
        return dataBase.detalleGrupoDao().getDetalleGrupoByParentId(i)
    }

    override fun getDetalleGrupoById(id: Int): Observable<DetalleGrupo> {
        return Observable.create {
            val d = dataBase.detalleGrupoDao().getDetalleGrupoById(id)
            it.onNext(d)
            it.onComplete()
        }
    }

    override fun insertGps(e: OperarioGps): Completable {
        return Completable.fromAction {
            dataBase.operarioGpsDao().insertOperarioGpsTask(e)
        }
    }

    override fun getSendGps(): Observable<List<OperarioGps>> {
        return Observable.create {
            val gps: List<OperarioGps> = dataBase.operarioGpsDao().getOperarioGpsTask()
            it.onNext(gps)
            it.onComplete()
        }
    }

    override fun saveOperarioGps(e: OperarioGps): Observable<Mensaje> {
        val json = Gson().toJson(e)
//        Log.i("TAG", json)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
        return apiService.saveOperarioGps(body)
    }


    override fun updateEnabledGps(t: Mensaje): Completable {
        return Completable.fromAction {
            dataBase.operarioGpsDao().updateEnabledGps(t.codigo)
        }
    }

    override fun insertBattery(e: OperarioBattery): Completable {
        return Completable.fromAction {
            dataBase.operarioBatteryDao().insertOperarioBatteryTask(e)
        }
    }

    override fun getSendBattery(): Observable<List<OperarioBattery>> {
        return Observable.create {
            val gps: List<OperarioBattery> = dataBase.operarioBatteryDao().getOperarioBatteryTask()
            it.onNext(gps)
            it.onComplete()
        }
    }

    override fun saveOperarioBattery(e: OperarioBattery): Observable<Mensaje> {
        val json = Gson().toJson(e)
//        Log.i("TAG", json)
        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json)
        return apiService.saveOperarioBattery(body)
    }

    override fun updateEnabledBattery(t: Mensaje): Completable {
        return Completable.fromAction {
            dataBase.operarioBatteryDao().updateEnabledBattery(t.codigo)
        }
    }

    override fun getClienteById(clienteId: Int): LiveData<GrandesClientes> {
        return dataBase.grandesClientesDao().getClienteById(clienteId)
    }

    override fun getClienteTaskById(clienteId: Int): Observable<GrandesClientes> {
        return Observable.create {
            val g = dataBase.grandesClientesDao().getClienteByIdTask(clienteId)
            it.onNext(g)
            it.onComplete()
        }
    }

    override fun updateClientes(c: GrandesClientes): Completable {
        return Completable.fromAction {
            dataBase.grandesClientesDao().updateGrandesClientesTask(c)
        }
    }

    override fun getMarca(): LiveData<List<Marca>> {
        return dataBase.marcaDao().getMarcas()
    }

    override fun getClienteFiles(clienteId: Int): Observable<List<String>> {
        return Observable.create {
            val list = ArrayList<String>()
            val c = dataBase.grandesClientesDao().getClienteByIdTask(clienteId)
            if (c.clientePermiteAcceso == "NO") {
                list.add(c.clientePermiteAcceso)
            } else {
                if (c.fotovManoPresionEntrada.isNotEmpty()) {
                    list.add(c.fotovManoPresionEntrada)
                }
                if (c.fotovVolumenSCorreUC.isNotEmpty()) {
                    list.add(c.fotovVolumenSCorreUC)
                }
                if (c.fotovVolumenSCorreMedidor.isNotEmpty()) {
                    list.add(c.fotovVolumenSCorreMedidor)
                }
                if (c.fotovVolumenRegUC.isNotEmpty()) {
                    list.add(c.fotovVolumenRegUC)
                }
                if (c.fotovPresionMedicionUC.isNotEmpty()) {
                    list.add(c.fotovPresionMedicionUC)
                }
                if (c.fotoTiempoVidaBateria.isNotEmpty()) {
                    list.add(c.fotoTiempoVidaBateria)
                }
                if (c.fotovTemperaturaMedicionUC.isNotEmpty()) {
                    list.add(c.fotovTemperaturaMedicionUC)
                }
                if (c.fotoPanomarica.isNotEmpty()) {
                    list.add(c.fotoPanomarica)
                }
                if (c.foroSitieneGabinete.isNotEmpty()) {
                    list.add(c.foroSitieneGabinete)
                }
                if (c.fotoBateriaDescargada.isNotEmpty()) {
                    list.add(c.fotoBateriaDescargada)
                }
                if (c.fotoDisplayMalogrado.isNotEmpty()) {
                    list.add(c.fotoDisplayMalogrado)
                }
                if (c.fotoPorMezclaExplosiva.isNotEmpty()) {
                    list.add(c.fotoPorMezclaExplosiva)
                }
                if (c.fotoTomaLectura.isNotEmpty()) {
                    list.add(c.fotoTomaLectura)
                }
            }

            it.onNext(list)
            it.onComplete()
        }
    }

    override fun sendPhotos(body: RequestBody): Observable<String> {
        return apiService.sendPhotos(body)
    }

    override fun sendCliente(body: RequestBody): Observable<Mensaje> {
        return apiService.sendClientes(body)
    }

    override fun getVerificateFile(clienteId: Int, fecha: String): Observable<Mensaje> {
        return apiService.getVerificateFile(clienteId, fecha)
    }

    override fun closeFileClienteById(id: Int): Completable {
        return Completable.fromAction {
            dataBase.grandesClientesDao().updateClienteEstado(id)
        }
    }

    override fun getGrandesClientes(): LiveData<List<GrandesClientes>> {
        return dataBase.grandesClientesDao().getGrandesClientes()
    }

    override fun suministroLecturaByOrden(orden: Int): Observable<SuministroLectura> {
        return Observable.create {
            val l = dataBase.lecturaDao().suministroLecturaByOrden(orden)
            it.onNext(l)
            it.onComplete()
        }
    }

    override fun getRegistroBySuministroTask(id: Int): Observable<Registro> {
        return Observable.create {
            val r: Registro? = dataBase.registroDao().getRegistroBySuministroTaskNull(id)
            if (r == null) {
                it.onError(Throwable("No hay datos"))
                it.onComplete()
                return@create
            }
            it.onNext(r)
            it.onComplete()
        }
    }

    override fun getSuministroLeft(estado: Int, orden: Int, suministroOrden: Int): Observable<Int> {
        return Observable.create {
            val primero = when (estado) {
                3 -> dataBase.corteDao().getSuministroPrimero()
                4 -> dataBase.reconexionDao().getSuministroPrimero()
                else -> when (estado) {
                    6 -> dataBase.lecturaDao().getSuministroPrimero("1", 1, 1)
                    10 -> dataBase.lecturaDao().getSuministroPrimero(estado.toString(), estado, 0)
                    else -> dataBase.lecturaDao().getSuministroPrimero(estado.toString(), estado, 0)
                }
            }
            if (orden <= primero) {
                val p = when (estado) {
                    3 -> dataBase.corteDao().getSuministroUltimoActivo()
                    4 -> dataBase.reconexionDao().getSuministroUltimoActivo()
                    else -> when (estado) {
                        6 -> dataBase.lecturaDao().getSuministroUltimoActivo("1", 1, 1)
                        10 -> dataBase.lecturaDao()
                            .getSuministroUltimoActivo(estado.toString(), estado, 0)
                        else -> dataBase.lecturaDao()
                            .getSuministroUltimoActivo(estado.toString(), estado, 0)
                    }
                }
                if (p != 0) {
                    it.onNext(p)
                } else {
                    it.onError(Throwable(String.format("%s", "No hay mas suministros")))
                }
            } else {
                val n = when (estado) {
                    3 -> dataBase.corteDao().getSuministroLeftTask(orden)
                    4 -> dataBase.reconexionDao().getSuministroLeftTask(orden)
                    else -> when (estado) {
                        6 -> dataBase.lecturaDao().getSuministroLeftTask("1", 1, 1, orden)
                        10 -> dataBase.lecturaDao()
                            .getSuministroLeftTask(estado.toString(), estado, 0, orden)
                        else -> dataBase.lecturaDao()
                            .getSuministroLeftTask(estado.toString(), estado, 0, orden)
                    }
                }
                if (n != 0) {
                    it.onNext(n)
                } else {
                    it.onError(
                        Throwable(
                            String.format(
                                "No hay orden menor a : %s",
                                suministroOrden
                            )
                        )
                    )
                }
            }
        }
    }

    override fun getSuministroRight(
        estado: Int, orden: Int, suministroOrden: Int
    ): Observable<Int> {
        return Observable.create {
            val ultimo = when (estado) {
                3 -> dataBase.corteDao().getSuministroUltimo()
                4 -> dataBase.reconexionDao().getSuministroUltimo()
                else -> when (estado) {
                    6 -> dataBase.lecturaDao().getSuministroUltimo("1", 1, 1)
                    10 -> dataBase.lecturaDao().getSuministroUltimo(estado.toString(), estado, 0)
                    else -> dataBase.lecturaDao().getSuministroUltimo(estado.toString(), estado, 0)
                }
            }

            if (orden >= ultimo) {
                val p = when (estado) {
                    3 -> dataBase.corteDao().getSuministroPrimeroActivo()
                    4 -> dataBase.reconexionDao().getSuministroPrimeroActivo()
                    else -> when (estado) {
                        6 -> dataBase.lecturaDao().getSuministroPrimeroActivo("1", 1, 1)
                        10 -> dataBase.lecturaDao()
                            .getSuministroPrimeroActivo(estado.toString(), estado, 0)
                        else -> dataBase.lecturaDao()
                            .getSuministroPrimeroActivo(estado.toString(), estado, 0)
                    }
                }
                if (p != 0) {
                    it.onNext(p)
                } else {
                    it.onError(Throwable(String.format("%s", "No hay mas suministros")))
                }
            } else {
                val n = when (estado) {
                    3 -> dataBase.corteDao().getSuministroRightTask(orden)
                    4 -> dataBase.reconexionDao().getSuministroRightTask(orden)
                    else -> when (estado) {
                        6 -> dataBase.lecturaDao().getSuministroRightTask("1", 1, 1, orden)
                        10 -> dataBase.lecturaDao()
                            .getSuministroRightTask(estado.toString(), estado, 0, orden)
                        else -> dataBase.lecturaDao()
                            .getSuministroRightTask(estado.toString(), estado, 0, orden)
                    }
                }
                if (n != 0) {
                    it.onNext(n)
                } else {
                    it.onError(
                        Throwable(
                            String.format(
                                "No hay orden mayor a : %s",
                                suministroOrden
                            )
                        )
                    )
                }
            }
            it.onComplete()
        }
    }

    override fun getRegistroBySuministro(id: Int): LiveData<Registro> {
        return dataBase.registroDao().getRegistroBySuministro(id)
    }

    override fun insertRegistro(r: Registro): Completable {
        return Completable.fromAction {
            r.iD_Operario = dataBase.usuarioDao().getUsuarioIdTask()
            if (r.estado == 1) {
                when {
                    r.tipo <= 2 -> dataBase.lecturaDao()
                        .updateActivoSuministroLectura(r.iD_Suministro, 0)
                    r.tipo == 3 -> dataBase.corteDao()
                        .updateActivoSuministroCortes(r.iD_Suministro, 0)
                    r.tipo == 4 -> dataBase.reconexionDao()
                        .updateActivoSuministroReconexion(r.iD_Suministro, 0)
                }
            }

            val registro: Registro? = dataBase.registroDao().getConfirmRegistro(r.iD_Suministro)
            if (registro == null) {
                dataBase.registroDao().insertRegistroTask(r)
            } else {
                dataBase.registroDao().updateRegistroTask(r)
            }
        }
    }

    override fun updateRegistro(id: Int, tipo: Int, estado: Int): Completable {
        return Completable.fromAction {
            if (estado != 2) {
                when (tipo) {
                    3 -> dataBase.corteDao()
                        .updateActivoSuministroCortes(id, 0)
                    4 -> dataBase.reconexionDao()
                        .updateActivoSuministroReconexion(id, 0)
                    else -> dataBase.lecturaDao()
                        .updateActivoSuministroLectura(id, 0)
                }
            }
            dataBase.registroDao().updateRegistroActive(id, estado)
        }
    }

    override fun getPhotoAllBySuministro(id: Int, tipo: Int, i: Int): LiveData<List<Photo>> {
        return dataBase.photoDao().getPhotoAllBySuministro(id, tipo, i)
    }

    override fun deletePhoto(p: Photo, context: Context): Completable {
        return Completable.fromAction {
            Util.deletePhoto(p.rutaFoto, context)
            dataBase.photoDao().deletePhotoTask(p)
        }
    }

    override fun insertPhoto(p: Photo): Completable {
        return Completable.fromAction {
            val photo = dataBase.photoDao().getExistePhoto(p.rutaFoto)
            if (photo == 0) {
                dataBase.photoDao().insertPhotoTask(p)
            }
        }
    }

    override fun getPhotoTaskFile(id: Int): Observable<List<Photo>> {
        return Observable.create {
            val files = dataBase.photoDao().getPhotoTask(id)
            it.onNext(files)
            it.onComplete()
        }
    }

    override fun getVerificateCorte(s: String): Observable<Mensaje> {
        return apiService.getVerificateCorte(s)
    }

    override fun getSuministroCorteByOrdenTask(orden: Int): Observable<SuministroCortes> {
        return Observable.create {
            val l = dataBase.corteDao().suministroCorteByOrden(orden)
            it.onNext(l)
            it.onComplete()
        }
    }

    override fun getSuministroReconexionByOrdenTask(orden: Int): Observable<SuministroReconexion> {
        return Observable.create {
            val l = dataBase.reconexionDao().suministroReconexionByOrden(orden)
            it.onNext(l)
            it.onComplete()
        }
    }

    override fun getPhotoFirm(id: Int): LiveData<List<Photo>> {
        return dataBase.photoDao().getPhotoFirm(id)
    }

    override fun getRegistroByIdTask(id: Int): Observable<Registro> {
        return Observable.create {
            val r = dataBase.registroDao().getRegistroBySuministroTask(id)
            r.photos = dataBase.photoDao().getPhotoTask(id)
            it.onNext(r)
            it.onComplete()
        }
    }

    override fun sendRegistro(body: RequestBody): Observable<Mensaje> {
        return apiService.sendRegistro(body)
    }

    override fun updateEnableRegistro(t: Mensaje): Completable {
        return Completable.fromAction {
            when (t.codigoRetorno) {
                1, 2 -> dataBase.lecturaDao()
                    .updateActivoSuministroLectura(t.codigo, 0)
                3 -> dataBase.corteDao()
                    .updateActivoSuministroCortes(t.codigo, 0)
                4 -> dataBase.reconexionDao()
                    .updateActivoSuministroReconexion(t.codigo, 0)
                else -> dataBase.lecturaDao()
                    .updateActivoSuministroLectura(t.codigo, 0)
            }
            dataBase.photoDao().updateEnablePhotos(t.codigo)
            dataBase.registroDao().updateRegistroActive(t.codigo, 0)
        }
    }

    override fun getPhotoTaskFiles(context: Context): Observable<List<Photo>> {
        return Observable.create {
            val list = ArrayList<Photo>()
            val files = dataBase.photoDao().getPhotosTask()
            files.forEach { p ->
                val file = File(Util.getFolder(context), p.rutaFoto)
                if (file.exists()) {
                    list.add(p)
                }
            }

            it.onNext(list)
            it.onComplete()
        }
    }

    override fun getRegistrosTask(): Observable<List<Registro>> {
        return Observable.create {
            val r = dataBase.registroDao().getRegistroTask()
            if (r.isEmpty()) {
                it.onError(Throwable("No hay datos disponibles por enviar"))
                it.onComplete()
                return@create
            }
            val list: ArrayList<Registro> = ArrayList()
            for (re: Registro in r) {
                re.photos = dataBase.photoDao().getPhotoTask(re.iD_Suministro)
                list.add(re)
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    override fun getRegistrosLecturasTask(): Observable<List<Registro>> {
        return Observable.create {
            val r = dataBase.registroDao().getRegistroLecturaTask()
            if (r.isEmpty()) {
                it.onError(Throwable("No hay datos disponibles por enviar"))
                it.onComplete()
                return@create
            }
            val list: ArrayList<Registro> = ArrayList()
            for (re: Registro in r) {
                re.photos = dataBase.photoDao().getPhotoTask(re.iD_Suministro)
                list.add(re)
            }
            it.onNext(list)
            it.onComplete()
        }
    }

    override fun getRegistros(): LiveData<List<Registro>> {
        return dataBase.registroDao().getRegistros()
    }

    override fun getPhotos(): LiveData<List<Photo>> {
        return dataBase.photoDao().getPhotos()
    }

    override fun getSuministroLecturaById(id: Int): LiveData<SuministroLectura> {
        return dataBase.lecturaDao().getSuministroLecturaById(id)
    }

    override fun getSuministroCorteById(id: Int): LiveData<SuministroCortes> {
        return dataBase.corteDao().getSuministroCorteById(id)
    }

    override fun getSuministroReconexionById(id: Int): LiveData<SuministroReconexion> {
        return dataBase.reconexionDao().getSuministroReconexionById(id)
    }

    override fun getRecoveredPhotos(): Observable<List<Photo>> {
        return Observable.create {
            val files = dataBase.photoDao().getPhotosTask()
            it.onNext(files)
            it.onComplete()
        }
    }

    override fun getAllPhotos(context: Context): Observable<ArrayList<String>> {
        return Observable.create {
            val filePaths: ArrayList<String> = ArrayList()
//            val directory = Util.getFolder(context)
//            val files = directory.listFiles()
//            if (files!!.isEmpty()) {
//                e.onError(Throwable("No hay fotos registrados"))
//                e.onComplete()
//                return@create
//            }

//            for (i in files.indices) {
//                filePaths.add(files[i].toString())
//            }

            val files = dataBase.photoDao().getPhotosTask(Util.getFecha())
            if (files.isEmpty()) {
                it.onError(Throwable("No hay fotos disponibles por enviar"))
                it.onComplete()
                return@create
            }

            files.forEach { p ->
                val file = File(Util.getFolder(context), p.rutaFoto)
                if (file.exists()) {
                    filePaths.add(file.absolutePath)
                }
            }

            it.onNext(filePaths)
            it.onComplete()
        }
    }
}