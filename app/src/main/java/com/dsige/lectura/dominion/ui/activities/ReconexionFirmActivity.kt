package com.dsige.lectura.dominion.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsige.lectura.dominion.R
import com.dsige.lectura.dominion.data.local.model.Photo
import com.dsige.lectura.dominion.data.viewModel.SuministroViewModel
import com.dsige.lectura.dominion.data.viewModel.ViewModelFactory
import com.dsige.lectura.dominion.helper.Util
import com.dsige.lectura.dominion.ui.adapters.FirmAdapter
import com.dsige.lectura.dominion.ui.listeners.OnItemClickListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_reconexion_firm.*
import javax.inject.Inject

class ReconexionFirmActivity : DaggerAppCompatActivity(), View.OnClickListener {

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fabFirm ->
                startActivity(
                    Intent(this, FirmActivity::class.java)
                        .putExtra("envioId", receive)
                        .putExtra("tipo", tipo)
                        .putExtra("online", online)
                        .putExtra("orden", orden)
                        .putExtra("orden_2", order2)
                        .putExtra("suministro", suministro)
                        .putExtra("tipoFirma", tipoFirma)
                )
            R.id.fabSend -> {
                if (online == 1) {
                    confirmSend()
                } else {
                    suministroViewModel.updateRegistro(receive, tipo, 1)
                }
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var suministroViewModel: SuministroViewModel
    lateinit var builder: AlertDialog.Builder
    private var dialog: AlertDialog? = null

    private var tipo: Int = 0
    private var receive: Int = 0
    private var online: Int = 0
    private var orden: Int = 0
    private var order2: Int = 0
    private var suministro: String = ""
    private var tipoFirma: String = "C"
    private var titulo: String = ""
    private var estado: Int = 0
    private var fechaAsignacion: String = ""
    private var direccion: String = ""
    private var latitud: String = ""
    private var longitud: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reconexion_firm)
        val b = intent.extras
        if (b != null) {
            receive = b.getInt("envioId")
            tipo = b.getInt("tipo")
            online = b.getInt("online")
            orden = b.getInt("orden")
            order2 = b.getInt("orden_2")
            suministro = b.getString("suministro", "")
            titulo = b.getString("nombre", "")
            estado = b.getInt("estado")
            fechaAsignacion = b.getString("fechaAsignacion", "")
            direccion = b.getString("direccion", "")
            latitud = b.getString("latitud", "")
            longitud = b.getString("longitud", "")
            bindUI()
        }
    }

    private fun bindUI() {
        suministroViewModel =
            ViewModelProvider(this, viewModelFactory).get(SuministroViewModel::class.java)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Reconexión de Firmas"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, PhotoActivity::class.java)
            intent.putExtra("envioId", receive)
            intent.putExtra("nombre", titulo)
            intent.putExtra("orden", orden)
            intent.putExtra("orden_2", order2)
            intent.putExtra("tipo", tipo)
            intent.putExtra("estado", estado)
            intent.putExtra("suministro", suministro)
            intent.putExtra("fechaAsignacion", fechaAsignacion)
            intent.putExtra("direccion", direccion)
            intent.putExtra("latitud", latitud)
            intent.putExtra("longitud", longitud)
            startActivity(intent)
            finish()
        }

        if (online == 0) {
            fabSend.text = String.format("%s", "Guardar")
        }

        val layoutManager = LinearLayoutManager(this)
        val firmAdapter = FirmAdapter(object : OnItemClickListener.PhotoListener {
            override fun onItemClick(f: Photo, view: View, position: Int) {
                showPopupMenu(f,view)
            }
        })
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = firmAdapter


        suministroViewModel.getPhotoFirm(receive).observe(this) {
            openSend(it)
            firmAdapter.addItems(it)
        }

        suministroViewModel.mensajeError.observe(this) {
            closeLoad()
            Util.toastMensaje(this, it)
        }
        suministroViewModel.mensajeSuccess.observe(this) {
            closeLoad()
            siguienteOrden(estado)
            Util.toastMensaje(this, it)
        }


        fabFirm.setOnClickListener(this)
        fabSend.setOnClickListener(this)
        fabSend.visibility = View.GONE
    }

    private fun openSend(p: List<Photo>?) {
        when (p?.size) {
            1 -> {
                fabFirm.visibility = View.GONE
                fabSend.visibility = View.VISIBLE
            }
            else -> {
                fabFirm.visibility = View.VISIBLE
                fabSend.visibility = View.GONE
            }
        }

        if (p != null) {
            for (t: Photo in p) {
                tipoFirma = if (t.tipoFirma == "O") "C" else "O"
            }
        } else {
            tipoFirma = "C"
        }
        //val registro = photoViewModel.getRegistro(order2, tipo)
        //if (registro.codigo_Resultado != "79") {
        //    tipoFirma = "O"
        //    when (p?.size) {
        //        1 -> {
        //            fabFirm.visibility = View.GONE
        //            fabSend.visibility = View.VISIBLE
        //        }
        //    }
        //}
    }

    private fun deletePhoto(p: Photo) {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Mensaje")
            .setMessage(
                String.format(
                    "Deseas eliminar la firma del %s ?.",
                    if (p.tipoFirma == "O") "Operario" else "Cliente"
                )
            )
            .setPositiveButton("Aceptar") { dialog, _ ->
                suministroViewModel.deletePhoto(p, this)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }
        dialog.show()
    }

    private fun load() {
        builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.AppTheme))
        @SuppressLint("InflateParams") val view =
            LayoutInflater.from(this).inflate(R.layout.dialog_login, null)
        val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)
        builder.setView(view)
        textViewTitle.text = String.format("Enviando...")
        dialog = builder.create()
        dialog!!.setCanceledOnTouchOutside(false)
        dialog!!.setCancelable(false)
        dialog!!.show()
    }

    private fun closeLoad() {
        if (dialog != null) {
            if (dialog!!.isShowing) {
                dialog!!.dismiss()
            }
        }
    }

    private fun siguienteOrden(estado: Int) {
        val nombre = when (estado) {
            1 -> "Lectura Normales"
            2 -> "Relectura"
            3 -> "Cortes"
            4 -> "Reconexión"
            6 -> "Lectura Observadas"
            7 -> "Lectura Manuales"
            10 -> "Lectura Recuperadas"
            9 -> "Reclamos"
            else -> {
                ""
            }
        }

        suministroViewModel.getSuministroRight(estado, orden, order2)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Int> {
                override fun onComplete() {}
                override fun onSubscribe(d: Disposable) {}
                override fun onError(e: Throwable) {
                    goListaSuministro(nombre, estado)
                }

                override fun onNext(t: Int) {
                    goListaSuministro(nombre, estado)

                }
            })
    }

    private fun goListaSuministro(title: String, tipo: Int) {
        val intent = Intent(this@ReconexionFirmActivity, SuministroActivity::class.java)
        intent.putExtra("nombre", title)
        intent.putExtra("estado", tipo)
        startActivity(intent)
        finish()
    }

    private fun confirmSend() {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Mensaje")
            .setMessage("Estas seguro de enviar ?")
            .setPositiveButton("SI") { dialog, _ ->
                load()
                suministroViewModel.sendFiles(receive, this)
                dialog.dismiss()
            }
            .setNegativeButton("GUARDAR") { dialog, _ ->
                suministroViewModel.updateRegistro(receive, tipo, 1)
                dialog.cancel()
            }
        dialog.show()
    }

    private fun showPopupMenu(p: Photo, v: View) {
        val popupMenu = PopupMenu(this, v)
        popupMenu.menu.add(0, Menu.FIRST, 0, getText(R.string.ver))
        popupMenu.menu.add(1, Menu.FIRST + 1, 1, getText(R.string.deleteFirm))
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    val intent = Intent(this@ReconexionFirmActivity, PreviewCameraActivity::class.java)
                    intent.putExtra("nameImg", p.rutaFoto)
                    startActivity(intent)
                }
                2 -> deletePhoto(p)

            }
            false
        }
        popupMenu.show()
    }
}