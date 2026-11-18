package Ontologia;

import jade.content.Concept;

public class PerfilMusical implements Concept {

    private String genero;
    private String idioma;
    private String popularidad;
    private String duracion;
    private String emocion;

    public PerfilMusical() {
    }

    public PerfilMusical(String genero, String idioma,
                         String popularidad, String duracion,
                         String emocion) {
        this.genero = genero;
        this.idioma = idioma;
        this.popularidad = popularidad;
        this.duracion = duracion;
        this.emocion = emocion;
    }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }

    public String getPopularidad() { return popularidad; }
    public void setPopularidad(String popularidad) { this.popularidad = popularidad; }

    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }

    public String getEmocion() { return emocion; }
    public void setEmocion(String emocion) { this.emocion = emocion; }

    /** @return null si el perfil es válido; si no, el motivo del error. */
    public String validar() {
        if (vacio(genero)) return "Falta el género.";
        if (vacio(idioma)) return "Falta el idioma.";
        if (!en(popularidad, "popular", "underground", "cualquiera"))
            return "Popularidad inválida: " + popularidad;
        if (!en(duracion, "cortas", "promedio", "largas", "cualquiera"))
            return "Duración inválida: " + duracion;
        if (!en(emocion, "feliz", "tranquilo", "triste", "intenso", "cualquiera"))
            return "Emoción inválida: " + emocion;
        return null;
    }

    private static boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static boolean en(String valor, String... opciones) {
        if (valor == null) return false;
        for (String o : opciones) {
            if (o.equalsIgnoreCase(valor.trim())) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return genero + ", " + idioma + ", " + popularidad
                + ", " + duracion + ", " + emocion;
    }
}