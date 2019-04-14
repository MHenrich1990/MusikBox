package com.example.manuel.musikbox;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.FaceDetector;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.client.ErrorCallback;
import com.spotify.protocol.client.Result;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Empty;
import com.spotify.protocol.types.ImageUri;
import com.spotify.protocol.types.ListItem;
import com.spotify.protocol.types.ListItems;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.widget.ViewFlipper;

import static android.view.View.VISIBLE;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


public class MainActivity extends AppCompatActivity {
    private static final String CLIENT_ID = "cb762895e17a4886bdbc47438cf500d7";
    private static final String REDIRECT_URI = "http://manuel.example.com/callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    private static final String TAG = MainActivity.class.getSimpleName();
    private final ErrorCallback mErrorCallback = throwable -> logError(throwable, "Boom!");
    private static final int REQUEST_CODE = 1337;
    public int playListNumber;
    public int counter = 0;
    private ViewFlipper viewFlipper;
    public List <ListItems> allPlaylists = new ArrayList<ListItems>();
    public String actualPlalistUri;
    private SeekBar volumeSeekBar;
    private AudioManager audioManager;
    final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public Button playButton;
    public Integer nextprevcounter;
    public Integer clicksOnPlay;
    public Integer clicksOnPlayFirstStartup;
    public Integer playListID;
    //Die Werte ab hier musst du eventuell anpssen//
    public Integer ImageSize = 400;
    public Integer ThumbnailSize = 75;
    public String StartNachricht = "Salomes MusikBox";
    public String plFilter = "Salome";
    public List<Object> playListData = new ArrayList<Object>(){};
    public Integer ErrorFlag;
    

    AuthenticationRequest.Builder builder =
            new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

    Connector.ConnectionListener mConnectionListener = new Connector.ConnectionListener() {

        @Override
        public void onConnected(SpotifyAppRemote spotifyAppRemote) {
            //Toast.makeText(MainActivity.this, "Bin verbunden!", Toast.LENGTH_SHORT).show();
            mSpotifyAppRemote = spotifyAppRemote;
            if (mSpotifyAppRemote.isConnected()){
                fetchPlaylist();
            }
            else{
                //Toast.makeText(MainActivity.this, "Fetch Failed!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Throwable error) {
            error.printStackTrace();
            //showDialog(error.getMessage(), gson.toJson(error.getMessage()));
            Button closeButton = findViewById(R.id.closeButton);
            Button playButton = findViewById(R.id.playButton);
            Button nextPlayList = findViewById(R.id.playlistUp);
            Button previousPlayList = findViewById(R.id.playlistDown);
            Button next = findViewById(R.id.vorButton);
            Button prev = findViewById(R.id.zuruckButton);
            playButton.setVisibility(View.INVISIBLE);
            if (closeButton != null) {
                closeButton.setVisibility(VISIBLE);
            }
            nextPlayList.setEnabled(FALSE);
            previousPlayList.setEnabled(FALSE);
            next.setEnabled(FALSE);
            prev.setEnabled(FALSE);
        }
    };
    ConnectionParams mConnectionParams =
            new ConnectionParams.Builder(CLIENT_ID)
                    .setRedirectUri(REDIRECT_URI)
                    .setPreferredImageSize(ImageSize)
                    .setPreferredThumbnailImageSize(ThumbnailSize)
                    .showAuthView(true)
                    .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Display dauerhaft an
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // Display fest im Landscape mode
        builder.setScopes(new String[]{"streaming", "playlist-read-private", "user-library-read"}); //Rechtezusweisung
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        setContentView(R.layout.activity_main);
        viewFlipper = findViewById(R.id.view_flipper);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        initControls();
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

    }
    @Override
    protected void onStart() {
        super.onStart();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Button playButton = findViewById(R.id.playButton);
        //Button nextPlayList = findViewById(R.id.playlistUp);
        //Button previousPlayList = findViewById(R.id.playlistDown);
        //Button next = findViewById(R.id.vorButton);
        //Button prev = findViewById(R.id.zuruckButton);
        //nextPlayList.setEnabled(FALSE);
        //previousPlayList.setEnabled(FALSE);
        //next.setEnabled(FALSE);
        //prev.setEnabled(FALSE);
        //Toast.makeText(MainActivity.this, "On Start ist gesartet!", Toast.LENGTH_SHORT).show();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        SpotifyAppRemote.connect(this, mConnectionParams, mConnectionListener);
        clicksOnPlay = 0;
        clicksOnPlayFirstStartup = 0;
        nextprevcounter = 0;
        playListID = 0;
        playButton.setActivated(false);
        setTextFeld(StartNachricht);
        EnableButtons();
       // pLInfo(plFilter);
    }
    @Override
    protected void onStop() {
        super.onStop();
        //mSpotifyAppRemote.getPlayerApi().pause();
        //
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (mSpotifyAppRemote != null) {
            mSpotifyAppRemote.getPlayerApi().pause();
            mSpotifyAppRemote.disconnect(mSpotifyAppRemote);
            finish();
            System.exit(0);
        }
    }

