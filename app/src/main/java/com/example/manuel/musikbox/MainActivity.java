package com.example.manuel.musikbox;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.session.PlaybackState;
import android.provider.DocumentsContract;
import android.service.carrier.CarrierMessagingService;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import com.google.gson.internal.bind.ArrayTypeAdapter;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.ErrorCallback;
import com.spotify.protocol.client.Result;
import com.spotify.protocol.client.RequiredFeatures;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.mappers.jackson.ImageUriJson;
import com.spotify.protocol.types.ImageUri;
import com.spotify.protocol.types.Item;
import com.spotify.protocol.types.LibraryState;
import com.spotify.protocol.types.ListItem;
import com.spotify.protocol.types.ListItems;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.protocol.types.Types;
import com.spotify.protocol.types.Uri;
import com.spotify.protocol.types.UserStatus;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Document;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


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
    public double playListNumber = 0.;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        setContentView(R.layout.activity_main);

            }

    @Override
    protected void onStart() {
        super.onStart();
        String StartNachricht = "Hallo Salome";
        setTextFeld(StartNachricht);
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .setPreferredImageSize(800)
                        .setPreferredThumbnailImageSize(100)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Toast.makeText(MainActivity.this, "Mit Spotify verbunden!", Toast.LENGTH_LONG).show();
                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("MyActivity", throwable.getMessage(), throwable);
                        Toast.makeText(MainActivity.this, "Verbindung fehlgeschlagen!", Toast.LENGTH_LONG).show();
                    }
                });

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
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public void showCurrentPlayerContext(View view) {
        if (view.getTag() != null) {
            showDialog("PlayerContext", gson.toJson(view.getTag()));
        }
    }


//Funktion Filtert Account nach Playlists mit dem Filterwort und Schreibt die URIS in Liste Playlists
     public void playSelectedPlaylist (String filter, double j) {
         mSpotifyAppRemote.getContentApi().getRecommendedContentItems(ContentApi.ContentType.DEFAULT)
         .setResultCallback(listItems -> mSpotifyAppRemote.getContentApi()
             .getChildrenOfItem(listItems.items[1],100, 0)
             .setResultCallback(childListItems -> mSpotifyAppRemote.getContentApi()
                 .getChildrenOfItem(childListItems.items[0],100, 0)
                 .setResultCallback(grandChildListItems -> {
                     List<String> playlistURIS = new ArrayList<>();
                     List<String> playlistname = new ArrayList<>();
                     ListItem[] item;

                     for (int i = 0; i< grandChildListItems.items.length; ++i) {
                         item = grandChildListItems.items;
                         if (item[i].title.contains(filter)) {
                             playlistname.add(item[i].title);
                             playlistURIS.add(item[i].uri);
                         }
                     }
                     int length = playlistURIS.size();
                     int m = (int) j;
                     double doubelLength = (double) length;
                     double minusratio = j/doubelLength-1;
                     int ratioInt = (int)minusratio;
                     int plusratio = m/length;
                     int n;
                     int negOne = -1;
                     int zero = 0;
                     if(m >= length){
                         n = m - (plusratio*length);
                         //logMessage(Integer.toString(ratioInt));
                         mSpotifyAppRemote.getPlayerApi().play(playlistURIS.get(n));
                         //List Data = Arrays.asList(m, doubelLength,plusratio,ratioInt,n);
                         //showDialog("Playlsists", gson.toJson(Data));
                         setTextFeld(playlistname.get(n));
                     }
                     if(j <= negOne){
                         n = m - (ratioInt*length) - 1;
                         //logMessage(Integer.toString(n));
                         //List Data = Arrays.asList(m, doubelLength,ratio,ratioInt,n);
                         //showDialog("Playlsists", gson.toJson(Data));
                         mSpotifyAppRemote.getPlayerApi().play(playlistURIS.get(n));
                         setTextFeld(playlistname.get(n));
                     }
                     else{
                         mSpotifyAppRemote.getPlayerApi().play(playlistURIS.get(m));
                         logMessage("bin in else");
                         setTextFeld(playlistname.get(m));
                     }
                 })
             )
         );
     }

     public void playListUp(View view){
         Button up = findViewById(R.id.playlistUp);
         playListNumber = playListNumber+1.;
         playSelectedPlaylist("Salome", playListNumber);
         //setPlaylistTitle();
         setPlaylistPicture();
         logMessage(Double.toString(playListNumber));
     }
     public void playListDown(View view){
         Button down = findViewById(R.id.playlistDown);
         playListNumber = playListNumber-1.;
         playSelectedPlaylist("Salome", playListNumber);
         //setPlaylistTitle();
         setPlaylistPicture();
         logMessage(Double.toString(playListNumber));
     }

     public void Play(View view){
         Button Play = findViewById(R.id.playButton);
         if (mSpotifyAppRemote.isConnected()){
             mSpotifyAppRemote.getPlayerApi().resume();
             //setPlaylistTitle();
             setPlaylistPicture();
         }
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
     }

     public void setPlaylistTitle (){
         TextView Title = findViewById(R.id.SongTitle);
         mSpotifyAppRemote.getPlayerApi().subscribeToPlayerState()
                 .setEventCallback(playerState -> {
                     final Track track = playerState.track;
                     if (track != null) Title.setText(track.name);
                 });
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
