package Ontologia;

import jade.content.Predicate;

public class ResultadoPlaylist implements Predicate {

    private Playlist playlist;

    public ResultadoPlaylist() {
    }

    public ResultadoPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public Playlist getPlaylist() { return playlist; }
    public void setPlaylist(Playlist playlist) { this.playlist = playlist; }
}