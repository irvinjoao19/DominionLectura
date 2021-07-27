package com.dsige.lectura.dominion.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.dsige.lectura.dominion.R
import com.dsige.lectura.dominion.data.local.model.GrandesClientes
import com.dsige.lectura.dominion.data.local.model.Marca
import com.dsige.lectura.dominion.data.local.model.MenuPrincipal
import com.dsige.lectura.dominion.data.viewModel.ClienteViewModel
import com.dsige.lectura.dominion.data.viewModel.ViewModelFactory
import com.dsige.lectura.dominion.helper.Gps
import com.dsige.lectura.dominion.helper.Util
import com.dsige.lectura.dominion.ui.adapters.MarcAdapter
import com.dsige.lectura.dominion.ui.adapters.MenuItemAdapter
import com.dsige.lectura.dominion.ui.listeners.OnItemClickListener
import com.google.android.material.button.MaterialButton
import dagger.android.support.DaggerFragment
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_general_client.*
import java.io.File
import java.util.ArrayList
import javax.inject.Inject

private const val ARG_PARAM1 = "param1"

class GeneralClientFragment : DaggerFragment(), View.OnClickListener, TextWatcher {

    override fun afterTextChanged(s: Editable?) {
        if (editTextMezclaExplosiva.text.toString() == "0") {
            fabCameraMezclaExplosiva.visibility = View.GONE
        } else {
            fabCameraMezclaExplosiva.visibility = View.VISIBLE
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun onClick(v: View) {
        validateCliente()
        when (v.id) {
            R.id.editTextCliente -> dialogSpinner("Cliente", 1)
            R.id.editTextCorrector -> dialogSpinner("Marca de Corrector", 2)
            R.id.editTextCabinete -> dialogSpinner("Tiene Gabinete de tememetria ? ", 3)
            R.id.editTextPresentaCliente -> dialogSpinner("Presenta Cliente ?", 4)
            R.id.editTextExisteMedidor -> dialogSpinner("Existe Medidor ?", 5)
            R.id.buttonEMR -> {
                c.fechaRegistroInicio = Util.getFechaActual()
                clienteViewModel.updateCliente(c, "Registro Inicio Guardado", requireContext())
            }
            R.id.fabCameraCliente -> dialogMenuPhotoGalery(v, 1, 1, 1)
            R.id.fabCameraMezclaExplosiva -> dialogMenuPhotoGalery(v, 2, 0, 11)
            R.id.fabCameraTomaLectura -> dialogMenuPhotoGalery(v, 3, 0, 14)
            R.id.fabCameraValorPresionEntrada -> dialogMenuPhotoGalery(v, 4, 0, 2)
            R.id.fabCameraVolumenSinCMedidor -> dialogMenuPhotoGalery(v, 5, 0, 3)
            R.id.fabCameraBateriaDescarga -> dialogMenuPhotoGalery(v, 6, 0, 12)
            R.id.fabCameraDisplayMalogrado -> dialogMenuPhotoGalery(v, 7, 0, 13)
            R.id.fabCameraVolumenSCorregirUC -> dialogMenuPhotoGalery(v, 8, 0, 4)
            R.id.fabCameraVolumenRegistradoUC -> dialogMenuPhotoGalery(v, 9, 0, 5)
            R.id.fabCameraPresionMedicionUC -> dialogMenuPhotoGalery(v, 10, 0, 6)
            R.id.fabCameraTemperaturaUC -> dialogMenuPhotoGalery(v, 11, 0, 7)
            R.id.fabCameraTiempoVidaBateria -> dialogMenuPhotoGalery(v, 12, 0, 8)
            R.id.fabFotoPanoramica -> dialogMenuPhotoGalery(v, 13, 0, 9)
            R.id.fabCameraCabinete -> dialogMenuPhotoGalery(v, 14, 0, 10)
            R.id.fabRegister -> {
                val gps = Gps(requireContext())
                if (gps.isLocationEnabled()) {
                    if (gps.getLatitude().toString() != "0.0" || gps.getLongitude()
                            .toString() != "0.0"
                    ) {
                        if (clienteViewModel.validateCliente(c, 15, 0)) {
                            c.estado = 7
                            c.latitud = gps.getLatitude().toString()
                            c.longitud = gps.getLongitude().toString()
                            c.comentario = editTextComentario.text.toString()
                            clienteViewModel.updateCliente(
                                c,
                                "Cliente Actualizado",
                                requireContext()
                            )
                            load()
                        }
                    }
                } else {
                    gps.showAlert(requireContext())
                }
            }
        }
    }


    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var clienteViewModel: ClienteViewModel
    private var viewPager: ViewPager? = null
    private var clienteId: Int = 0

    lateinit var builder: AlertDialog.Builder
    private var dialog: AlertDialog? = null

    lateinit var folder: File
    private lateinit var image: File
    private var nameImg: String = ""
    private var direction: String = ""

    lateinit var c: GrandesClientes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        c = GrandesClientes()
        arguments?.let {
            clienteId = it.getInt(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_general_client, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
    }

    private fun bindUI() {
        clienteViewModel =
            ViewModelProvider(this, viewModelFactory).get(ClienteViewModel::class.java)

        viewPager = requireActivity().findViewById(R.id.viewPager)
        clienteViewModel.getClienteById(clienteId).observe(viewLifecycleOwner) {
            c = it
            when (it.clientePermiteAcceso) {
                "SI" -> editTextCliente.setText(String.format("%s", "Permite acceso"))
                "NO" -> {
                    editTextCliente.setText(String.format("%s", "No permite acceso"))
                    fabCameraCliente.visibility = View.VISIBLE
                    linearLayoutCliente.visibility = View.GONE
                }
            }

            if (it.marcaCorrectorId != 0) {
                editTextCorrector.setText(it.marcaCorrectorIdNombre)
                if (it.marcaCorrectorId != 1) {
                    c.fotovTemperaturaMedicionUC = it.fotovTemperaturaMedicionUC
                    c.fotoTiempoVidaBateria = it.fotoTiempoVidaBateria
                    fabCameraTemperaturaUC.visibility = View.VISIBLE
                    fabCameraTiempoVidaBateria.visibility = View.VISIBLE
                }
            }


            editTextCabinete.setText(it.tieneGabinete)
            editTextPresentaCliente.setText(it.presenteCliente)
            if (it.presenteCliente.isNotEmpty()) {
                if (it.presenteCliente == "SI") {
                    textViewContacto.visibility = View.VISIBLE
                    editTextContacto.setText(it.contactoCliente)
                }
            }

            editTextCodigoEMR.setText(it.codigoEMR)
            editTextMezclaExplosiva.setText(it.porMezclaExplosiva)
            editTextTomaLectura.setText(it.tomaLectura)
            editTextValorPresionEntrada.setText(it.vManoPresionEntrada)
            editTextVolumenSCorregirUC.setText(it.vVolumenSCorreUC)
            editTextVolumenSinCMedidor.setText(it.vVolumenSCorreMedidor)
            editTextVolumenRegistradoUC.setText(it.vVolumenRegUC)
            editTextPresionMedicionUC.setText(it.vPresionMedicionUC)
            editTextTemperaturaUC.setText(it.vTemperaturaMedicionUC)
            editTextTiempoVidaBateria.setText(it.tiempoVidaBateria)
            editTextComentario.setText(it.comentario)
            editTextExisteMedidor.setText(it.existeMedidor)
            textView16.visibility = View.VISIBLE
            if (it.existeMedidor == "NO") {
                textView16.visibility = View.GONE
            }

            showCliente(it)
        }


        buttonEMR.setOnClickListener(this)
        editTextCliente.setOnClickListener(this)
        editTextCorrector.setOnClickListener(this)
        editTextCabinete.setOnClickListener(this)
        editTextPresentaCliente.setOnClickListener(this)
        editTextExisteMedidor.setOnClickListener(this)
        fabCameraCliente.setOnClickListener(this)
        fabCameraMezclaExplosiva.setOnClickListener(this)
        fabCameraTomaLectura.setOnClickListener(this)
        fabCameraBateriaDescarga.setOnClickListener(this)
        fabCameraDisplayMalogrado.setOnClickListener(this)
        fabCameraValorPresionEntrada.setOnClickListener(this)
        fabCameraVolumenSCorregirUC.setOnClickListener(this)
        fabCameraVolumenSinCMedidor.setOnClickListener(this)
        fabCameraVolumenRegistradoUC.setOnClickListener(this)
        fabCameraPresionMedicionUC.setOnClickListener(this)
        fabCameraTemperaturaUC.setOnClickListener(this)
        fabCameraTiempoVidaBateria.setOnClickListener(this)
        fabFotoPanoramica.setOnClickListener(this)
        fabCameraCabinete.setOnClickListener(this)
        fabRegister.setOnClickListener(this)
        editTextMezclaExplosiva.addTextChangedListener(this)

        clienteViewModel.mensajeError.observe(viewLifecycleOwner, { s ->
            if (dialog != null) {
                if (dialog!!.isShowing) {
                    dialog!!.dismiss()
                }
            }
            if (s == "Confirmar lectura") {
                confirmLectura()
            }
            Util.toastMensaje(requireContext(), s)
        })

        clienteViewModel.mensajeSuccess.observe(viewLifecycleOwner, { s ->
            Util.toastMensaje(requireContext(), s)
            if (s == "Cliente Actualizado") {
                if (dialog != null) {
                    if (dialog!!.isShowing) {
                        dialog!!.dismiss()
                    }
                }
                viewPager?.currentItem = 1
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Int) =
            GeneralClientFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                }
            }
    }

    private fun dialogMenuPhotoGalery(view: View, type: Int, format: Int, orden: Int) {
        if (clienteViewModel.validateCliente(c, type, format)) {
            val popupMenu = PopupMenu(requireContext(), view)
            popupMenu.menu.add(0, 1, 0, getText(R.string.takePhoto))
            popupMenu.menu.add(0, 2, 0, getText(R.string.galery))
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> createImage(orden)
                    2 -> {
                        nameImg = ""
                        galery(orden)
                    }
                }
                false
            }
            popupMenu.show()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun createImage(tipo: Int) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireContext().packageManager) != null) {
            folder = Util.getFolder(requireContext())
            nameImg = Util.getFechaForGrandesCliente(editTextCodigoEMR.text.toString())
            image = File(folder, nameImg)
            direction = "$folder/$nameImg"
            val uriSavedImage = Uri.fromFile(image)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage)

            if (Build.VERSION.SDK_INT >= 24) {
                try {
                    val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
                    m.invoke(null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

//            resultPhotoLauncher.launch(takePictureIntent)
            startActivityForResult(takePictureIntent, tipo)
        }
    }

    private fun galery(orden: Int) {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.type = "image/*"
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
//        resultGalleryLauncher.launch(i)
        startActivityForResult(i, orden)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (nameImg.isEmpty()) {
                if (data != null) {
                    clienteViewModel.generarArchivo(
                        editTextCodigoEMR.text.toString(), requireContext(), data
                    )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Observer<String> {
                            override fun onSubscribe(d: Disposable) {}
                            override fun onComplete() {}
                            override fun onNext(t: String) {
                                updatePhotoGalery(requestCode, t)
                            }

                            override fun onError(e: Throwable) {
                                Util.toastMensaje(requireContext(), "Volver a intentarlo")
                            }
                        })
                } else {
                    Util.toastMensaje(requireContext(), "Volver a seleccionar imagen")
                }
            } else {
                generateImage(requestCode)
            }
        }
    }

