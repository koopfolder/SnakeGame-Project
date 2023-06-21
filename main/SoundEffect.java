package main;


import java.io.*;
import java.net.URL;
import javax.sound.sampled.*;

public enum SoundEffect {
	DIE("/audios/died.wav"),
	EAT("/audios/eat.wav"),
	CLICK("/audios/click.wav");
	
	public static enum Volume {
		MUTE, LOW, MEDIUM, HIGHT
	}
	public static Volume volume = Volume.LOW;
	private Clip clip;
	public boolean loop = false;
	private boolean loopStarted;
	
	SoundEffect(String soundFileName){
		try {
			URL url = SoundEffect.class.getResource(soundFileName);
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
			clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			
		}catch(UnsupportedAudioFileException e) {
			e.printStackTrace();
		}catch (LineUnavailableException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void play() {
		if(volume != volume.MUTE) {
			if(clip.isRunning()) clip.stop();
			clip.setFramePosition(0);
			clip.start();
		}
	}
	public void loop() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(loop) {
					if(volume != volume.MUTE && !clip.isRunning()) {
						clip.loop(Clip.LOOP_CONTINUOUSLY); //Start playing
					}
					try {
						Thread.sleep(clip.getMicrosecondLength());
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			}
		}).start();
	}
	
	public void startLoop() {
		if(loopStarted != true) {
			loop = true;
			loop();
		}
	}
	
	public void stopLoop() {
		loop = false;
		clip.loop(0);
	}
	
	public void stop() {
		clip.stop();
		clip.setFramePosition(0);
	}
	
	static void init() {
		values();
	}
}