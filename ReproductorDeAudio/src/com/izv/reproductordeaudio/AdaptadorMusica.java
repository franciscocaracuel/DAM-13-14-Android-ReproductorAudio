package com.izv.reproductordeaudio;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AdaptadorMusica extends ArrayAdapter<Cancion>{
	
	private Context contexto;
	private ArrayList<Cancion> lista;
	private Cancion cancionClick;
	
	private TextView tvTitulo, tvDuracion, tvArtista, tvAlbum, tvTamaño;
	private LinearLayout lyAdaptador;
	
	public AdaptadorMusica(Context c, ArrayList<Cancion> l, Cancion cancionClick){
		super(c, R.layout.item_listview, l);
		this.contexto=c;
		this.lista=l;
		this.cancionClick=cancionClick;
	}
	
	public View getView(int posicion, View vista, ViewGroup padre){
		
		//Dibuja las lineas
		if(vista==null){
			LayoutInflater i=(LayoutInflater)contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			vista=i.inflate(R.layout.item_listview, null);
		}
		
		Cancion cancion=lista.get(posicion);
		
		tvTitulo=(TextView)vista.findViewById(R.id.tvLvTitulo);
		tvDuracion=(TextView)vista.findViewById(R.id.tvLvDuracion);
		tvArtista=(TextView)vista.findViewById(R.id.tvLvArtista);
		tvAlbum=(TextView)vista.findViewById(R.id.tvLvAlbum);
		tvTamaño=(TextView)vista.findViewById(R.id.tvLvTamaño);
		lyAdaptador=(LinearLayout)vista.findViewById(R.id.lyAdaptador);
		
		tvTitulo.setText(cancion.getTitulo());
		tvDuracion.setText(cancion.getDuracion());
		tvArtista.setText(cancion.getArtista());
		tvAlbum.setText(cancion.getAlbum());
		tvTamaño.setText(cancion.getTamaño());
		
		if(cancionClick.equals(cancion)){
			lyAdaptador.setBackgroundColor(Color.DKGRAY);
		}else{
			lyAdaptador.setBackgroundColor(Color.TRANSPARENT);
		}
		
		return vista;
		
	}

}
