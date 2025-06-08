import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class PlaylistManager {

    public static final String LIKED_PLAYLIST_NAME = "LikedSongs";
    public static final File PLAYLISTS_ROOT_DIR = new File("Playlists");

    private Map<String, Set<File>> playlists = new HashMap<>();


    public PlaylistManager(){
        initializePlaylistsDirectory();
        loadPlaylistsFromDisk();

    }



    private void initializePlaylistsDirectory(){
        if(!PLAYLISTS_ROOT_DIR.exists()){
            if(!PLAYLISTS_ROOT_DIR.mkdirs()){
                AlertUtil.showAlert("Error", "Failed to create root playlists directory: " + PLAYLISTS_ROOT_DIR.getAbsolutePath());
            }
        }
    }


    private void loadPlaylistsFromDisk(){

        playlists.clear();

        playlists.put(LIKED_PLAYLIST_NAME, new HashSet<>());

        File[] playlistDirs = PLAYLISTS_ROOT_DIR.listFiles(File::isDirectory);

        if(playlistDirs != null){

            for(File playlistDir : playlistDirs){

                String playlistName = playlistDir.getName();
                Set<File> songsInPlaylist = new HashSet<>();

                File[] songFiles = playlistDir.listFiles();

                if(songFiles != null){

                    for(File songFile : songFiles){

                        if(songFile.isFile() && isAudioFile(songFile)){
                            songsInPlaylist.add(songFile);
                        }
                    }
                }

                playlists.put(playlistName, songsInPlaylist);

            }
        }
    }


    private boolean isAudioFile(File file){
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".aac") || name.endsWith(".m4a");
    }





    public boolean likeSong(File song){
        return addToCustomPlaylist(LIKED_PLAYLIST_NAME, song);
    }

    public boolean unlikeSong(File song){
        return removeFromCustomPlaylist(LIKED_PLAYLIST_NAME, song);
    }

    public Set<String> getCustomPlaylistNames(){
        Set<String> names = new HashSet<>(playlists.keySet());
        names.remove(LIKED_PLAYLIST_NAME);
        return names;
    }



    public Set<String> getAllPlaylistNames(){
        return new HashSet<>(playlists.keySet());
    }

    public Set<File> getSongsInPlaylist(String playlistName){
        return playlists.getOrDefault(playlistName, Collections.emptySet());
    }


    public boolean createCustomPlaylists(String name){
        if(playlists.containsKey(name) || name.equalsIgnoreCase(LIKED_PLAYLIST_NAME)){
            return false;
        }

        File newPlaylistDir = new File(PLAYLISTS_ROOT_DIR, name);

        if (!newPlaylistDir.mkdir()) {
            AlertUtil.showAlert("Error", "Failed to create directory for playlist: " + name);
            return false;
        }


        playlists.put(name, new HashSet<>());
        return true;
    }


    public boolean deletePlaylist(String playlistName){

        if(!playlists.containsKey(playlistName) || playlistName.equalsIgnoreCase(LIKED_PLAYLIST_NAME)){
            return false;
        }

        File playlistDir = new File(PLAYLISTS_ROOT_DIR, playlistName);

        try {
            // Recursively delete directory and its contents
            Files.walk(playlistDir.toPath())
                 .sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(File::delete);
        } catch (IOException e) {
            AlertUtil.showAlert("Error", "Failed to delete playlist directory: " + playlistName + " - " + e.getMessage());
            return false;
        }

        playlists.remove(playlistName);
        return true;
    }


    public boolean addToCustomPlaylist(String playlistName, File sourceSongFile){

        if(!playlists.containsKey(playlistName)){
            AlertUtil.showAlert("Error", "Playlist does not exist: " + playlistName);
            return false;
        }

        File playlistDir = new File(PLAYLISTS_ROOT_DIR, playlistName);
        File destSongFile = new File(playlistDir, sourceSongFile.getName());

        if(playlists.get(playlistName).stream().anyMatch(f -> f.getName().equals(destSongFile.getName()))){
            return false;
        }

        try {
            Files.copy(sourceSongFile.toPath(), destSongFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            playlists.get(playlistName).add(destSongFile); // Add the *copied* file path
            return true;
        } catch (IOException e) {
            AlertUtil.showAlert("Error", "Failed to copy song to playlist: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    public boolean removeFromCustomPlaylist(String playlistName, File songToRemove){

        if(!playlists.containsKey(playlistName)){
            return false;
        }

        Optional<File> storedFileOpt = playlists.get(playlistName).stream().filter(f -> f.getName().equals(songToRemove.getName())).findFirst();

        if(storedFileOpt.isPresent()){

            File fileToDelete = storedFileOpt.get();

            if(fileToDelete.delete()){
                playlists.get(playlistName).remove(fileToDelete);
                return true;
            }
            else{
                AlertUtil.showAlert("Error", "Failed to delete song from disk: " + fileToDelete.getAbsolutePath());
                return false;
            }
        }

        return false;
    }

    public boolean isSongInPlaylist(String playlistName, File song){
        return playlists.getOrDefault(playlistName, Collections.emptySet()).contains(song);
    }
}