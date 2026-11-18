package Ontologia;

import jade.content.Concept;

public class Cancion implements Concept {

    private String id;
    private String titulo;
    private String artista;
    private String genero;
    private String idioma;
    private String escuchas;
    private String duracion;
    private String emocion;
    private int puntuacion;   // 0-100, la calcula el AgenteCatalogo

    public Cancion() {
    }

    public Cancion(String id, String titulo, String artista,
                   String genero, String idioma, String escuchas,
                   String duracion, String emocion) {
        this.id = id;
        this.titulo = titulo;
        this.artista = artista;
        this.genero = genero;
        this.idioma = idioma;
        this.escuchas = escuchas;
        this.duracion = duracion;
        this.emocion = emocion;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getArtista() { return artista; }
    public void setArtista(String artista) { this.artista = artista; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }

    public String getEscuchas() { return escuchas; }
    public void setEscuchas(String escuchas) { this.escuchas = escuchas; }

    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }

    public String getEmocion() { return emocion; }
    public void setEmocion(String emocion) { this.emocion = emocion; }

    public int getPuntuacion() { return puntuacion; }
    public void setPuntuacion(int puntuacion) { this.puntuacion = puntuacion; }

    @Override
    public String toString() {
        return titulo + " - " + artista
                + " | ID: " + id
                + " | Género: " + genero
                + " | Idioma: " + idioma
                + " | Escuchas: " + escuchas
                + " | Duración: " + duracion + " s"
                + " | Emoción: " + emocion
                + " | Puntuación: " + puntuacion;
    }
}