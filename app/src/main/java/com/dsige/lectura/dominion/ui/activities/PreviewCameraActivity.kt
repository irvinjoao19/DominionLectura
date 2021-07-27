package com.dsige.lectura.dominion.ui.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dsige.lectura.dominion.R
import com.dsige.lectura.dominion.helper.Util
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_preview_camera.*
import java.io.File

class PreviewCameraActivity : AppCompatActivity(), View.OnClickListener {

    override fun onClick(v: View) {
        when (v.id) {
            R.id.imgClose -> finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_camera)
        val b = intent.extras
        if (b != null) {
            bindUI(b.getString("nameImg", ""))
        }
    }

    private fun bindUI(nameImg: String) {
        imgClose.setOnClickListener(this)
        textTile.text = nameImg
        Looper.myLooper()?.let {
            Handler(it).postDelayed({
                val f = File(Util.getFolder(this), nameImg)
                Picasso.get().load(f)
                    .fit()
                    .into(imageView, object : Callback {
                        override fun onSuccess() {
                            progressBar.visibility = View.GONE
                        }

                        override fun onError(e: Exception?) {}
                    })
            }, 800)
        }
    }
}