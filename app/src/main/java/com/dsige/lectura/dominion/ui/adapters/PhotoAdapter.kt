package com.dsige.lectura.dominion.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.dsige.lectura.dominion.R
import com.dsige.lectura.dominion.data.local.model.Photo
import com.dsige.lectura.dominion.helper.Util
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.cardview_photo.view.*
import java.io.File

class PhotoAdapter(private var listener: OnItemClickListener) :
    RecyclerView.Adapter<PhotoAdapter.ViewHolder>() {

    private var photos = emptyList<Photo>()

    fun addItems(list: List<Photo>) {
        photos = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.cardview_photo, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(photos[position], listener)
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal fun bind(photo: Photo, listener: OnItemClickListener) = with(itemView) {
            val f = File(Util.getFolder(itemView.context), photo.rutaFoto)
            Picasso.get().load(f)
                .into(imageViewPhoto, object : Callback {
                    override fun onSuccess() {
                        progress.visibility = View.GONE
                    }

                    override fun onError(e: Exception?) {
                        progress.visibility = View.GONE
                        imageViewPhoto.setImageDrawable(
                            ContextCompat.getDrawable(
                                itemView.context,
                                R.drawable.no_imagen
                            )
                        )
                    }
                })
            textViewPhoto.text = photo.rutaFoto
            itemView.setOnClickListener { v -> listener.onItemClick(photo, v, bindingAdapterPosition) }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(photo: Photo, view: View, position: Int)
    }
}