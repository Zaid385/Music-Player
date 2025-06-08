# 🎵 JavaFX Music Player

A sleek, functional, and modern Music Player built using JavaFX. This desktop application allows you to play, pause, skip, and manage your music library with ease. Features include playlist support, volume and time control, and a responsive UI designed with FXML and SceneBuilder.

---

## 🚀 Features

- 🎧 Play/Pause/Skip music tracks
- ⏱ Track time display and seek control
- 🔊 Volume control with mute/unmute
- 📁 Load songs from local directories
- ❤️ "Liked Songs" playlist by default
- 📜 Create and manage custom playlists
- 🚫 Prevent duplicate songs in playlists
- 🎨 Clean UI built with JavaFX + FXML


## 🛠 Tech Stack

- **Java 23**
- **JavaFX 24**
- **FXML + SceneBuilder**
- **JavaFX MediaPlayer**

---

## Quick note

If the program doesn't work on Windows, try this:

Change line 11 in the PlaylistManager class.
Copy the Absolute path of the "Playlists" folder and paste it inside:

**public static final File PLAYLISTS_ROOT_DIR = new File("Paste/the/Absolute/path/here");**

There are several bugs here and there. If you encounter one, simply restart the program (usually when registering or logging in for the first time).

---

This was a semester project for my OOP class