    private fun generateImage(code: Int) {
        Util.generateImageAsync(requireContext(), direction)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {}
                override fun onComplete() {
                    updatePhotoGalery(code, nameImg)
                }

                override fun onError(e: Throwable) {
                    Util.toastMensaje(requireContext(), e.message.toString())
                }
            })
    }

    private fun updatePhotoGalery(code: Int, nameImg: String) {
        when (code) {
            1 -> {
                c.fotoConstanciaPermiteAcceso = nameImg
                clienteViewModel.updateCliente(
                    c, "Foto de cliente permite acceso guardado", requireContext()
                )
            }
            2 -> {
                c.fotovManoPresionEntrada = nameImg
                clienteViewModel.updateCliente(c, "Foto presión entrada guardada", requireContext())
            }
            3 -> {
                c.fotovVolumenSCorreMedidor = nameImg
                clienteViewModel.updateCliente(
                    c,
                    "Foto sin corregir medidor guardada.",
                    requireContext()
                )
            }
            4 -> {
                c.fotovVolumenSCorreUC = nameImg
                clienteViewModel.updateCliente(
                    c, "Foto sin corregir unidad correctora guardada", requireContext()
                )
            }
            5 -> {
                c.fotovVolumenRegUC = nameImg
                clienteViewModel.updateCliente(
                    c, "Foto registrador de la unidad correctora guardada", requireContext()
                )
            }
            6 -> {
                c.fotovPresionMedicionUC = nameImg
                clienteViewModel.updateCliente(
                    c,
                    "Foto presion de medición UC guardada",
                    requireContext()
                )
            }
            7 -> {
                c.fotovTemperaturaMedicionUC = nameImg
                clienteViewModel.updateCliente(
                    c, "Foto temperatura medicion UC guardada", requireContext()
                )
            }
            8 -> {
                c.fotoTiempoVidaBateria = nameImg
                clienteViewModel.updateCliente(
                    c,
                    "Foto tiempo de bateria guardada",
                    requireContext()
                )
            }
            9 -> {
                c.fotoPanomarica = nameImg
                clienteViewModel.updateCliente(c, "Foto panoramica Actualizado", requireContext())
            }
            10 -> {
                c.foroSitieneGabinete = nameImg
                clienteViewModel.updateCliente(c, "Foto Gabinete guardada", requireContext())
            }
            11 -> {
                c.fotoPorMezclaExplosiva = nameImg
                clienteViewModel.updateCliente(
                    c,
                    "Foto Por Mezcla Explosiva guardada",
                    requireContext()
                )
            }
            12 -> {
                c.fotoBateriaDescargada = nameImg
                clienteViewModel.updateCliente(
                    c,
                    "Foto Bateria Descargada guardada",
                    requireContext()
                )
            }
            13 -> {
                c.fotoDisplayMalogrado = nameImg
                clienteViewModel.updateCliente(
                    c,
                    "Foto Display Malogrado guardada",
                    requireContext()
                )
            }
            14 -> {
                c.fotoTomaLectura = nameImg
                clienteViewModel.updateCliente(c, "Foto Toma Lectura guardada", requireContext())
            }
        }
    }

    private fun dialogSpinner(title: String, tipo: Int) {
        val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.AppTheme))
        @SuppressLint("InflateParams") val v =
            LayoutInflater.from(context).inflate(R.layout.dialog_combo, null)
        val textViewTitulo: TextView = v.findViewById(R.id.textViewTitulo)
        val recyclerView: RecyclerView = v.findViewById(R.id.recyclerView)
        textViewTitulo.text = title
        val layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )
        builder.setView(v)
        val dialog = builder.create()
        dialog.show()

        if (tipo != 2) {
            val menuAdapter = MenuItemAdapter(object : OnItemClickListener.MenuListener {
                override fun onItemClick(m: MenuPrincipal, v: View, position: Int) {
                    when (tipo) {
                        1 -> {
                            if (m.menuId == 1) {
                                c.clientePermiteAcceso = "SI"
                                fabCameraCliente.visibility = View.GONE
                                linearLayoutCliente.visibility = View.VISIBLE
                            } else {
                                c.clientePermiteAcceso = "NO"
                                fabCameraCliente.visibility = View.VISIBLE
                                linearLayoutCliente.visibility = View.GONE
                            }
                            editTextCliente.setText(m.title)
                        }
                        3 -> {
                            fabCameraCabinete.visibility = View.GONE
                            if (m.menuId == 1) {
                                fabCameraCabinete.visibility = View.VISIBLE
                            }
                            editTextCabinete.setText(m.title)
                        }
                        4 -> {
                            textViewContacto.visibility = View.GONE
                            if (m.menuId == 1) {
                                textViewContacto.visibility = View.VISIBLE
                            }
                            editTextPresentaCliente.setText(m.title)
                        }
                        5 -> {
                            editTextExisteMedidor.setText(m.title)
                            textView16.visibility = View.VISIBLE
                            if (m.title == "NO") {
                                textView16.visibility = View.GONE
                            }
                        }
                    }
                    dialog.dismiss()
                }
            })
            recyclerView.itemAnimator = DefaultItemAnimator()
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = menuAdapter
            when (tipo) {
                1 -> {
                    val menus: ArrayList<MenuPrincipal> = ArrayList()
                    menus.add(MenuPrincipal(1, "Permite acceso", 0, 0))
                    menus.add(MenuPrincipal(2, "No permite acceso", 0, 0))
                    menuAdapter.addItems(menus)
                }
                else -> {
                    val menus: ArrayList<MenuPrincipal> = ArrayList()
                    menus.add(MenuPrincipal(1, "SI", 0, 0))
                    menus.add(MenuPrincipal(2, "NO", 0, 0))
                    menuAdapter.addItems(menus)
                }
            }
        } else {
            val marcAdapter = MarcAdapter(object : OnItemClickListener.MarcaListener {
                override fun onItemClick(m: Marca, v: View, position: Int) {
                    fabCameraTemperaturaUC.visibility = View.GONE
                    fabCameraTiempoVidaBateria.visibility = View.GONE
                    if (m.marcaMedidorId != 1) {
                        fabCameraTemperaturaUC.visibility = View.VISIBLE
                        fabCameraTiempoVidaBateria.visibility = View.VISIBLE
                    }
                    editTextCorrector.setText(m.nombre)
                    c.marcaCorrectorId = m.marcaMedidorId
                    c.marcaCorrectorIdNombre = m.nombre
                    dialog.dismiss()
                }
            })
            recyclerView.adapter = marcAdapter
            clienteViewModel.getMarca().observe(viewLifecycleOwner) {
                marcAdapter.addItems(it)
            }
        }
    }

    private fun validateCliente() {
        c.clienteId = clienteId
        c.codigoEMR = editTextCodigoEMR.text.toString()
        c.porMezclaExplosiva = editTextMezclaExplosiva.text.toString()
        c.vManoPresionEntrada = editTextValorPresionEntrada.text.toString()
        c.vVolumenSCorreUC = editTextVolumenSCorregirUC.text.toString()
        c.vVolumenSCorreMedidor = editTextVolumenSinCMedidor.text.toString()
        c.vVolumenRegUC = editTextVolumenRegistradoUC.text.toString()
        c.vPresionMedicionUC = editTextPresionMedicionUC.text.toString()
        c.vTemperaturaMedicionUC = editTextTemperaturaUC.text.toString()
        c.tiempoVidaBateria = editTextTiempoVidaBateria.text.toString()
        c.tieneGabinete = editTextCabinete.text.toString()
        c.presenteCliente = editTextPresentaCliente.text.toString()
        c.contactoCliente = editTextContacto.text.toString()
        c.existeMedidor = editTextExisteMedidor.text.toString()
        c.tomaLectura = editTextTomaLectura.text.toString()
    }

    private fun showCliente(g: GrandesClientes) {
        if (g.fotoConstanciaPermiteAcceso.isNotEmpty()) {
            fabCameraCliente.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
            )
        }
        if (g.fotovManoPresionEntrada.isNotEmpty()) {
            fabCameraValorPresionEntrada.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
            )
        }
        if (g.fotovVolumenSCorreUC.isNotEmpty()) {
            fabCameraVolumenSCorregirUC.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
            )
        }
        if (g.fotovVolumenSCorreMedidor.isNotEmpty()) {
            fabCameraVolumenSinCMedidor.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
            )
        }
        if (g.fotovVolumenRegUC.isNotEmpty()) {
            fabCameraVolumenRegistradoUC.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
            )
        }
        if (g.fotovPresionMedicionUC.isNotEmpty()) {
            fabCameraPresionMedicionUC.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
            )
        }
        if (g.fotovTemperaturaMedicionUC.isNotEmpty()) {
            fabCameraTemperaturaUC.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
            )
        }
        if (g.fotoTiempoVidaBateria.isNotEmpty()) {
            fabCameraTiempoVidaBateria.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
            )
        }
        if (g.fotoPanomarica.isNotEmpty()) {
            fabFotoPanoramica.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
            )
        }
        if (g.foroSitieneGabinete.isNotEmpty()) {
            fabCameraCabinete.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
            )
        }
        if (g.fotoPorMezclaExplosiva.isNotEmpty()) {
            fabCameraMezclaExplosiva.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
            )
        }
        if (g.fotoTomaLectura.isNotEmpty()) {
            fabCameraTomaLectura.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
            )
        }
        if (g.fotoBateriaDescargada.isNotEmpty()) {
            fabCameraBateriaDescarga.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
            )
        }
        if (g.fotoDisplayMalogrado.isNotEmpty()) {
            fabCameraDisplayMalogrado.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
            )
        }
        editTextVolumenSinCMedidor.isEnabled = true
        if (c.existeMedidor == "NO") {
            editTextVolumenSinCMedidor.isEnabled = false
        }
    }


    private fun load() {
        builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.AppTheme))
        @SuppressLint("InflateParams") val view =
            LayoutInflater.from(context).inflate(R.layout.dialog_login, null)
        builder.setView(view)
        val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)
        textViewTitle.text = String.format("%s", "Enviando...")
        dialog = builder.create()
        dialog!!.setCanceledOnTouchOutside(false)
        dialog!!.setCancelable(false)
        dialog!!.show()
    }

    private fun confirmLectura() {
        val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.AppTheme))
        @SuppressLint("InflateParams") val v =
            LayoutInflater.from(context).inflate(R.layout.dialog_confirm, null)
        val editTextLecturaConfirm = v.findViewById<TextView>(R.id.editTextLecturaConfirm)
        val buttonCancelar: MaterialButton = v.findViewById(R.id.buttonCancelar)
        val buttonAceptar: MaterialButton = v.findViewById(R.id.buttonAceptar)
        builder.setView(v)
        val dialog = builder.create()
        dialog.show()

        buttonAceptar.setOnClickListener {
            if (editTextLecturaConfirm.text.toString().isNotEmpty()) {
                c.vVolumenSCorreUC = editTextVolumenSCorregirUC.text.toString()
                c.confirmarVolumenSCorreUC = editTextLecturaConfirm.text.toString()

                if (clienteViewModel.validateCliente(c, 4, 0)) {
                    clienteViewModel.updateCliente(c, "Validando Relectura", requireContext())
                }
                dialog.dismiss()
            } else {
                editTextLecturaConfirm.error = "Ingrese un valor"
                editTextLecturaConfirm.requestFocus()
            }
        }
        buttonCancelar.setOnClickListener {
            dialog.dismiss()
        }
    }


//    incorporando nuevos metodos para galeria y foto
//
//    private val resultPhotoLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == DaggerAppCompatActivity.RESULT_OK) {
//
//            }
//        }
//
//
//    private val resultGalleryLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == DaggerAppCompatActivity.RESULT_OK) {
//
//            }
//        }

}