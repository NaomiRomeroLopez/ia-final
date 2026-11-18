package Interfaz;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

public class ReproductorAudio {

    private static final double MS_POR_FRAME = 26.0;   // un frame MP3 dura ~26 ms

    private AdvancedPlayer player;
    private Path archivo;
    private int generacion = 0;        // invalida eventos de reproductores ya detenidos
    private int frameBase = 0;
    private long inicioMs = 0;
    private int frameReanudar = 0;
    private boolean reproduciendo = false;
    private boolean pausado = false;

    private Runnable alTerminar = () -> { };
    private Consumer<String> alError = msg -> { };

    public void setAlTerminar(Runnable r) { this.alTerminar = r; }
    public void setAlError(Consumer<String> c) { this.alError = c; }

    public synchronized boolean estaReproduciendo() { return reproduciendo; }
    public synchronized boolean estaPausado() { return pausado; }

    public synchronized void reproducir(Path nuevo) {
        detener();
        archivo = nuevo;
        iniciar(0);
    }

    public synchronized void pausar() {
        if (!reproduciendo) return;
        frameReanudar = frameBase
                + (int) ((System.currentTimeMillis() - inicioMs) / MS_POR_FRAME);
        apagar();
        pausado = true;
    }

    public synchronized void reanudar() {
        if (pausado && archivo != null) {
            pausado = false;
            iniciar(frameReanudar);
        }
    }

    public synchronized void detener() {
        apagar();
        pausado = false;
    }

    // ---------- Internos ----------

    private void apagar() {
        generacion++;                 // el listener del reproductor viejo se ignorará
        reproduciendo = false;
        if (player != null) {
            player.stop();
            player = null;
        }
    }

    private void iniciar(int frameInicio) {
        final int gen = ++generacion;
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(archivo.toFile()));
            final AdvancedPlayer p = new AdvancedPlayer(in);

            p.setPlayBackListener(new PlaybackListener() {
                @Override
                public void playbackFinished(PlaybackEvent evt) {
                    boolean terminoSolo;
                    synchronized (ReproductorAudio.this) {
                        terminoSolo = (gen == generacion);
                        if (terminoSolo) {
                            reproduciendo = false;
                            player = null;
                        }
                    }
                    if (terminoSolo) {
                        alTerminar.run();     // la canción acabó por sí sola
                    }
                }
            });

            player = p;
            frameBase = frameInicio;
            inicioMs = System.currentTimeMillis();
            reproduciendo = true;

            Thread hilo = new Thread(() -> {
                try {
                    p.play(frameInicio, Integer.MAX_VALUE);
                } catch (Exception e) {
                    synchronized (ReproductorAudio.this) {
                        if (gen == generacion) reproduciendo = false;
                    }
                    alError.accept("Error al reproducir: " + e.getMessage());
                }
            }, "reproductor-audio");
            hilo.setDaemon(true);
            hilo.start();

        } catch (Exception e) {
            reproduciendo = false;
            alError.accept("No se pudo abrir el audio: " + e.getMessage());
        }
    }
}