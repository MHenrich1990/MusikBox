package com.example.manuel.musikbox;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.ErrorCallback;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.ListItem;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
    private static final String CLIENT_ID = "cb762895e17a4886bdbc47438cf500d7";
    private static final String REDIRECT_URI = "http://manuel.example.com/callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    private static final String TAG = MainActivity.class.getSimpleName();
    private final ErrorCallback mErrorCallback = throwable -> logError(throwable, "Boom!");
    // Request code will be used to verify if result comes from the login activity. Can be set to any integer.
    private static final int REQUEST_CODE = 1337;
    ArrayList<String> playlistURIS = new ArrayList<>();
    //final ArrayList<ImageUri> playlistImages = new ArrayList<com.spotify.protocol.types.ImageUri>();
    public static final String playlist = "spotify:user:m.henrich_v:playlist:4NHfHAWyT9UzsKyd933Gb0" ;




    AuthenticationRequest.Builder builder =
            new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);


    public void setTextFeld(String Title) {
        int input1 = R.id.SongTitle;
        TextView Track = findViewById(input1);
        Track.setText(Title);
    }
    public void setPlaylistPicture (String imgURI) {
        int input = R.id.playlistImage;
        ImageView playlistPic = findViewById(input);
        //playlistPic.setImageResource(imgURI);
        Picasso.get()
                .load(imgURI)
                .resize(250,250)
                .into(playlistPic);
    }

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
        String StartNachricht = "Hallo";
        setTextFeld(StartNachricht);
        List<List<String>> Bib = filterPlaylist("Salome");
        //SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Toast.makeText(MainActivity.this, "Mit Spotify verbunden!", Toast.LENGTH_LONG).show();
                        // Now you can start interacting with App Remote
                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("MyActivity", throwable.getMessage(), throwable);
                        Toast.makeText(MainActivity.this, "Verbindung fehlgeschlagen!", Toast.LENGTH_LONG).show();

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });

    }

    @Override
    protected void onStop() {
        super.onStop();
        mSpotifyAppRemote.getPlayerApi().pause();
        //
    }
    protected void onDestroy(){
        super.onDestroy();
        //SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public void showCurrentPlayerContext(View view) {
        if (view.getTag() != null) {
            showDialog("PlayerContext", gson.toJson(view.getTag()));
        }
    }

    public void showCurrentPlayerState(View view) {
        if (view.getTag() != null) {
            showDialog("PlayerState", gson.toJson(view.getTag()));
        }
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

//Funktion Filtert Account nach Playlists mit dem Filterwort und Schreibt die URIS in Liste Playlists
        public List<List<String>> filterPlaylist (String filter) {
            String Filter = filter;
            ArrayList playlistURI = null;
            ArrayList playlistImage = null;
            List<List<String>> result = null;
            mSpotifyAppRemote.getContentApi().getRecommendedContentItems(ContentApi.ContentType.DEFAULT)
                .setResultCallback(listItems -> mSpotifyAppRemote.getContentApi()
                        .getChildrenOfItem(listItems.items[1],100, 0)
                        .setResultCallback(childListItems -> mSpotifyAppRemote.getContentApi()
                                .getChildrenOfItem(childListItems.items[0],100, 0)
                                .setResultCallback(grandChildListItems -> {
                                    //showDialog("Dialog",  gson.toJson(grandChildListItems));

                        for (int i = 0; i< grandChildListItems.items.length; ++i){
                            ListItem item = grandChildListItems.items[i];
                            if (item.title.contains(Filter)){
                                logMessage(String.format("Trying to play %s", item.title));
                                // mSpotifyAppRemote.getPlayerApi().play(item.uri);
                                playlistURI.add(item.uri);
                                playlistImage.add(item.imageUri);
                                List<String> playlistURIS = new ArrayList<>();
                                List<String> playlistImages = new ArrayList<>();

                                result.add(playlistURIS);
                                result.add(playlistImages);



                            }
                            else {
                                //logMessage(String.format("No Playlist with Salome. Actual Playlist is called: %s", item.title));
                                item = null;
                            }
                        }
                    })));
            return result;
        }
    public void selectPlaylist(int index){
        filterPlaylist("Salome");
        List<List<String>> Bib = filterPlaylist("Salome");
        List playlist = Bib.get(0);
        List image = Bib.get(1);
        //Play a playlist
        //List playlist = Bib.get(0);
        //List image = Bib.get(1);
        if (index == 1) {
            //mSpotifyAppRemote.getPlayerApi().play(playlist.get(0).toString());
            //setPlaylistPicture(image.get(0).toString());
            setPlaylistPicture("http://i.imgur.com/DvpvklR.png");
            logMessage("Spiele Playlist1");
        }
        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(new Subscription.EventCallback<PlayerState>() {

                    public void onEvent(PlayerState playerState) {
                        final Track track = playerState.track;
                        if (track != null) {
                            String trackName= track.name;
                            String artistName = track.artist.name;
                            setTextFeld(trackName);

                        }
                    }
                });
    }
    public void Play(View view){
        Button Play = findViewById(R.id.playButton);
        //Toast.makeText(MainActivity.this, "StopButton Clicked", Toast.LENGTH_LONG).show();
        selectPlaylist(1);
    }
//Die Funktion hier funktioniert verstehe sie aber noch nicht
    //public void onGetFitnessRecommendedContentItems(View view) {
     //   mSpotifyAppRemote.getContentApi()
       //         .getRecommendedContentItems(ContentApi.ContentType.DEFAULT)
       //         .setResultCallback(listItems -> mSpotifyAppRemote.getContentApi()
       //                 .getChildrenOfItem(listItems.items[0], 100, 0)
       //                 .setResultCallback(childListItems -> {
       //                     showDialog("RecommendedContentItems", gson.toJson(childListItems));
       //                     ListItem item = null;
       //                     for (int i = 0; i < childListItems.items.length; ++i) {
       //                         item = childListItems.items[i];
       //                         if (item.title.contains("Salome") ) {
       //                             logMessage(String.format("Trying to play %s", item.title));
       //                             mSpotifyAppRemote.getPlayerApi().play(item.uri);
       //                             break;
       //                         } else {
       //                             item = null;
       //                         }
       //                     }
       //                 })
       //                 .setErrorCallback(mErrorCallback)).setErrorCallback(mErrorCallback);
   // }

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
}
