package com.dsige.lectura.dominion.ui.activities

import android.graphics.Insets
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.lifecycle.ViewModelProvider
import com.dsige.lectura.dominion.R
import com.dsige.lectura.dominion.data.local.model.Photo
import com.dsige.lectura.dominion.data.viewModel.SuministroViewModel
import com.dsige.lectura.dominion.data.viewModel.ViewModelFactory
import com.dsige.lectura.dominion.helper.Gps
import com.dsige.lectura.dominion.helper.Util
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_firm.*
import javax.inject.Inject

class FirmActivity : DaggerAppCompatActivity(), View.OnClickListener {

    override fun onClick(v: View) {
        save()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var suministroViewModel: SuministroViewModel

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.firm, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.clear -> {
                paintView.clear()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    lateinit var p: Photo

    private var tipo: Int = 0
    private var receive: Int = 0
    private var online: Int = 0
    private var orden: Int = 0
    private var order2: Int = 0
    private var suministro: String = ""
    private var tipoFirma: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firm)
        p = Photo()
        val b = intent.extras
        if (b != null) {
            receive = b.getInt("envioId")
            tipo = b.getInt("tipo")
            online = b.getInt("online")
            orden = b.getInt("orden")
            order2 = b.getInt("orden_2")
            suministro = b.getString("suministro", "")
            tipoFirma = b.getString("tipoFirma", "")
            bindUI()
        }
    }

    private fun bindUI() {
        suministroViewModel =
            ViewModelProvider(this, viewModelFactory).get(SuministroViewModel::class.java)

        setSupportActionBar(toolbar)
        supportActionBar!!.title =
            String.format("Firma del %s", if (tipoFirma == "O") "Operario" else "Cliente")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
            paintView.initNew(windowMetrics)
        } else {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(metrics)
            paintView.init(metrics)
        }



        suministroViewModel.mensajeSuccess.observe(this) {
            Util.toastMensaje(this, it)
            finish()
        }
        suministroViewModel.mensajeError.observe(this) {
            Util.toastMensaje(this, it)
        }

        fabFirma.setOnClickListener(this)
    }

    private fun save(){
        if (paintView.validDraw()) {
            val gps = Gps(this@FirmActivity)
            if (gps.isLocationEnabled()) {
                if (gps.getLatitude().toString() == "0.0" || gps.getLongitude().toString() == "0.0") {
                    gps.showAlert(this@FirmActivity)
                } else {
                    val name = paintView.save(this@FirmActivity,receive, tipo, tipoFirma)
                    p.conformidad = 2
                    p.iD_Suministro = receive
                    p.rutaFoto = name
                    p.fecha_Sincronizacion_Android = Util.getFechaActual()
                    p.tipo = tipo
                    p.estado = 1
                    p.latitud = gps.getLatitude().toString()
                    p.longitud = gps.getLongitude().toString()
                    p.firm = 1
                    p.tipoFirma = tipoFirma
                    suministroViewModel.insertPhoto(p)
                }
            } else {
                gps.showSettingsAlert(this@FirmActivity)
            }
        } else {
            suministroViewModel.setError("Debes de Firmar.")
        }
    }
}