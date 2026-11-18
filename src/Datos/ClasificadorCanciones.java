package Datos;

import Ontologia.Cancion;
import Ontologia.PerfilMusical;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClasificadorCanciones {

    // Pesos (suman 100). Un criterio "cualquiera" otorga todos sus puntos.
    public static final int P_GENERO = 30;
    public static final int P_IDIOMA = 20;
    public static final int P_POPULARIDAD = 15;
    public static final int P_DURACION = 15;
    public static final int P_EMOCION = 20;

    public static final int UMBRAL_MINIMO = 60;    // puntuación mínima para ser candidata
    public static final int MAX_CANDIDATAS = 50;
    private static final long ESCUCHAS_POPULAR = 1000;

    private ClasificadorCanciones() {
    }

    public static int puntuar(PerfilMusical p, Cancion c) {
        int pts = 0;

        if (cualquiera(p.getGenero()) || igual(p.getGenero(), c.getGenero())) {
            pts += P_GENERO;
        }
        if (cualquiera(p.getIdioma()) || igual(p.getIdioma(), c.getIdioma())) {
            pts += P_IDIOMA;
        }
        if (coincidePopularidad(p.getPopularidad(), c.getEscuchas())) {
            pts += P_POPULARIDAD;
        }
        if (coincideDuracion(p.getDuracion(), c.getDuracion())) {
            pts += P_DURACION;
        }
        if (cualquiera(p.getEmocion()) || igual(p.getEmocion(), c.getEmocion())) {
            pts += P_EMOCION;
        }
        return pts;
    }

    /** Devuelve copias de las mejores canciones, con su puntuación asignada. */
    public static List<Cancion> seleccionar(PerfilMusical perfil, List<Cancion> catalogo) {
        List<Cancion> aptas = new ArrayList<>();

        for (Cancion c : catalogo) {
            int pts = puntuar(perfil, c);
            if (pts >= UMBRAL_MINIMO) {
                // Copia: no se modifica el catálogo en memoria.
                Cancion copia = new Cancion(c.getId(), c.getTitulo(), c.getArtista(),
                        c.getGenero(), c.getIdioma(), c.getEscuchas(),
                        c.getDuracion(), c.getEmocion());
                copia.setPuntuacion(pts);
                aptas.add(copia);
            }
        }

        // shuffle + sort estable: los empates salen en orden variable.
        Collections.shuffle(aptas);
        aptas.sort((a, b) -> Integer.compare(b.getPuntuacion(), a.getPuntuacion()));

        if (aptas.size() > MAX_CANDIDATAS) {
            return new ArrayList<>(aptas.subList(0, MAX_CANDIDATAS));
        }
        return aptas;
    }

    private static boolean coincidePopularidad(String buscada, String escuchasTxt) {
        if (cualquiera(buscada)) {
            return true;
        }
        try {
            long escuchas = Long.parseLong(escuchasTxt.trim());
            if (escuchas < 0) {
                return false;
            }
            if (buscada.equalsIgnoreCase("popular")) {
                return escuchas >= ESCUCHAS_POPULAR;
            }
            return escuchas < ESCUCHAS_POPULAR;   // underground
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean coincideDuracion(String buscada, String duracionTxt) {
        if (cualquiera(buscada)) {
            return true;
        }
        try {
            double s = Double.parseDouble(duracionTxt.trim());
            if (!Double.isFinite(s) || s <= 0) {
                return false;
            }
            switch (buscada.trim().toLowerCase()) {
                case "cortas":   return s < 180;
                case "promedio": return s >= 180 && s <= 300;
                case "largas":   return s > 300;
                default:         return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean cualquiera(String v) {
        return v == null || v.trim().isEmpty() || v.equalsIgnoreCase("cualquiera");
    }

    private static boolean igual(String a, String b) {
        return a != null && b != null && a.trim().equalsIgnoreCase(b.trim());
    }
}