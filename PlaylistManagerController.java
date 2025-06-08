import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;



import javafx.geometry.Insets;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;



public class PlaylistManagerController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    private PlaylistManager playlistManager;

    @FXML private Button backButton;
    @FXML private ListView<String> playlistListView;
    @FXML private TableView<Song> songsTableView;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> artistColumn;

    @FXML private Button playPlaylistButton;
    @FXML private Button playSongButton;


    //=====================================================

    @FXML private BorderPane borderPane;
    @FXML private VBox playlistNameVBox;
    @FXML private Label playlistTitle;
    @FXML private HBox playlistRelatedButtonHBox;
    @FXML private Button createButton;
    @FXML private Button deleteButton;
    @FXML private Label playlistTitleLabel;
    @FXML private Button removeSongButton;
    @FXML private Button addSongButton;
    @FXML private HBox addRemoveHBox;
    @FXML private HBox playSongOrPlayliistHbox;
    @FXML private HBox backButtonHBox;


    //=====================================================


    private final ObservableList<String> playlistsObservableList = FXCollections.observableArrayList();
    private final ObservableList<Song> currentSongsObservableList = FXCollections.observableArrayList();

    private File lastPlayingFile;
    private Duration lastPlayingTime;
    private List<File> lastPlayingPlaylist;

    public void setPlaylistManager(PlaylistManager pm){
        this.playlistManager = pm;
        refreshPlaylistView();
    }


    @FXML
    public void initialize(){

        titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        artistColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getArtist()));


        playlistListView.setItems(playlistsObservableList);
        songsTableView.setItems(currentSongsObservableList);

        playlistListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> showSongsForPlaylist(newVal)
        );

        stylePlaylistManagerUI();

    }


    public void refreshPlaylistView(){

        if(playlistManager == null){
            return;
        }

        playlistsObservableList.clear();
        playlistsObservableList.addAll(playlistManager.getAllPlaylistNames().stream().sorted().collect(Collectors.toList()));

        if(!playlistsObservableList.isEmpty()){

            if(playlistListView.getSelectionModel().getSelectedItem() == null || !playlistsObservableList.contains(playlistListView.getSelectionModel().getSelectedItem())){
                playlistListView.getSelectionModel().selectFirst();
            }
        }

        showSongsForPlaylist(playlistListView.getSelectionModel().getSelectedItem());

    }



    private void showSongsForPlaylist(String playlistName){

        currentSongsObservableList.clear();

        if(playlistName != null && playlistManager != null){
            Set<File> songsInPlaylist = playlistManager.getSongsInPlaylist(playlistName);

            // Convert Set to List for indexed access and sorting (if desired)
            List<File> sortedSongs = songsInPlaylist.stream().sorted(Comparator.comparing(File::getName)).collect(Collectors.toList());

            for(File file : sortedSongs){ // Iterate through sorted list
                Media media = new Media(file.toURI().toString());
                MediaPlayer tempPlayer = new MediaPlayer(media);
                tempPlayer.setOnReady(() -> {
                    Map<String, Object> metadata = media.getMetadata();
                    String title = metadata.getOrDefault("title", file.getName()).toString();
                    String artist = metadata.getOrDefault("artist", "Unknown Artist").toString();
                    javafx.application.Platform.runLater(() -> {
                        currentSongsObservableList.add(new Song(file, title, artist));
                    });
                    tempPlayer.dispose();
                });
                tempPlayer.setOnError(() -> {
                    System.err.println("Error loading media for metadata: " + file.getAbsolutePath());
                    javafx.application.Platform.runLater(() -> {
                        currentSongsObservableList.add(new Song(file, file.getName(), "Unknown Artist (Error)"));
                    });
                    tempPlayer.dispose();
                });
            }
        }
    }


    @FXML
    private void handleCreatePlaylist(){

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Playlist");
        dialog.setHeaderText("Enter new Playlist name:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String trimmedName = name.trim();

            if(!trimmedName.isEmpty()){

                if(playlistManager.createCustomPlaylists(trimmedName)){
                    AlertUtil.showAlert("Success", "Playlist '" + trimmedName + "' created.");
                    refreshPlaylistView();
                    playlistListView.getSelectionModel().select(trimmedName);
                }
                else{
                   AlertUtil.showAlert("Error", "Could not create playlist. It might already exist or the name is reserved.");
                }
            }
            else{
                AlertUtil.showAlert("Error", "Playlist name cannot be empty.");
            }
        });

    }


    @FXML
    private void handleDeletePlaylist(){

        String selected = playlistListView.getSelectionModel().getSelectedItem();

        if(selected == null){
            AlertUtil.showAlert("Playlist Manager", "Please select a playlist to delete");
            return;
        }

        if (selected.equals(PlaylistManager.LIKED_PLAYLIST_NAME)) {
            AlertUtil.showAlert("Playlist Manager", "The 'Liked Songs' playlist cannot be deleted.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Playlist '" + selected + "'?");
        confirmation.setContentText("This will permanently delete the playlist and its contents from disk. Are you sure?");

        Optional<ButtonType> result = confirmation.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.OK){

            if(playlistManager.deletePlaylist(selected)){
                AlertUtil.showAlert("Success", "Playlist '" + selected + "' deleted.");
                refreshPlaylistView();
            }
            else{
                AlertUtil.showAlert("Error", "Failed to delete playlist '" + selected + "'.");
            }
        }
    }

    @FXML
    private void handleAddSong(){

        String selectedPlaylist = playlistListView.getSelectionModel().getSelectedItem();

        if(selectedPlaylist == null){
            AlertUtil.showAlert("Playlist Manager", "Select a Playlist first!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Add Songs to " + selectedPlaylist);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac"));
        List<File> files = fileChooser.showOpenMultipleDialog(playlistListView.getScene().getWindow());

        if(files != null){

            int addedCount = 0;

            for(File file : files){

                if(playlistManager.addToCustomPlaylist(selectedPlaylist, file)){
                    addedCount++;
                }
            }

            if (addedCount > 0) {
                AlertUtil.showAlert("Success", addedCount + " song(s) added to '" + selectedPlaylist + "'.");
                showSongsForPlaylist(selectedPlaylist); // Refresh current playlist view
            } else {
                AlertUtil.showAlert("Info", "No new songs were added. They might already be in the playlist or an error occurred.");
            }
        }
    }


    @FXML
    private void handleRemoveSong(){

        Song selectedSong = songsTableView.getSelectionModel().getSelectedItem();
        String selectedPlaylist = playlistListView.getSelectionModel().getSelectedItem();

        if(selectedSong == null || selectedPlaylist == null){
            AlertUtil.showAlert("Playlist Manager", "Select a song or Playlist first");
            return;
        }

        if(playlistManager.removeFromCustomPlaylist(selectedPlaylist, selectedSong.getFile())){
            AlertUtil.showAlert("Success", "'" + selectedSong.getTitle() + "' removed from '" + selectedPlaylist + "'.");
            showSongsForPlaylist(selectedPlaylist);
        }
        else{
            AlertUtil.showAlert("Error", "Failed to remove '" + selectedSong.getTitle() + "' from '" + selectedPlaylist + "'.");
        }
    }



    @FXML
    private void handlePlaySelectedPlaylist() throws IOException{

        String selectedPlaylistName = playlistListView.getSelectionModel().getSelectedItem();
        Song selectedSong = songsTableView.getSelectionModel().getSelectedItem();

        if(selectedPlaylistName == null){
            AlertUtil.showAlert("Play Song", "Please select a playlist first.");
            return;
        }

        if(selectedSong == null){
            AlertUtil.showAlert("Play Song", "Please select a song from the playlist first.");
            return;
        }

        Set<File> songSet = playlistManager.getSongsInPlaylist(selectedPlaylistName);

        if(songSet.isEmpty()){
            AlertUtil.showAlert("Play Song", "The selected playlist is empty.");
            return;
        }


        List<File> playlistToPlay = new ArrayList<>(songSet);

        playlistToPlay.sort(Comparator.comparing(File::getName));

        int startIndex = -1;

        for(int i = 0; i < playlistToPlay.size(); i++){

            if(playlistToPlay.get(i).getName().equals(selectedSong.getFile().getName())){
                startIndex = i;
                break;
            }
        }

        if(startIndex != -1){
            goBackToPlayerScreen(Optional.of(playlistToPlay), OptionalInt.of(startIndex));
        }
        else{
            AlertUtil.showAlert("Play Song", "Selected song not found in the current playlist.");
        }
    }

    @FXML
    private void handlePlaySelectedSong() throws IOException {

        String selectedPlaylistName = playlistListView.getSelectionModel().getSelectedItem();
        Song selectedSong = songsTableView.getSelectionModel().getSelectedItem();

        if (selectedPlaylistName == null) {
            AlertUtil.showAlert("Play Song", "Please select a playlist first.");
            return;
        }
        if (selectedSong == null) {
            AlertUtil.showAlert("Play Song", "Please select a song from the playlist first.");
            return;
        }

        Set<File> songsSet = playlistManager.getSongsInPlaylist(selectedPlaylistName);
        if (songsSet.isEmpty()) {
            AlertUtil.showAlert("Play Song", "The selected playlist is empty.");
            return;
        }

        List<File> playlistToPlay = new ArrayList<>(songsSet);
        // Sort for consistent playback order (e.g., by name)
        playlistToPlay.sort(Comparator.comparing(File::getName));

        int startIndex = -1;
        for(int i = 0; i < playlistToPlay.size(); i++){

            if(playlistToPlay.get(i).getName().equals(selectedSong.getFile().getName())){
                startIndex = i;
                break;
            }
        }

        if (startIndex != -1) {
            goBackToPlayerScreen(Optional.of(playlistToPlay), OptionalInt.of(startIndex));
        } else {
            AlertUtil.showAlert("Play Song", "Selected song not found in the current playlist.");
        }

    }


    public void setCurrentlyPlayingInfo(File file, Duration time, List<File> currentPlaylist){
        this.lastPlayingFile = file;
        this.lastPlayingTime = time;
        this.lastPlayingPlaylist = new ArrayList<>(currentPlaylist);
    }




    public void goBackToPlayerScreen() throws IOException{
        goBackToPlayerScreen(Optional.empty(), OptionalInt.empty());
    }

    public void goBackToPlayerScreen(Optional<List<File>> specificPlaylist, OptionalInt startIndex) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/player.fxml"));
        root = loader.load();

        Player playerController = loader.getController();
        playerController.setPlaylistManager(playlistManager);


        if(specificPlaylist.isPresent()){
            playerController.startPlayback(specificPlaylist.get(), startIndex.getAsInt());

        }
        else{
            if(lastPlayingFile != null && lastPlayingPlaylist != null && !lastPlayingPlaylist.isEmpty()){
                int resumeIndex = -1;

                for(int i = 0; i < lastPlayingPlaylist.size(); i++){

                    try{
                        if(lastPlayingPlaylist.get(i).getCanonicalPath().equals(lastPlayingFile.getCanonicalPath())){
                            resumeIndex = i;
                            break;
                        }
                    }
                    catch(IOException e){
                        System.err.println("Error comparing paths for resume: " + e.getMessage());
                        if(lastPlayingPlaylist.get(i).getName().equals(lastPlayingFile.getName())){
                            resumeIndex = i;
                            break;
                        }
                    }
                }

                if(resumeIndex != -1){
                    playerController.startPlayback(lastPlayingPlaylist, resumeIndex);
                    playerController.seekTo(lastPlayingTime);
                }
                else{
                    playerController.startPlayback(Arrays.asList(lastPlayingFile), 0);
                    playerController.seekTo(lastPlayingTime);
                }

            }
        }

        stage = (Stage) backButton.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


// ================================================= Styling ==========================================================


     private void stylePlaylistManagerUI() {
        // Background Gradient for the main pane (borderPane)
        BackgroundFill bgFill = new BackgroundFill(
            new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0F2027")), // Start with a very dark blue/grey
                new Stop(1, Color.web("#203A43"))), // Transition to a muted blue
            CornerRadii.EMPTY,
            Insets.EMPTY
        );
        borderPane.setBackground(new Background(bgFill));

        // Main Titles and Labels
        playlistTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        playlistTitleLabel.setTextFill(Color.WHITE);
        playlistTitleLabel.setEffect(new DropShadow(2, Color.BLACK));

        // Assuming playlistTitle displays the currently selected playlist name
        playlistTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        playlistTitle.setTextFill(Color.LIGHTGRAY);
        playlistTitle.setEffect(new DropShadow(1, Color.BLACK));


        // ListView Styling
        playlistListView.setBackground(new Background(new BackgroundFill(Color.web("#2E3D4F"), new CornerRadii(5), Insets.EMPTY)));
        // Setting border and inner background for the ListView using CSS
        playlistListView.setStyle("-fx-border-color: #4F6C7D; -fx-border-width: 1; -fx-selection-bar: #4F6C7D; -fx-selection-bar-text: white; -fx-control-inner-background: #2E3D4F;");
        playlistListView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("-fx-background-color: transparent;"); // Transparent background for empty cells
                    } else {
                        // Apply specific text color to list cell content
                        setTextFill(Color.LIGHTGRAY);
                        setStyle("-fx-background-color: transparent;"); // Transparent background to show ListView's background

                        if (isSelected()) {
                            setTextFill(Color.WHITE);
                            setStyle("-fx-background-color: #4F6C7D; -fx-text-fill: white;"); // Highlight selection
                        } else {
                            setTextFill(Color.LIGHTGRAY);
                            setStyle("-fx-background-color: transparent;");
                        }
                    }
                }
            };
            cell.setOnMouseEntered(e -> {
                if (!cell.isEmpty() && !cell.isSelected()) {
                    cell.setStyle("-fx-background-color: #3A4D59;"); // Subtle highlight on hover
                }
            });
            cell.setOnMouseExited(e -> {
                if (!cell.isEmpty() && !cell.isSelected()) {
                    cell.setStyle("-fx-background-color: transparent;");
                }
            });
            return cell;
        });


        // TableView Styling
        songsTableView.setBackground(new Background(new BackgroundFill(Color.web("#2E3D4F"), new CornerRadii(5), Insets.EMPTY)));
        songsTableView.setStyle("-fx-border-color: #4F6C7D; -fx-border-width: 1; -fx-control-inner-background: #2E3D4F;");

        // TableColumn Header Styling
        titleColumn.setStyle("-fx-background-color: #3A4D59; -fx-text-fill: white; -fx-font-weight: bold;");
        artistColumn.setStyle("-fx-background-color: #3A4D59; -fx-text-fill: white; -fx-font-weight: bold;");

        // === CORRECTED: Apply setCellFactory to each TableColumn, not TableView ===
        titleColumn.setCellFactory(col -> new TableCell<Song, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setTextFill(Color.LIGHTGRAY); // Text color for non-selected cells
                }
            }
        });

        artistColumn.setCellFactory(col -> new TableCell<Song, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setTextFill(Color.LIGHTGRAY); // Text color for non-selected cells
                }
            }
        });

        // TableCell Row Styling (for row background and hover)
        songsTableView.setRowFactory(tv -> {
            TableRow<Song> row = new TableRow<>();
            row.setStyle("-fx-background-color: transparent;"); // Default to transparent to show table background
            row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected) {
                    row.setStyle("-fx-background-color: #4F6C7D;"); // Selected row background
                } else {
                    row.setStyle("-fx-background-color: transparent;"); // Reset to transparent
                }
            });
            row.setOnMouseEntered(e -> {
                if (!row.isEmpty() && !row.isSelected()) {
                    row.setStyle("-fx-background-color: #3A4D59;"); // Subtle highlight on hover
                }
            });
            row.setOnMouseExited(e -> {
                if (!row.isEmpty() && !row.isSelected()) {
                    row.setStyle("-fx-background-color: transparent;"); // Reset on exit
                }
            });
            return row;
        });


        // Button Styling (reusing the helper method, adjusted for this context)
        stylePlaylistManagerButton(backButton);
        stylePlaylistManagerButton(createButton);
        stylePlaylistManagerButton(deleteButton);
        stylePlaylistManagerButton(addSongButton);
        stylePlaylistManagerButton(removeSongButton);
        stylePlaylistManagerButton(playPlaylistButton);
        stylePlaylistManagerButton(playSongButton);

        // HBox/VBox Backgrounds (make them transparent to show main borderPane gradient)
        // If these HBoxes/VBoxes have their own specific background color set in FXML,
        // this will override it to transparent.
        playlistNameVBox.setBackground(Background.EMPTY);
        playlistRelatedButtonHBox.setBackground(Background.EMPTY);
        addRemoveHBox.setBackground(Background.EMPTY);
        playSongOrPlayliistHbox.setBackground(Background.EMPTY);
        backButtonHBox.setBackground(Background.EMPTY);
    }

    // Helper method to style buttons for the playlist manager
    private void stylePlaylistManagerButton(Button btn) {
        btn.setFont(Font.font("System", FontWeight.BOLD, 12)); // Slightly smaller font for more buttons
        btn.setTextFill(Color.WHITE);

        Color buttonBaseColor = Color.web("#3A4D59"); // Consistent with player buttons
        Color buttonHoverColor = Color.web("#4F6C7D"); // Consistent with player buttons

        btn.setBackground(new Background(new BackgroundFill(buttonBaseColor, new CornerRadii(5), Insets.EMPTY)));
        btn.setPadding(new Insets(7, 12, 7, 12)); // Slightly smaller padding
        btn.setEffect(new DropShadow(2, Color.BLACK)); // Subtle shadow

        btn.setOnMouseEntered(e ->
            btn.setBackground(new Background(new BackgroundFill(buttonHoverColor, new CornerRadii(5), Insets.EMPTY)))
        );
        btn.setOnMouseExited(e ->
            btn.setBackground(new Background(new BackgroundFill(buttonBaseColor, new CornerRadii(5), Insets.EMPTY)))
        );
    }
}