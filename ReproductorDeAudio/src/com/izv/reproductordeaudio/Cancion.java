package com.izv.reproductordeaudio;

import java.io.Serializable;

public class Cancion implements Serializable{

	private static final long serialVersionUID = 4177467748709910182L;
	private String titulo, album, artista, duracion, tamaño, ruta;
	private long duracionMs;
	
	public Cancion(){
		
	}
	
	public Cancion(String titulo, String album, String artista, String duracion, String tamaño, String ruta){
		this.titulo=titulo;
		this.album=album;
		setArtista(artista);
		setDuracion(duracion);
		setTamaño(tamaño);
		this.ruta=ruta;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getArtista() {
		return artista;
	}

	public void setArtista(String artista) {
		if(artista.equalsIgnoreCase("<unknown>")){
			this.artista="Artista desconocido";
		} else{
			this.artista = artista;			
		}
	}

	public String getDuracion() {
		return duracion;
	}

	public void setDuracion(String duracion) {
		
		this.duracionMs=Long.parseLong(duracion);
		
		//Se convierte la duracion en minutos y se guarda en numero
		double total=((Double.parseDouble(duracion)/1000)/60);
				
		//Se coge la parte entera del numero
		int minutos=(int)total;
		
		//Se coge la parte decimal del numero
		double decimal=total-minutos;
		
		//Se redondea la parte decimal a dos decimales
		decimal=Math.round(decimal*Math.pow(10,2))/Math.pow(10,2);
						
		//Se guarda en decimal un entero con los segundos
		int segundos=(Integer.parseInt((decimal+"").substring(2)))*60/100;	
		
		if(segundos<10){
			this.duracion = minutos+":0"+segundos;
		} else{
			this.duracion = minutos+":"+segundos;
		}
		
	}

	public String getTamaño() {
		return tamaño;
	}

	public void setTamaño(String tamaño) {
		double numero = (Double.parseDouble(tamaño)/1024)/1024;
		this.tamaño = Math.round(numero*Math.pow(10,2))/Math.pow(10,2)+" MB";
	}
	
	public String getRuta(){
		return ruta;
	}
	
	public void setRuta(String ruta){
		this.ruta=ruta;
	}
	
	public long getDuracionMs(){
		return duracionMs;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((album == null) ? 0 : album.hashCode());
		result = prime * result + ((artista == null) ? 0 : artista.hashCode());
		result = prime * result
				+ ((duracion == null) ? 0 : duracion.hashCode());
		result = prime * result + ((ruta == null) ? 0 : ruta.hashCode());
		result = prime * result + ((tamaño == null) ? 0 : tamaño.hashCode());
		result = prime * result + ((titulo == null) ? 0 : titulo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cancion other = (Cancion) obj;
		if (album == null) {
			if (other.album != null)
				return false;
		} else if (!album.equals(other.album))
			return false;
		if (artista == null) {
			if (other.artista != null)
				return false;
		} else if (!artista.equals(other.artista))
			return false;
		if (duracion == null) {
			if (other.duracion != null)
				return false;
		} else if (!duracion.equals(other.duracion))
			return false;
		if (ruta == null) {
			if (other.ruta != null)
				return false;
		} else if (!ruta.equals(other.ruta))
			return false;
		if (tamaño == null) {
			if (other.tamaño != null)
				return false;
		} else if (!tamaño.equals(other.tamaño))
			return false;
		if (titulo == null) {
			if (other.titulo != null)
				return false;
		} else if (!titulo.equals(other.titulo))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Cancion [titulo=" + titulo + ", album=" + album + ", artista="
				+ artista + ", duracion=" + duracion + ", ruta=" + ruta + "]";
	}
	
}
