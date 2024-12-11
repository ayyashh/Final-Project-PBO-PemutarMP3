import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class MusicPlayer extends PlaybackListener {

    private static final Object playSignal = new Object();

    private MusicPlayerGUI musicPlayerGUI;
    private Song currentSong;

    public Song getCurrentSong(){
        return currentSong;
    }

    private AdvancedPlayer advancedPlayer;
    private boolean isPaused;
    private boolean songFinished;
    private int currentFrame;

    public void setCurrentFrame(int Frame){
        currentFrame = Frame;
    }

    private int currentTimeInMilli;

    public void setCurrentTimeInMilli(int TimeInMilli){
        currentTimeInMilli = TimeInMilli;
    }

    public MusicPlayer(MusicPlayerGUI musicPlayerGUI){
        this.musicPlayerGUI = musicPlayerGUI;
    }

    public void loadSong(Song song){
        currentSong = song;

        if(!songFinished){
            stopSong();
        }

        if(currentSong != null){

            currentFrame = 0;
            currentTimeInMilli = 0;

            musicPlayerGUI.setPlaybackSliderValue(0);

            playCurrentSong();
        }
    }

    public void pauseSong(){

        if(advancedPlayer != null){
            isPaused = true;
        }

        stopSong();
    }

    public void stopSong(){

        if(advancedPlayer != null) {
            advancedPlayer.stop();
            advancedPlayer.close();
            advancedPlayer = null;
        }
    }

    public void playCurrentSong(){
        try{

            FileInputStream fileInputStream = new FileInputStream(currentSong.getFilePath());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            advancedPlayer = new AdvancedPlayer(bufferedInputStream);
            advancedPlayer.setPlayBackListener(this);

            startMusicThread();

            startPlaybackSliderThread();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void startMusicThread(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    if(isPaused){

                        synchronized(playSignal){
                            isPaused = false;

                            playSignal.notify();
                        }

                        advancedPlayer.play(currentFrame, Integer.MAX_VALUE);
                    }else{

                        advancedPlayer.play();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startPlaybackSliderThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (isPaused){
                    try{
                        synchronized (playSignal){
                            playSignal.wait();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                while(!isPaused){
                    try{
                        currentTimeInMilli++;

                        int calculatedFrame = (int) ((double) currentTimeInMilli * 2.08 * currentSong.getFrameRatePerMs());

                        musicPlayerGUI.setPlaybackSliderValue(calculatedFrame);

                        Thread.sleep(1);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void playbackStarted(PlaybackEvent evt) {

        System.out.println("Music Start");    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        super.playbackFinished(evt);

        System.out.println("Music Stop");

        if(isPaused){
            currentFrame += (int) ((double)evt.getFrame() * currentSong.getFrameRatePerMs());
        }

    }
}
