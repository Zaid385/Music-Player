import java.io.File;

public class Song {

    private final File file;
    private final String title;
    private final String artist;

    public Song(File file, String title, String artist) {
        this.file = file;
        this.title = title;
        this.artist = artist;
    }

    public File getFile() {
        return file;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    @Override
    public String toString() {
        return title + " - " + artist;
    }

}
