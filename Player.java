import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;




import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;





import java.io.File;

import java.io.IOException;
import java.util.*;

import javafx.stage.Stage;
import javafx.util.Duration;


public class Player implements Logout{


    private Stage stage;
    private Scene scene;
    private Parent root;


    @FXML private Button playButton;
    @FXML private Button pauseButton;
    @FXML private Button forwardSkipButton;
    @FXML private Button backSkipButton;
    @FXML private Button addToPlaylist;
    @FXML private Button removeFromPlaylist;
    @FXML private Button likeTrack;
    @FXML private Button unlikeTrack;
    @FXML private Button playlistButton;
    @FXML private Button logoutButton;


    @FXML private Slider trackProgressSlider;
    @FXML private Slider volumeSlider;

    @FXML private Label trackName;
    @FXML private Label artistName;
    @FXML private Label albumName;

    @FXML private Label currentTrackTime;
    @FXML private Label totalTrackTime;

    @FXML private ImageView trackCoverArt;



    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

    @FXML private AnchorPane playerRootPane;
    @FXML private Label volumeSliderLabel;

    //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=




    private PlaylistManager playlistManager = new PlaylistManager();

    private MediaPlayer player;
    private Media media;

    private boolean isSeeking = false;


    private List<File> playlist = new ArrayList<>();
    private int currentTrackIndex = 0;


    private File directory = new File(PlaylistManager.PLAYLISTS_ROOT_DIR.getPath());

    public void setPlaylistManager(PlaylistManager pm){
        this.playlistManager = pm;

        if(playlist.isEmpty()){
            initializePlayerComponents();
        }
    }

