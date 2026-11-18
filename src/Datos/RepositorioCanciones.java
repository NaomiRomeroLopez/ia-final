package Datos;

import Ontologia.Cancion;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RepositorioCanciones {

    private final Path ruta;
    private long ultimaModificacion = Long.MIN_VALUE;
    private List<Cancion> canciones = Collections.emptyList();
    private String error;

    public RepositorioCanciones(String ruta) {
        this.ruta = Paths.get(ruta);
    }

    /** @return true si el archivo cambió y se recargó. */
    public synchronized boolean recargarSiCambio() {
        try {
            long modificado = Files.getLastModifiedTime(ruta).toMillis();
            if (modificado == ultimaModificacion) {
                return false;
            }
            canciones = leer();
            ultimaModificacion = modificado;
            error = null;
            return true;
        } catch (Exception e) {
            error = "No se pudo leer " + ruta + ": " + e.getMessage();
            return false;   // se conserva el catálogo anterior
        }
    }

    public synchronized List<Cancion> getCanciones() {
        return canciones;
    }

    public synchronized String getError() {
        return error;
    }

    private List<Cancion> leer() throws IOException {
        List<Cancion> lista = new ArrayList<>();

        try (BufferedReader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {

            if (lector.readLine() == null) {          // fila de encabezados
                throw new IOException("El archivo está vacío.");
            }

            String linea;
            while ((linea = lector.readLine()) != null) {

                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] c = linea.split("\t", -1);   // -1 conserva campos vacíos

                if (c.length < 8) {
                    continue;                         // fila malformada
                }

                for (int i = 0; i < c.length; i++) {
                    c[i] = c[i].trim();
                }

                // id, titulo, artista, genero, idioma, escuchas, duracion, emocion
                lista.add(new Cancion(c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7]));
            }
        }

        return Collections.unmodifiableList(lista);
    }
}