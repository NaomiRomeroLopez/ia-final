package Comportamientos;

import Comunicacion.Mensajes;
import Datos.ClasificadorCanciones;
import Datos.RepositorioCanciones;
import Ontologia.Cancion;
import Ontologia.PerfilMusical;
import Ontologia.Playlist;
import Ontologia.ResultadoPlaylist;
import Ontologia.SolicitudCatalogo;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.List;

public class CCatalogo extends CyclicBehaviour {

    private final RepositorioCanciones repo;

    private final MessageTemplate plantilla =
            Mensajes.de(Mensajes.GESTOR, ACLMessage.REQUEST);

    public CCatalogo(Agent a, RepositorioCanciones repo) {
        super(a);
        this.repo = repo;
    }

    @Override
    public void action() {

        ACLMessage msg = myAgent.receive(plantilla);

        if (msg == null) {
            block();
            return;
        }

        // 1. Decodificar el objeto PerfilMusical.
        PerfilMusical perfil;
        try {
            Action accion = (Action) myAgent.getContentManager().extractContent(msg);
            perfil = ((SolicitudCatalogo) accion.getAction()).getPerfil();
        } catch (Exception e) {
            responder(msg, ACLMessage.FAILURE,
                    "No se pudo interpretar la solicitud: " + e.getMessage());
            return;
        }

        // 2. Validar.
        String problema = perfil.validar();
        if (problema != null) {
            responder(msg, ACLMessage.REFUSE, problema);
            return;
        }

        if (repo.getCanciones().isEmpty()) {
            responder(msg, ACLMessage.FAILURE,
                    "Catálogo no disponible: " + repo.getError());
            return;
        }

        // 3. Aceptar y trabajar.
        responder(msg, ACLMessage.AGREE, "Solicitud aceptada; clasificando canciones...");

        List<Cancion> candidatas =
                ClasificadorCanciones.seleccionar(perfil, repo.getCanciones());

        if (candidatas.isEmpty()) {
            responder(msg, ACLMessage.REFUSE,
                    "Ninguna canción alcanzó la puntuación mínima ("
                            + ClasificadorCanciones.UMBRAL_MINIMO + "/100).");
            return;
        }

        // 4. Enviar las candidatas como objetos.
        try {
            ACLMessage inform = Mensajes.respuesta(msg, ACLMessage.INFORM);
            myAgent.getContentManager().fillContent(inform,
                    new ResultadoPlaylist(new Playlist("Candidatas", perfil, candidatas)));
            myAgent.send(inform);
            System.out.println("[Catálogo] Candidatas enviadas: " + candidatas.size());
        } catch (Exception e) {
            responder(msg, ACLMessage.FAILURE,
                    "Error al codificar las candidatas: " + e.getMessage());
        }
    }

    private void responder(ACLMessage original, int performativa, String texto) {
        ACLMessage r = Mensajes.respuesta(original, performativa);
        r.setContent(texto);
        myAgent.send(r);
    }
}