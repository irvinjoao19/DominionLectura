package com.dsige.lectura.dominion.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.dsige.lectura.dominion.R
import com.dsige.lectura.dominion.helper.Permission
import com.dsige.lectura.dominion.helper.Util
import com.dsige.lectura.dominion.data.viewModel.UsuarioViewModel
import com.dsige.lectura.dominion.data.viewModel.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import javax.inject.Inject

class LoginActivity : DaggerAppCompatActivity(), View.OnClickListener {

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        var cantidad = 0

        when (requestCode) {
            1 -> {
                for (valor: Int in grantResults) {
                    if (valor == PackageManager.PERMISSION_DENIED) {
                        cantidad += 1
                    }
                }
                if (cantidad > 0) {
                    buttonEnviar.visibility = View.GONE
                    messagePermission()
                } else {
                    buttonEnviar.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonEnviar -> {
                val usuario = editTextUser.text.toString().trim()
                val pass = editTextPass.text.toString().trim()
                if (usuario.isNotEmpty()) {
                    if (pass.isNotEmpty()) {
                        load()
                        if (Build.VERSION.SDK_INT >= 29) {
                            usuarioViewModel.getLogin(
                                usuario, pass, pass, Util.getVersion(this), Util.getVersion(this)
                            )
                        } else {
                            usuarioViewModel.getLogin(
                                usuario,
                                pass,
                                Util.getImei(this),
                                Util.getVersion(this),
                                Util.getVersion(this)
                            )
                        }
                    } else {
                        usuarioViewModel.setError("Ingrese password")
                    }
                } else {
                    usuarioViewModel.setError("Ingrese usuario")
                }
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var usuarioViewModel: UsuarioViewModel
    lateinit var builder: AlertDialog.Builder
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        bindUI()
    }

    private fun permision() {
        if (!Permission.hasPermissions(this@LoginActivity, *Permission.PERMISSIONS)) {
            ActivityCompat.requestPermissions(
                this@LoginActivity, Permission.PERMISSIONS, Permission.PERMISSION_ALL
            )
        }
    }

    private fun bindUI() {
        usuarioViewModel =
            ViewModelProvider(this, viewModelFactory).get(UsuarioViewModel::class.java)
        textViewVersion.text = String.format("Versión %s", Util.getVersion(this))
        buttonEnviar.setOnClickListener(this)

        if (Build.VERSION.SDK_INT >= 23) {
            permision()
        }

        usuarioViewModel.mensajeError.observe(this, {
            if (it != null) {
                closeLoad()
                Util.toastMensaje(this, it)
            }
        })
        usuarioViewModel.mensajeSuccess.observe(this, {
            if (it != null) {
                closeLoad()
                goMainActivity()
            }
        })
    }

    private fun load() {
        builder = AlertDialog.Builder(ContextThemeWrapper(this@LoginActivity, R.style.AppTheme))
        @SuppressLint("InflateParams") val view =
            LayoutInflater.from(this@LoginActivity).inflate(R.layout.dialog_login, null)
        builder.setView(view)
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

    private fun goMainActivity() {
        Util.executeGpsWork(this)
        Util.executeBatteryWork(this)
        Util.executePhotosWork(this)
        startActivity(
            Intent(this, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    private fun messagePermission() {
        val material =
            MaterialAlertDialogBuilder(ContextThemeWrapper(this@LoginActivity, R.style.AppTheme))
                .setTitle("Permisos Denegados")
                .setMessage("Debes de aceptar los permisos para poder acceder al aplicativo.")
                .setPositiveButton("Aceptar") { dialogInterface, _ ->
                    permision()
                    dialogInterface.dismiss()
                }
        material.show()
    }
}