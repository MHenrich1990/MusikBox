package com.example.manuel.musikbox;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;


public class MainActivity extends AppCompatActivity {
    private static final String CLIENT_ID = "cb762895e17a4886bdbc47438cf500d7";
    private static final String REDIRECT_URI = "http://manuel.example.com/callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    // Request code will be used to verify if result comes from the login activity. Can be set to any integer.
    private static final int REQUEST_CODE = 1337;
    final String[] setPlaylist = {""};

    AuthenticationRequest.Builder builder =
            new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

    public void TextFeld(String Title) {
        int input1 = R.id.textView;
        TextView Track = (TextView)findViewById(input1);
        Track.setText(Title);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        setContentView(R.layout.activity_main);

        Button playlist1 = (Button)findViewById(R.id.Playlist1);
        Button playlist2 = (Button)findViewById(R.id.Playlist2);
        Button playlist3 = (Button)findViewById(R.id.Playlist3);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Playlist URI");
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setPlaylist[0] = input.getText().toString();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });



        playlist1.setOnLongClickListener(
                new Button.OnLongClickListener() {
                    public boolean onLongClick (View V){
                        Toast.makeText(MainActivity.this, "Button Held", Toast.LENGTH_LONG).show();
                        builder.show();
                        return true;
                    }
                }
        );
        playlist1.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View view){
                        Play(1);
                    }
                }
        );
        playlist2.setOnLongClickListener(
                new Button.OnLongClickListener() {
                    public boolean onLongClick (View V){
                        builder.show();
                        //Toast.makeText(MainActivity.this, "Button Held", Toast.LENGTH_LONG).show();
                        return true;
                    }
                }
        );
        playlist2.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View view){
                        Play(2);
                    }
                }
        );
        playlist3.setOnLongClickListener(
                new Button.OnLongClickListener() {
                    public boolean onLongClick (View V){
                        builder.show();
                        //Toast.makeText(MainActivity.this, "Button Held", Toast.LENGTH_LONG).show();
                        return true;
                    }
                }
        );
        playlist3.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View view){
                        Play(3);
                    }
                }
        );
    }
    @Override
    protected void onStart() {
        super.onStart();
        String StartNachricht = "Hallo";
        TextFeld(StartNachricht);
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
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
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
    private void Play(int Playlist) {

        // Play a playlist
        if (Playlist == 1){
        mSpotifyAppRemote.getPlayerApi().play(setPlaylist[0]);
        }
        if (Playlist == 2) {
            mSpotifyAppRemote.getPlayerApi().play(setPlaylist[0]);
        }
        if (Playlist == 3) {
            mSpotifyAppRemote.getPlayerApi().play(setPlaylist[0]);
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
                            TextFeld(trackName);

                        }
                    }
                });
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


    public void UnPause(View view){
        Button Play = findViewById(R.id.Play);
        //Toast.makeText(MainActivity.this, "StopButton Clicked", Toast.LENGTH_LONG).show();
        Unpause();
    }
    public void Stop(View view){
        Button Stop = findViewById(R.id.Stop);
        //Toast.makeText(MainActivity.this, "StopButton Clicked", Toast.LENGTH_LONG).show();
        Stop();
    }
    public void Previous(View view){

        Button Previous = findViewById(R.id.Previous);
        //Toast.makeText(MainActivity.this, "StopButton Clicked", Toast.LENGTH_LONG).show();
        Previous();
    }
    public void Next(View view){
        Button Next = findViewById(R.id.Next);
        //Toast.makeText(MainActivity.this, "StopButton Clicked", Toast.LENGTH_LONG).show();
        Next();

    }
}
