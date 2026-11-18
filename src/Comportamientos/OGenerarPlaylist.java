package Comportamientos;

import Comunicacion.Mensajes;
import Ontologia.Cancion;
import Ontologia.PerfilMusical;
import Ontologia.Playlist;
import Ontologia.ResultadoPlaylist;
import Ontologia.SolicitudGenerar;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OGenerarPlaylist extends OneShotBehaviour {

    private static final int TAM_PLAYLIST = 20;

    private final ACLMessage solicitud;

    public OGenerarPlaylist(Agent a, ACLMessage solicitud) {
        super(a);
        this.solicitud = solicitud;
    }

    @Override
    public void action() {
        try {
            Action accion = (Action) myAgent.getContentManager().extractContent(solicitud);
            Playlist candidatas = ((SolicitudGenerar) accion.getAction()).getCandidatas();

            if (candidatas == null
                    || candidatas.getCanciones() == null
                    || candidatas.getCanciones().isEmpty()) {
                responder(ACLMessage.REFUSE, "No se proporcionaron canciones candidatas.");
                return;
            }

            responder(ACLMessage.AGREE, "Solicitud aceptada. Armando la playlist...");

            Playlist playlistFinal = construir(candidatas);

            ACLMessage inform = Mensajes.respuesta(solicitud, ACLMessage.INFORM);
            myAgent.getContentManager().fillContent(inform, new ResultadoPlaylist(playlistFinal));
            myAgent.send(inform);

            System.out.println("[Generador] Playlist enviada: "
                    + playlistFinal.getCanciones().size() + " canciones.");

        } catch (Exception e) {
            e.printStackTrace();
            responder(ACLMessage.FAILURE, "Error al generar la playlist: " + e.getMessage());
        }
    }

    private Playlist construir(Playlist candidatas) {

        // 1. Evitar duplicados por id (se conserva la de mayor puntuación).
        Map<String, Cancion> unicas = new LinkedHashMap<>();
        for (Cancion c : candidatas.toJavaList()) {
            Cancion previa = unicas.get(c.getId());
            if (previa == null || c.getPuntuacion() > previa.getPuntuacion()) {
                unicas.put(c.getId(), c);
            }
        }

        // 2. Ordenar por puntuación descendente.
        List<Cancion> lista = new ArrayList<>(unicas.values());
        lista.sort((a, b) -> Integer.compare(b.getPuntuacion(), a.getPuntuacion()));

        // 3. Recortar al tamaño final.
        if (lista.size() > TAM_PLAYLIST) {
            lista = new ArrayList<>(lista.subList(0, TAM_PLAYLIST));
        }

        PerfilMusical p = candidatas.getPerfil();
        String nombre = "Playlist " + p.getGenero() + " / " + p.getEmocion();

        return new Playlist(nombre, p, lista);
    }

    private void responder(int performativa, String texto) {
        ACLMessage r = Mensajes.respuesta(solicitud, performativa);
        r.setContent(texto);
        myAgent.send(r);
    }
}