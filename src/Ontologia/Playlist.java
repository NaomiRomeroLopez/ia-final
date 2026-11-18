package Ontologia;

import jade.content.Concept;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

public class Playlist implements Concept {

    private String nombre;
    private PerfilMusical perfil;
    private List canciones = new ArrayList();   // jade.util.leap.List (lo que JADE sabe codificar)

    public Playlist() {
    }

    /** Constructor de conveniencia: recibe una lista normal de Java. */
    public Playlist(String nombre, PerfilMusical perfil, java.util.List<Cancion> lista) {
        this.nombre = nombre;
        this.perfil = perfil;
        for (Cancion c : lista) {
            canciones.add(c);
        }
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public PerfilMusical getPerfil() { return perfil; }
    public void setPerfil(PerfilMusical perfil) { this.perfil = perfil; }

    // Getter y setter que usa JADE (slot "canciones").
    public List getCanciones() { return canciones; }
    public void setCanciones(List canciones) { this.canciones = canciones; }

    /** Para uso interno de los agentes: copia a una lista normal de Java. */
    public java.util.List<Cancion> toJavaList() {
        java.util.List<Cancion> resultado = new java.util.ArrayList<>();
        for (int i = 0; i < canciones.size(); i++) {
            resultado.add((Cancion) canciones.get(i));
        }
        return resultado;
    }
}