    @FXML
    public void initialize(){

        stylePlayerUI();

        volumeSlider.setValue(100);

        volumeSlider.valueProperty().addListener(o ->{
            if(player != null){
                player.setVolume(volumeSlider.getValue() / 100.0);
            }
        });

        playButton.setOnAction(e -> { if(player != null) player.play(); });
        pauseButton.setOnAction((e -> { if(player != null) player.pause(); }));

        forwardSkipButton.setOnAction(this::skipForward);
        backSkipButton.setOnAction(this::skipBack);

        likeTrack.setOnAction(this::handleLikeTrack);
        unlikeTrack.setOnAction(this::handleUnlikeTrack);
        addToPlaylist.setOnAction(this::handleAddToPlaylist);
        removeFromPlaylist.setOnAction(this::handleRemoveFromPlaylist);

        playlistButton.setOnAction(e -> {
            try {
                openPlaylistManager();
            } catch (IOException ex) {
                AlertUtil.showAlert("Error", "Failed to open playlist manager: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        logoutButton.setOnAction(this::logout);

        initializePlayerComponents();


    }




    private void initializePlayerComponents(){

        collectAudioFiles(directory);

        if(!playlist.isEmpty()){
            loadTrackAtCurrentIndex();

            if(player != null){
                player.setVolume(volumeSlider.getValue() / 100.0);
            }
        }
        else{
            AlertUtil.showAlert("Info", "No audio files found in the 'Playlists' directory.");
        }


    }



    public void startPlayback(List<File> newPlaylist, int startIndex){

        if(newPlaylist == null || newPlaylist.isEmpty()){
            AlertUtil.showAlert("Playback Error", "Cannot play an empty playlist.");
            return;
        }

        this.playlist.clear();
        this.playlist.addAll(newPlaylist);
        this.currentTrackIndex = startIndex;

        loadTrackAtCurrentIndex();
    }




    private void loadTrackAtCurrentIndex(){

        if(player != null){
            player.stop();
            player.dispose();
        }

        if (playlist.isEmpty()) {
            AlertUtil.showAlert("Playback", "Current playlist is empty. No track to load.");
            trackName.setText("No Track");
            artistName.setText("");
            albumName.setText("");
            trackCoverArt.setImage(null);
            currentTrackTime.setText("00:00");
            totalTrackTime.setText("00:00");
            trackProgressSlider.setValue(0);
            return;
        }


        File currentTrack = playlist.get(currentTrackIndex);
        media = new Media(currentTrack.toURI().toString());
        player = new MediaPlayer(media);

        setupMediaPlayerListeners();

        player.setOnReady(() -> {

            Duration total = player.getTotalDuration();
            totalTrackTime.setText(formatTime(total));
            trackProgressSlider.setMax(total.toSeconds());


            var metadata = media.getMetadata();

            trackName.setText(metadata.getOrDefault("title", currentTrack.getName()).toString());
            artistName.setText(metadata.getOrDefault("artist", "Unknown Artist").toString());
            albumName.setText(metadata.getOrDefault("album", "Unknown Album").toString());

            if(metadata.containsKey("image")){
                trackCoverArt.setImage((javafx.scene.image.Image) metadata.get("image"));
            }
            else{

                // can add some default cover image
                trackCoverArt.setImage(null);
            }

        });

        player.setOnError(() -> {
            System.err.println("MediaPlayer error for track: " + currentTrack.getAbsolutePath() + " - " + player.getError());
            AlertUtil.showAlert("Playback Error", "Could not play: " + currentTrack.getName() + "\nError: " + player.getError());
            // Attempt to skip to the next track if an error occurs
            skipForward(null);
        });

    }


    private void collectAudioFiles(File dir){
        playlist.clear();

        if(dir.exists() && dir.isDirectory()){
            collectAudioFilesRecursive(dir);
        }
    }

    private void collectAudioFilesRecursive(File dir){
        File[] files = dir.listFiles();

        if(files != null){
            for(File file : files){
                if(file.isDirectory()) {
                    collectAudioFiles(file);
                }
                else if(isAudioFile(file)){
                    playlist.add(file);
                }
            }
        }
    }

    private boolean isAudioFile(File file){
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".aac") || name.endsWith(".m4a");
    }


    private void playTrack(){
        if(player != null){
            player.play();
        }
    }


    private void setupMediaPlayerListeners(){


        player.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (!isSeeking) {
                currentTrackTime.setText(formatTime(newValue));
                trackProgressSlider.setValue(newValue.toSeconds());
            }
        });

        player.setOnEndOfMedia(() -> {
            if(currentTrackIndex < playlist.size() - 1){
                currentTrackIndex++;
                loadTrackAtCurrentIndex();
                playTrack();
            }
            else{

                player.stop();
                currentTrackIndex = 0;
                loadTrackAtCurrentIndex();
            }
        });



        trackProgressSlider.setOnMousePressed(e -> isSeeking = true);;

        trackProgressSlider.setOnMouseDragged(e -> {
            double value = trackProgressSlider.getValue();
            currentTrackTime.setText(formatTime(Duration.seconds(value)));
        });

        trackProgressSlider.setOnMouseReleased(e -> {
            double value = trackProgressSlider.getValue();
            player.seek(Duration.seconds(value));
            isSeeking = false;
        });

    }



    private void skipForward(ActionEvent e){

        if(player != null && !playlist.isEmpty()){

            if(currentTrackIndex < playlist.size() - 1){
                currentTrackIndex++;
            }
            else{
                currentTrackIndex = 0;
            }

            loadTrackAtCurrentIndex();
            player.play();

        }

    }

    private void skipBack(ActionEvent e){

        if(player != null && !playlist.isEmpty()){

            if(currentTrackIndex > 0){
                currentTrackIndex--;
            }
            else{
                currentTrackIndex = playlist.size() - 1;
            }

            loadTrackAtCurrentIndex();
            player.play();

        }
    }

    private void handleLikeTrack(ActionEvent e){

        if(player != null && !playlist.isEmpty()){
            File current = playlist.get(currentTrackIndex);

            boolean added = playlistManager.likeSong(current);

            if (added) {
                AlertUtil.showAlert("Liked", "Song added to Liked Songs.");
            } else {
                AlertUtil.showAlert("Info", "Song is already in Liked Songs.");
            }
        }
        else {
            AlertUtil.showAlert("Error", "No track playing to like.");
        }

    }


    private void handleUnlikeTrack(ActionEvent e){

        if(player != null && !playlist.isEmpty()){

            File current = playlist.get(currentTrackIndex);

            boolean removed = playlistManager.unlikeSong(current);

            if (removed) {
                AlertUtil.showAlert("Unliked", "Song removed from Liked Songs.");
            } else {
                AlertUtil.showAlert("Info", "Song was not in Liked Songs.");
            }
        }
        else {
            AlertUtil.showAlert("Error", "No track playing to unlike.");
        }
    }


    private void handleAddToPlaylist(ActionEvent e){

        if(player == null || playlist.isEmpty()){
            AlertUtil.showAlert("Error", "No track playing to add.");
            return;
        }

        File currentSong = playlist.get(currentTrackIndex);
        Set<String> customPlaylistNames= playlistManager.getCustomPlaylistNames();

        if(customPlaylistNames.isEmpty()){
            AlertUtil.showAlert("Info", "No custom playlists available. Create one in the Playlist Manager first.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(null, FXCollections.observableArrayList(customPlaylistNames));
        dialog.setTitle("Add to Playlist");
        dialog.setHeaderText("Select a playlist to add the current song to:");
        dialog.setContentText("Playlist:");


        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selectedPlaylist -> {

            boolean added = playlistManager.addToCustomPlaylist(selectedPlaylist, currentSong);

            if (added) {
                AlertUtil.showAlert("Success", "'" + currentSong.getName() + "' added to '" + selectedPlaylist + "'.");
            } else {
                AlertUtil.showAlert("Info", "'" + currentSong.getName() + "' is already in '" + selectedPlaylist + "'.");
            }
        });
    }


    private void handleRemoveFromPlaylist(ActionEvent e){

        if (player == null || playlist.isEmpty()) {
            AlertUtil.showAlert("Error", "No track playing to remove.");
            return;
        }

        File currentSong = playlist.get(currentTrackIndex);
        Set<String> allPlaylistNames = playlistManager.getCustomPlaylistNames();

        if(allPlaylistNames.isEmpty()){
            AlertUtil.showAlert("Info", "No playlists available");
            return;
        }


        ChoiceDialog<String> dialog = new ChoiceDialog<>(null, FXCollections.observableArrayList(allPlaylistNames));
        dialog.setTitle("Remove from Playlist");
        dialog.setHeaderText("Select a playlist to remove the current song from:");
        dialog.setContentText("Playlist:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selectedPlaylist -> {

            boolean removed = playlistManager.removeFromCustomPlaylist(selectedPlaylist, currentSong);

            if (removed) {
                AlertUtil.showAlert("Success", "'" + currentSong.getName() + "' removed from '" + selectedPlaylist + "'.");
            } else {
                AlertUtil.showAlert("Info", "'" + currentSong.getName() + "' was not found in '" + selectedPlaylist + "'.");
            }
        });
    }


    //  Just formats the time that will be displayed as the current song time
    public String formatTime(Duration duration){
        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);

        return String.format("%02d:%02d", minutes, seconds);
    }


