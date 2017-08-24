package com.izv.reproductordeaudio;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private MediaPlayer mp;
	private ArrayList<Cancion> lista;
	private Cancion cancion;
	private ListView lvCancion;
	private TextView tvNotificacionTitulo, tvDuracionTotal, tvDuracion, tvBarra;
	private Button btPlay, btSonido, btBajar, btSubir, btFondoControles;
	private SeekBar sbProgreso, sbVol;
	private AudioManager am;
	private MediaRecorder mr;
	private int vol;
	private LinearLayout lyControles;
	private HiloProgreso hp;
	private ImageView iv;
	
	private int posicion=0;
	private boolean preparado=false, grabando=false;;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		inicio();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    
	    if(preparado){
		    mp.release();
	    	hp.cancel(true);
	    }
	    
	}
	
	public void inicio(){
		
		//INICIALIZACION DE VARIABLES
		lista= new ArrayList<Cancion>();
		lvCancion=(ListView)findViewById(R.id.lvCancion);
		tvNotificacionTitulo=(TextView)findViewById(R.id.tvNotificacionTitulo);
		tvDuracionTotal=(TextView)findViewById(R.id.tvDuracionTotal);
		tvDuracion=(TextView)findViewById(R.id.tvDuracion);
		tvBarra=(TextView)findViewById(R.id.tvBarra);
		tvBarra.setVisibility(View.INVISIBLE);
		iv=(ImageView)findViewById(R.id.ivGrabando);
		iv.setVisibility(View.INVISIBLE);
	
		sbProgreso=(SeekBar)findViewById(R.id.sbProgreso);
		sbProgreso.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				if(preparado){
				hp.cancel(true);
				mp.pause();
				mp.seekTo(sbProgreso.getProgress());
				mp.start();
				hp=new HiloProgreso();
				hp.execute();
				btPlay.setBackgroundResource(R.drawable.pause);
				}
				
				return false;
			}   
			
		});
		
		sbVol=(SeekBar)findViewById(R.id.sbVol);
		sbVol.setMax(15);
		sbVol.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				am.setStreamVolume(AudioManager.STREAM_MUSIC, sbVol.getProgress(), AudioManager.FLAG_SHOW_UI);
				setSonido();
				return false;
			}   
			
		});

		btBajar=(Button)findViewById(R.id.btBajar);
		btSubir=(Button)findViewById(R.id.btSubir);
		btSubir.setVisibility(View.INVISIBLE);
		
		btFondoControles=(Button)findViewById(R.id.btFondoControles);
		lyControles=(LinearLayout)findViewById(R.id.lyControles);
		
		btPlay=(Button)findViewById(R.id.btPlay);
		
		btSonido=(Button)findViewById(R.id.btSonido);
		am=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
		am.requestAudioFocus(new Foco(),AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);

		vol=am.getStreamVolume(AudioManager.STREAM_MUSIC);
		setSonido();
								
		//Mete en el arraylist las canciones
		setCanciones();
		
		//Pone en el listview las canciones del arraylist
		loadLista();
		
		//Escuchador cuando se pulse una cancion
		lvCancion.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> av, View view, int pos, long id) {
				
				posicion=pos;
				cancion=lista.get(pos);
				loadLista();				
				
				if(preparado){
					hp.cancel(true);
					mp.reset();
				} 
								
				reproducir();				
				
			}
			
		});
				
	}
	
	public void play(View v){
		
		setSonido();
		
		//Si no esta reproduciendo
		if(!mp.isPlaying()){
			
			btPlay.setBackgroundResource(R.drawable.pause);
			
			//Si no esta preparado se inicia todo
			if(!preparado){
				reproducir();
			//Si viene del pause
			} else{
				mp.start();
			}
			
		//Si esta reproduciendo se cambia el icono y se pausa
		} else{
			btPlay.setBackgroundResource(R.drawable.play);
			mp.pause();
		}
		
	}
	
	public void anterior(View v){
		
		if(posicion>0){
			posicion--;
		}else{
			posicion=lista.size();
		}

		cancion=lista.get(posicion);
		
		hp.cancel(true);
		mp.reset();
		reproducir();
		
	}
	
	public void siguiente(View v){
		
		if(posicion<lista.size()-1){
			posicion++;
		}else{
			posicion=0;
		}

		cancion=lista.get(posicion);
		
		hp.cancel(true);
		mp.reset();
		reproducir();
		
	}
	
	public void sonido(View v){
		
		if(am.getStreamVolume(AudioManager.STREAM_MUSIC)==0){
			am.setStreamVolume(AudioManager.STREAM_MUSIC, 7, AudioManager.FLAG_SHOW_UI);
		}else{
			am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI);
		}
		
		setSonido();
		
	}
	
	public void grabar(View v){
		
		if(!grabando){
			
			iv.setVisibility(View.VISIBLE);
			
			GregorianCalendar ahora=new GregorianCalendar();
			
			String outputFile = Environment.getExternalStorageDirectory().
				      getAbsolutePath() + "/"+ahora.get(GregorianCalendar.DAY_OF_MONTH)+""+
				      ahora.get(GregorianCalendar.MONTH)+""+
				      ahora.get(GregorianCalendar.YEAR)+"_"+
				      ahora.get(GregorianCalendar.MINUTE)+""+
				      ahora.get(GregorianCalendar.SECOND)+".mp3";

				      mr = new MediaRecorder();
				      mr.setAudioSource(MediaRecorder.AudioSource.MIC);
				      mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				      mr.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
				      mr.setOutputFile(outputFile);
				      
			mr.setOutputFile(outputFile);
					
			try {
		         mr.prepare();
		         mr.start();
		    } catch (IllegalStateException e) {
		    	
		    } catch (IOException e) {
		    	
		    }
			
		}else{
						
			iv.setVisibility(View.INVISIBLE);
			
			mr.stop();
		    mr.release();
		    mr  = null;
		    
		    setCanciones();
		    loadLista();
			
		}
				
	}
	
	public void bajar(View v){
		
		Animation rotar = AnimationUtils.loadAnimation(MainActivity.this,R.anim.rotar);
		rotar.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {

				btBajar.setBackgroundResource(R.drawable.flecha_arriba);

				Animation bajar = AnimationUtils.loadAnimation(MainActivity.this, R.anim.transladar_arriba_abajo);
				bajar.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationEnd(Animation animation) {

						btSubir.setVisibility(View.VISIBLE);

					}
				});

				bajar.reset();

				btBajar.startAnimation(bajar);
				btFondoControles.startAnimation(bajar);
				lyControles.startAnimation(bajar);

				btBajar.setVisibility(View.INVISIBLE);
				btFondoControles.setVisibility(View.INVISIBLE);
				lyControles.setVisibility(View.INVISIBLE);

			}
		});

		btBajar.startAnimation(rotar);

	}
	
	public void subir(View v){
		
		Animation rotar = AnimationUtils.loadAnimation(MainActivity.this,R.anim.rotar);
		rotar.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				
				btBajar.setBackgroundResource(R.drawable.flecha_abajo);

				Animation subir = AnimationUtils.loadAnimation(MainActivity.this, R.anim.transladar_abajo_arriba);
				subir.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationEnd(Animation animation) {

					}
				});

				subir.reset();
				
				btBajar.setVisibility(View.VISIBLE);
				btFondoControles.setVisibility(View.VISIBLE);
				lyControles.setVisibility(View.VISIBLE);
				btSubir.setVisibility(View.INVISIBLE);

				btBajar.startAnimation(subir);
				btFondoControles.startAnimation(subir);
				lyControles.startAnimation(subir);

			}
		});

		btSubir.startAnimation(rotar);
				
	}
	
	public void setCanciones(){
		
		Cursor cur = getContentResolver().query(
				android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null, 
				MediaStore.Audio.Media.IS_MUSIC + " = 1",null, null);
		
		while(cur.moveToNext()){
			
			cancion=new Cancion();
			
			cancion.setTitulo(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE)));
			cancion.setDuracion(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DURATION)));
			cancion.setArtista(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
			cancion.setAlbum(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
			cancion.setTamaño(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.SIZE)));
			cancion.setRuta(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA)));
						
			lista.add(cancion);
			
		}
		
		cur.close();
		
		//Se inicializa cancion a la 1º
		cancion=lista.get(0);
		
	}
	
	public void loadLista(){
		
		AdaptadorMusica adaptador=new AdaptadorMusica(this, lista, cancion);
		
		registerForContextMenu(lvCancion);
		lvCancion.setAdapter(adaptador);

		lvCancion.setSelection(posicion-1);
				
	}
	
	public void reproducir(){
		
		try {
			
			setSonido();
			loadLista();
			
			tvNotificacionTitulo.setText(cancion.getTitulo());
			tvDuracionTotal.setText(cancion.getDuracion());
			tvBarra.setVisibility(View.VISIBLE);
			btPlay.setBackgroundResource(R.drawable.pause);
			
			preparado=true;
			
			mp.setDataSource(cancion.getRuta());
			
			mp.prepareAsync();
			
			sbProgreso.setMax((int)cancion.getDuracionMs());
			hp=new HiloProgreso();
			hp.execute();
			
		} catch (IllegalArgumentException e) {
			
		} catch (SecurityException e) {

		} catch (IllegalStateException e) {

		} catch (IOException e) {

		}
		
	}
	
	private void setSonido(){
		
		vol=am.getStreamVolume(AudioManager.STREAM_MUSIC);
		
		sbVol.setProgress(vol);
		
		//Si el vol esta a 0 se le cambiara el icono al sonido
		if(vol==0){
			btSonido.setBackgroundResource(R.drawable.sonido_off);
		} else{
			btSonido.setBackgroundResource(R.drawable.sonido_on);
		}
		
	}
	
	private class Preparado implements OnPreparedListener {
		
		@Override
		public void onPrepared(MediaPlayer arg0) {
			mp.start();

		}
		
	}
	
	private class Fin implements OnCompletionListener {
		
		@Override
		public void onCompletion(MediaPlayer mp) {
			siguiente(null);			
		}
		
	}
	
	private class HiloProgreso extends AsyncTask<Void, Integer, Boolean>{

		@Override
		protected Boolean doInBackground(Void... params) {
						
			while(!isCancelled()){
				
				try {
					Thread.sleep(10);
					publishProgress(mp.getCurrentPosition());
				} catch (InterruptedException e) {
					break;
				}
				
			}
			
			return true;
			
		}
		
		@Override
		protected void onProgressUpdate(Integer... v){
			
			int valor=v[v.length-1];
			sbProgreso.setProgress(valor);
			
			tvDuracion.setText(getDuracionParcial(valor));
			
		}
		
		public String getDuracionParcial(int valor){
			
			//Se convierte la duracion en minutos y se guarda en numero
			double total=((double)valor/1000)/60;
					
			//Se coge la parte entera del numero
			int minutos=(int)total;
			
			//Se coge la parte decimal del numero
			double decimal=total-minutos;
			
			//Se redondea la parte decimal a dos decimales
			decimal=Math.round(decimal*Math.pow(10,2))/Math.pow(10,2);
							
			//Se guarda en decimal un entero con los segundos
			int segundos=(Integer.parseInt((decimal+"").substring(2)))*60/100;	
			
			if(segundos<10){
				return minutos+":0"+segundos;
			} else{
				return minutos+":"+segundos;
			}
			
		}
		
	}

	private class Foco implements OnAudioFocusChangeListener {

		@Override
		public void onAudioFocusChange(int focusChange) {
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:
				mp.setVolume(0.5f, 0.5f);
				break;
			case AudioManager.AUDIOFOCUS_LOSS:
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				mp.setVolume(0.1f, 0.1f);
				break;
			}
		}
	}

}