    private void initControls(){
        try {
            volumeSeekBar = (SeekBar) findViewById(R.id.seekBar);
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumeSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

    public void fetchPlaylist() {
        ContentApi contentApi = mSpotifyAppRemote.getContentApi();
        contentApi.getRecommendedContentItems(ContentApi.ContentType.DEFAULT)
                .setResultCallback(listItems -> contentApi.getChildrenOfItem(listItems.items[1],100,0)
                        .setResultCallback(Bib -> contentApi.getChildrenOfItem(Bib.items[0],100,0)
                                .setResultCallback(allPlayLists -> {
                                    allPlaylists = getListItem(allPlayLists);
                                    getListItem(Bib);
                                    //showDialog("fetchPlayList",gson.toJson(Bib));
                                })));
    }

    public void EnableButtons(){
        Button nextPlayList = findViewById(R.id.playlistUp);
        Button previousPlayList = findViewById(R.id.playlistDown);
        Button next = findViewById(R.id.vorButton);
        Button prev = findViewById(R.id.zuruckButton);
        nextPlayList.setEnabled(TRUE);
        previousPlayList.setEnabled(TRUE);
        next.setEnabled(TRUE);
        prev.setEnabled(TRUE);
        logMessage("Buttons Enabled!!");
    }

    public List<ListItems> getListItem(ListItems listItems){
        try{
            allPlaylists.add(listItems);
        }
        catch (Exception e){
            allPlaylists = null;
        }
        return allPlaylists;
    }

    public List getPlayListData(String filter) {
        List<ListItems> items;
        try {
            //allPlaylists = getListItem(null);
            items = allPlaylists;
            //showDialog("getPlayListData try 1", gson.toJson(items));
        }
        catch (Exception e){
            e.printStackTrace();
            //showDialog("Error getPlayListData", e.getMessage());
            items = null;
        }

        try{
            //showDialog("getPlayListData try 2",gson.toJson(items));
            int length = items.get(0).items.length;
            ListItem item[] = items.get(0).items;
            List<String> playlistName = new ArrayList<String>();
            List<String> playlistUris = new ArrayList<String>();
            List<ImageUri> playlistImage = new ArrayList<ImageUri>();
            //List<Object> playListData = new ArrayList<Object>(){};
            if (!item[0].hasChildren )
            {
                for (int i = 0; i < length ; ++i) {
                    if (item[i].title.contains(filter)) {
                        playlistName.add(item[i].title);
                        playlistUris.add(item[i].uri);
                        playlistImage.add(item[i].imageUri);
                    }
                }
                playListData.add(playlistName);
                playListData.add(playlistUris);
                playListData.add(playlistImage);
                //showDialog("Fetch Done",gson.toJson(item));
            }
        }
        catch (Exception e){
            //showDialog("getPlayListData catch", gson.toJson(items));
            playListData.add(0,"Error");
        }

        //else{
        //    //logMessage(String.valueOf(playListData.size()));
        //    //SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        //    //SpotifyAppRemote.connect(this, mConnectionParams, mConnectionListener);
        //    playListData = null;
        //    showDialog("Fetch Failed",gson.toJson(playListData));
        //}
        return playListData;

    }

    public List<Object> pLInfo(String plFilter){
        try {
            playListData.add(getPlayListData(plFilter));
            logMessage("pLInfo Worked");
        } catch (Exception e) {
            e.printStackTrace();
            //showDialog("Error pLInfo", e.getMessage());
        }
        return playListData;
    }

    public int setPlaylistNumber (int counter, int listLength){
        Button up = findViewById(R.id.playlistUp);
        Button down = findViewById(R.id.playlistDown);
        if(counter >= listLength-1){
            up.setVisibility(View.INVISIBLE);
            down.setVisibility(VISIBLE);
            playListNumber = listLength-1;
            //logMessage(Integer.toString(playListNumber));
        }
        else if(counter == 0){
            playListNumber = 0;
            up.setVisibility(VISIBLE);
            down.setVisibility(View.INVISIBLE);
            //logMessage(Integer.toString(playListNumber));
        }
        else{
            up.setVisibility(VISIBLE);
            down.setVisibility(VISIBLE);
            playListNumber = counter;
            //logMessage(Integer.toString(playListNumber));
        }
        return playListNumber;
    }

    private void Previous(){
         nextprevcounter = ++nextprevcounter;
         Button Play = findViewById(R.id.playButton);
         mSpotifyAppRemote.getPlayerApi().skipPrevious();
         Play.setActivated(TRUE);
     }

    private void Next(){
         nextprevcounter = ++nextprevcounter;
         Button Play = findViewById(R.id.playButton);
         mSpotifyAppRemote.getPlayerApi().skipNext();
         Play.setActivated(TRUE);
     }

    public void previousView(View v) {
        //List<Object> playListData = new ArrayList<Object>(){};
        try {
            playListData = getPlayListData(plFilter);
            //playListData = pLInfo(plFilter);
            //showDialog("next View Try", gson.toJson(playListData));
        }
        catch (Exception e){
            e.printStackTrace();
            //showDialog("Error nextView",e.getMessage());
        }
        if (playListData.get(0) != "Error") {
            playButton = findViewById(R.id.playButton);
            nextprevcounter = 0;
            counter = --counter;
            viewFlipper.setInAnimation(this, R.anim.slide_in_right);
            viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
            List<Object> pLInfo = getPlayListData(plFilter);
            List<String> pLName = (List<String>) pLInfo.get(0);
            List<String> pLUri = (List<String>) pLInfo.get(1);
            List<ImageUri> pLImage = (List<ImageUri>) pLInfo.get(2);
            int playListNumber = setPlaylistNumber(counter, pLName.size());
            if (playListNumber >= pLName.size() - 1)
                counter = pLName.size() - 1;
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
            if (counter - playListID != 0) {
                playButton.setActivated(false);
                playButton.setBackgroundResource(R.drawable.play_circle_regular_wt);
            }
            else if (counter - playListID == 0 && clicksOnPlayFirstStartup != 0){
                playButton.setActivated(true);
                playButton.setBackgroundResource(R.drawable.pause_circle_regular_wt);
            }
            else{
                playButton.setActivated(false);
                playButton.setBackgroundResource(R.drawable.play_circle_regular_wt);
            }
        }
        else {
            playListData.clear();
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
            SpotifyAppRemote.connect(this, mConnectionParams, mConnectionListener);
        }
    }

    public void nextView(View v) {
        try {
            playListData = getPlayListData(plFilter);
            //playListData = pLInfo(plFilter);
            //showDialog("next View Try", gson.toJson(playListData));
        }
        catch (Exception e){
            e.printStackTrace();
            //showDialog("Error nextView",e.getMessage());
        }
        //playListData = pLInfo(plFilter);
        //PlayListData = getPlayListData(plFilter);
        if (playListData.get(0) != "Error") {
            playButton = findViewById(R.id.playButton);
            nextprevcounter = 0;
            //playListChange = ++playListChange;
            counter = ++counter;
            viewFlipper.setInAnimation(this, android.R.anim.slide_in_left);
            viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
            List<Object> pLInfo = getPlayListData(plFilter);
            List<String> pLName = (List<String>) pLInfo.get(0);
            List<String> pLUri = (List<String>) pLInfo.get(1);
            List<ImageUri> pLImage = (List<ImageUri>) pLInfo.get(2);
            int playListNumber = setPlaylistNumber(counter, pLName.size());
            if (playListNumber >= pLName.size() - 1)
                counter = pLName.size() - 1;
            else if (playListNumber <= 0)
                counter = 0;
            setPlaylistPic((ImageUri) pLImage.get(counter));
            setTextFeld((String) pLName.get(counter));
            actualPlalistUri = pLUri.get(counter).toString();
            //showDialog("next",gson.toJson(pLName));
            //setPlaylistPic(playlist.imageUri);
            setTextFeld((String) pLName.get(counter));
            viewFlipper.showNext();
            if (counter - playListID != 0) {
                playButton.setActivated(false);
                playButton.setBackgroundResource(R.drawable.play_circle_regular_wt);
            } else {
                playButton.setActivated(true);
                playButton.setBackgroundResource(R.drawable.pause_circle_regular_wt);
            }
        }
        else {
            playListData.clear();
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
            SpotifyAppRemote.connect(this, mConnectionParams, mConnectionListener);
            //logMessage("try again");
        }
    }

    public void PlayButton(View view){

        playButton = findViewById(R.id.playButton);
        if (playButton.isActivated()) {
            mSpotifyAppRemote.getPlayerApi().pause();
            playButton.setActivated(false);
            playButton.setBackgroundResource(R.drawable.play_circle_regular_wt);
            clicksOnPlay = 0;
        }
        else if (!playButton.isActivated() && (clicksOnPlay != 0 || nextprevcounter!=0 ) && counter-playListID==0 ){
            mSpotifyAppRemote.getPlayerApi().resume();
            playButton.setActivated(true);
            playButton.setBackgroundResource(R.drawable.pause_circle_regular_wt);
        }

        else{
            mSpotifyAppRemote.getPlayerApi().play(actualPlalistUri);
            playButton.setActivated(true);
            playButton.setBackgroundResource(R.drawable.pause_circle_regular_wt);
            clicksOnPlay = ++ clicksOnPlay;
            clicksOnPlayFirstStartup = ++ clicksOnPlayFirstStartup;
        }
        playListID = counter;
        //
    }

    public void PreviousButton(View view){
        Previous();
     }

    public void NextButton(View view){
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

    public void closeApp(View view){
        finish();
        System.exit(0);
    }

}
