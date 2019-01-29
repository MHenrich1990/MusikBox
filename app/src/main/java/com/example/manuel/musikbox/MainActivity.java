package com.example.manuel.musikbox;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.ErrorCallback;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.ImageUri;
import com.spotify.protocol.types.ListItem;
import com.spotify.protocol.types.ListItems;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.lang.reflect.Field;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.widget.ViewFlipper;

import static java.util.concurrent.TimeUnit.SECONDS;


public class MainActivity extends AppCompatActivity {
    private static final String CLIENT_ID = "cb762895e17a4886bdbc47438cf500d7";
    private static final String REDIRECT_URI = "http://manuel.example.com/callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    private static final String TAG = MainActivity.class.getSimpleName();
    private final ErrorCallback mErrorCallback = throwable -> logError(throwable, "Boom!");
    private static final int REQUEST_CODE = 1337;
    public static final String playlist = "spotify:user:m.henrich_v:playlist:4NHfHAWyT9UzsKyd933Gb0" ;
    AuthenticationRequest.Builder builder =
            new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
    public int playListNumber;
    public int counter = 0;
    public double playListDouble = 0.;
    private ViewFlipper viewFlipper;
    public List <ListItems> allPlaylists = new ArrayList<>();
    public String actualPlalistUri;
    private String plFilter = "Salome";
    private SeekBar volumSeekBar;
    private AudioManager audioManager;
    final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        setContentView(R.layout.activity_main);
        viewFlipper = findViewById(R.id.view_flipper);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        initControls();
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .setPreferredImageSize(500)
                        .setPreferredThumbnailImageSize(100)
                        .showAuthView(true)
                        .build();
        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        if (mSpotifyAppRemote.isConnected()){
                            Toast.makeText(MainActivity.this, "Mit Spotify verbunden!", Toast.LENGTH_LONG).show();
                            fetchPlaylist1();
                        }
                    }
                    public void onFailure(Throwable throwable) {
                        Log.e("MyActivity", throwable.getMessage(), throwable);
                        Toast.makeText(MainActivity.this, "Verbindung fehlgeschlagen!", Toast.LENGTH_LONG).show();
                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        String StartNachricht = "Hallo Salome";
        setTextFeld(StartNachricht);
        //Button down = findViewById(R.id.playlistDown);
        //previousView(down);

    }


    @Override
    protected void onStop() {
        super.onStop();
        //mSpotifyAppRemote.getPlayerApi().pause();
        //
    }
    protected void onDestroy(){
        super.onDestroy();
        mSpotifyAppRemote.getPlayerApi().pause();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    private void initControls(){
        try {
            volumSeekBar = (SeekBar) findViewById(R.id.seekBar);
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            volumSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
        catch (Exception e){}
    }
    public void fetchPlaylist1() {
        ContentApi contentApi = mSpotifyAppRemote.getContentApi();
        contentApi.getRecommendedContentItems(ContentApi.ContentType.DEFAULT)
                .setResultCallback(listItems -> contentApi.getChildrenOfItem(listItems.items[1],100,0)
                        .setResultCallback(Bib -> contentApi.getChildrenOfItem(Bib.items[0],100,0)
                                .setResultCallback(allPlayLists -> {
                                    getListItem(allPlayLists);
                                    getListItem(Bib);
                                })));

    }

    public List<ListItems> getListItem(ListItems listItems){
        allPlaylists.add(listItems);
        return allPlaylists;
    }

    public List getPlayListData(String filter) {
        allPlaylists = getListItem(null);
        List<String> playlistName = new ArrayList<>();
        List<String> playlistUris = new ArrayList<>();
        List<ImageUri> playlistImage = new ArrayList<>();
        for (int i = 0; i < allPlaylists.get(0).items.length; ++i) {
            if (allPlaylists.get(0).items[i].title.contains(filter)) {
                playlistName.add(allPlaylists.get(0).items[i].title);
                playlistUris.add(allPlaylists.get(0).items[i].uri);
                playlistImage.add(allPlaylists.get(0).items[i].imageUri);
            }
        }
        List playListData = new ArrayList(){};
        playListData.add(playlistName);
        playListData.add(playlistUris);
        playListData.add(playlistImage);
        return playListData;
    }


    public int setPlaylistNumber (int counter, int listLength){
        Button up = findViewById(R.id.playlistUp);
        Button down = findViewById(R.id.playlistDown);
        if(counter >= listLength-1){
            up.setVisibility(View.INVISIBLE);
            down.setVisibility(View.VISIBLE);
            playListNumber = listLength-1;
            logMessage(Integer.toString(playListNumber));
        }
        else if(counter == 0){
            playListNumber = 0;
            up.setVisibility(View.VISIBLE);
            down.setVisibility(View.INVISIBLE);
            logMessage(Integer.toString(playListNumber));
        }
        else{
            up.setVisibility(View.VISIBLE);
            down.setVisibility(View.VISIBLE);
            playListNumber = counter;
            logMessage(Integer.toString(playListNumber));
        }
        return playListNumber;
    }

    public void previousView(View v) {
        counter = --counter;
        viewFlipper.setInAnimation(this, R.anim.slide_in_right);
        viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
        List pLInfo = getPlayListData(plFilter);
        List pLName = (List<String>)pLInfo.get(0);
        List pLUri = (List<String>)pLInfo.get(1);
        List pLImage = (List<ImageUri>)pLInfo.get(2);
        int playListNumber = setPlaylistNumber(counter,pLName.size());
        if (playListNumber >= pLName.size()-1)
            counter = pLName.size()-1;
        else if (playListNumber <= 0)
            counter = 0;
        setPlaylistPic((ImageUri) pLImage.get(counter));
        setTextFeld((String) pLName.get(counter));
        actualPlalistUri = pLUri.get(counter).toString();
        //showDialog("next",gson.toJson(pLName));
        //setPlaylistPic(playlist.imageUri);
        setTextFeld((String) pLName.get(counter));
        //play.setTag(1, pLUri.get(counter));
        viewFlipper.showNext();
    }

    public void nextView(View v) {
        counter = ++counter;
        viewFlipper.setInAnimation(this, android.R.anim.slide_in_left);
        viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
        List pLInfo = getPlayListData(plFilter);
        List pLName = (List<String>)pLInfo.get(0);
        List pLUri = (List<String>)pLInfo.get(1);
        List pLImage = (List<ImageUri>)pLInfo.get(2);
        int playListNumber = setPlaylistNumber(counter,pLName.size());
        if (playListNumber >= pLName.size()-1)
            counter = pLName.size()-1;
        else if (playListNumber <= 0)
            counter = 0;
        setPlaylistPic((ImageUri) pLImage.get(counter));
        setTextFeld((String) pLName.get(counter));
        actualPlalistUri = pLUri.get(counter).toString();
        //showDialog("next",gson.toJson(pLName));
        //setPlaylistPic(playlist.imageUri);
        setTextFeld((String) pLName.get(counter));
        viewFlipper.showNext();

    }

     public void Play(View view){
         Button Play = findViewById(R.id.playButton);
         if (actualPlalistUri !=null) mSpotifyAppRemote.getPlayerApi().play(actualPlalistUri);
     }

     private void Unpause() {
         mSpotifyAppRemote.getPlayerApi().resume();
     }
     private void Stop() {
         mSpotifyAppRemote.getPlayerApi().pause();
     }
     private void Previous(){
         mSpotifyAppRemote.getPlayerApi().skipPrevious();
     }
     private void Next(){
         mSpotifyAppRemote.getPlayerApi().skipNext();
     }

     public void Stop(View view){
         Button Stop = findViewById(R.id.pauseButton);
         //Toast.makeText(MainActivity.this, "StopButton Clicked", Toast.LENGTH_LONG).show();
         Stop();
     }
     public void Previous(View view){

         Button Previous = findViewById(R.id.zuruckButton);
         //Toast.makeText(MainActivity.this, "StopButton Clicked", Toast.LENGTH_LONG).show();
         Previous();
     }
     public void Next(View view){
         Button Next = findViewById(R.id.vorButton);
         //Toast.makeText(MainActivity.this, "StopButton Clicked", Toast.LENGTH_LONG).show();
         Next();

     }
     public void setTextFeld(String title) {
         TextView Title = findViewById(R.id.SongTitle);
         Title.setText(title);
         //showDialog("Dialog", Uri);
     }
     public void setPlaylistTitle (){
         TextView Title = findViewById(R.id.SongTitle);
         mSpotifyAppRemote.getPlayerApi().subscribeToPlayerState()
                 .setEventCallback(playerState -> {
                     final Track track = playerState.track;
                     if (track != null) Title.setText(track.name);
                 });
     }

     public void setPlaylistPic(ImageUri imageUri){
        ImageView playlistPic = findViewById(R.id.playlistImage);
        mSpotifyAppRemote.getImagesApi().getImage(imageUri)
                .setResultCallback(bitmap -> playlistPic.setImageBitmap(bitmap));
     }

     public void setPlaylistPicture() {

         ImageView playlistPic = findViewById(R.id.playlistImage);
         mSpotifyAppRemote.getPlayerApi().subscribeToPlayerState()
                 .setEventCallback(new Subscription.EventCallback<PlayerState>() {
                     @Override
                     public void onEvent(PlayerState playerState) {
                         final Track track = playerState.track;
                         ImageUri imageUri = track.imageUri;
                         //playlistPic.setImageURI(imageUri);
                         if (imageUri != null) {
                             mSpotifyAppRemote.getImagesApi().getImage(track.imageUri)
                                     .setResultCallback(new CallResult.ResultCallback<Bitmap>() {
                                         @Override
                                         public void onResult(Bitmap bitmap) {
                                             playlistPic.setImageBitmap(bitmap);
                                         }
                                     });
                         }
                     }
                 });
     }

     private void logError(Throwable throwable, String msg) {
         Toast.makeText(this, "Error: " + msg, Toast.LENGTH_SHORT).show();
         Log.e(TAG, msg, throwable);
     }

     private void logMessage(String msg) {
         logMessage(msg, Toast.LENGTH_SHORT);
     }

     private void logMessage(String msg, int duration) {
         Toast.makeText(this, msg, duration).show();
         Log.d(TAG, msg);
     }
     private void showDialog(String title, String message) {
         new AlertDialog.Builder(this)
                 .setTitle(title)
                 .setMessage(message)
                 .create()
                 .show();
     }

}