    public List<File> getPlaylistFiles(){
        return this.playlist;
    }

    public void seekTo(Duration duration){
        if(player != null && duration != null){
            player.seek(duration);
        }
    }



    public void openPlaylistManager() throws IOException {

        File currentPlayingFile = null;
        Duration currentPlayingTime = Duration.ZERO;
        List<File> currentPlaylistCopy = new ArrayList<>();


        if(player != null){

            currentPlayingFile = playlist.get(currentTrackIndex);
            currentPlayingTime = player.getCurrentTime();
            currentPlaylistCopy.addAll(this.playlist);

            player.pause();
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/playlistManagerView.fxml"));
        root = loader.load();

        PlaylistManagerController pmController = loader.getController();
        pmController.setPlaylistManager(playlistManager);

        pmController.setCurrentlyPlayingInfo(currentPlayingFile, currentPlayingTime, currentPlaylistCopy);

        pmController.refreshPlaylistView();

        stage = (Stage) playlistButton.getScene().getWindow();
        scene = new Scene(root);



        stage.setScene(scene);
        stage.show();


    }

    @Override
    public void logout(ActionEvent e){

        if(player != null){
            player.stop();
            player.dispose();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/loginPage.fxml"));
            root = loader.load();
            Login loginController = loader.getController();
            //loginController.setPlaylistManager(playlistManager); // Pass manager back to login

            stage = (Stage) ((Button)e.getSource()).getScene().getWindow();
            scene = new Scene(root);

            //scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        }
        catch (IOException ex) {
            AlertUtil.showAlert("Error", "Failed to switch to login page: " + ex.getMessage());
            ex.printStackTrace();
        }

    }


    //=========================================== Styling =======================================================

    private void stylePlayerUI() {
        // === Label Fonts and Colors ===
        trackName.setFont(Font.font("System", FontWeight.BOLD, 18));
        trackName.setTextFill(Color.WHITE);
        trackName.setEffect(new DropShadow(2, Color.BLACK));
        trackName.setAlignment(Pos.CENTER);

        artistName.setFont(Font.font("System", 14));
        artistName.setTextFill(Color.LIGHTGRAY);
        artistName.setAlignment(Pos.CENTER);

        albumName.setFont(Font.font("System", 14));
        albumName.setTextFill(Color.LIGHTGRAY);
        albumName.setAlignment(Pos.CENTER);

        // Style Playback Buttons
        styleControlButton(playButton);
        styleControlButton(pauseButton);
        styleControlButton(backSkipButton);
        styleControlButton(forwardSkipButton);

        // Style Playlist Buttons
        styleControlButton(likeTrack);
        styleControlButton(unlikeTrack);
        styleControlButton(addToPlaylist);
        styleControlButton(removeFromPlaylist);

        // Style playlistButton and logoutButton separately
        styleControlButton(playlistButton);
        styleControlButton(logoutButton);

        // === Volume Controls ===
        volumeSliderLabel.setFont(Font.font("System", 25));
        volumeSliderLabel.setTextFill(Color.WHITE);

        // === Track Time and Progress ===
        currentTrackTime.setFont(Font.font("System", 12));
        currentTrackTime.setTextFill(Color.LIGHTGRAY);
        totalTrackTime.setFont(Font.font("System", 12));
        totalTrackTime.setTextFill(Color.LIGHTGRAY);

        // === Background of playerRootPane ===
        BackgroundFill bgFill = new BackgroundFill(
            new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0F2027")), // Start with a very dark blue/grey
                new Stop(1, Color.web("#203A43"))), // Transition to a muted blue
            CornerRadii.EMPTY,
            Insets.EMPTY
        );
        playerRootPane.setBackground(new Background(bgFill));
    }

    // Helper method to style buttons without CSS or setStyle()
    private void styleControlButton(Button btn) {
        btn.setFont(Font.font("System", FontWeight.BOLD, 14));
        btn.setTextFill(Color.WHITE);
        // Changed button colors to complement the dark blue/grey gradient
        Color buttonBaseColor = Color.web("#3A4D59"); // A darker, desaturated blue/grey
        Color buttonHoverColor = Color.web("#4F6C7D"); // A slightly lighter, more vibrant blue/grey on hover

        btn.setBackground(new Background(new BackgroundFill(buttonBaseColor, new CornerRadii(5), Insets.EMPTY)));
        btn.setPadding(new Insets(8, 15, 8, 15));
        btn.setEffect(new DropShadow(3, Color.BLACK));

        btn.setOnMouseEntered(e ->
            btn.setBackground(new Background(new BackgroundFill(buttonHoverColor, new CornerRadii(5), Insets.EMPTY)))
        );
        btn.setOnMouseExited(e ->
            btn.setBackground(new Background(new BackgroundFill(buttonBaseColor, new CornerRadii(5), Insets.EMPTY)))
        );
    }

}