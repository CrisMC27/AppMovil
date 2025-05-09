package com.example.appmovil

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class FotosAdapter(private val fotos: List<Bitmap>) : RecyclerView.Adapter<FotosAdapter.FotoViewHolder>() {

    class FotoViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val imageView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_foto, parent, false) as ImageView
        return FotoViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        holder.imageView.setImageBitmap(fotos[position])
    }

    override fun getItemCount() = fotos.size
}
