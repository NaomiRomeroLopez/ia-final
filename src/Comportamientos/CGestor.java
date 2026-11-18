package Comportamientos;

import Comunicacion.Mensajes;
import Interfaz.Reproductor;
import Ontologia.Cancion;
import Ontologia.PerfilCapturado;
import Ontologia.PerfilMusical;
import Ontologia.Playlist;
import Ontologia.ResultadoPlaylist;
import Ontologia.SolicitudCatalogo;
import Ontologia.SolicitudGenerar;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

// Máquina de estados: PERFIL -> CATALOGO -> GENERADOR -> (vuelve a PERFIL).
public class CGestor extends SimpleBehaviour {

    private enum Estado { ESPERANDO_PERFIL, ESPERANDO_CATALOGO, ESPERANDO_GENERADOR }

    private static final long TIMEOUT_MS = 30_000;

    private final Agent agente;
    private final Reproductor ventana;

    private Estado estado = Estado.ESPERANDO_PERFIL;
    private String idConversacion;
    private long limite;

    public CGestor(Agent agente, Reproductor ventana) {
        super(agente);
        this.agente = agente;
        this.ventana = ventana;

        // El botón "Nueva consulta" le pide al Perfilador que abra otra vez el formulario.
        this.ventana.setAlNuevaConsulta(() -> {
            ACLMessage pedido = Mensajes.nuevo(ACLMessage.REQUEST, Mensajes.PERFILADOR);
            pedido.setContent("nueva-consulta");
            this.agente.send(pedido);
        });
    }

    @Override
    public void action() {

        ACLMessage msg = myAgent.receive(plantillaActual());

        if (msg == null) {
            if (estado == Estado.ESPERANDO_PERFIL) {
                block();
            } else {
                long restante = limite - System.currentTimeMillis();
                if (restante <= 0) {
                    fallar("El agente no respondió en " + (TIMEOUT_MS / 1000) + " segundos.");
                } else {
                    block(restante);
                }
            }
            return;
        }

        try {
            switch (estado) {
                case ESPERANDO_PERFIL:
                    recibirPerfil(msg);
                    break;
                case ESPERANDO_CATALOGO:
                    recibirDelCatalogo(msg);
                    break;
                case ESPERANDO_GENERADOR:
                    recibirDelGenerador(msg);
                    break;
            }
        } catch (Exception e) {
            fallar("Error interno del Gestor: " + e.getMessage());
        }
    }

    // ---------- Plantillas por estado ----------

    private MessageTemplate plantillaActual() {
        switch (estado) {
            case ESPERANDO_PERFIL:
                return Mensajes.de(Mensajes.PERFILADOR, ACLMessage.INFORM);
            case ESPERANDO_CATALOGO:
                return respuestasDe(Mensajes.CATALOGO);
            default:
                return respuestasDe(Mensajes.GENERADOR);
        }
    }

    // emisor AND ontología AND conversation-id AND (AGREE OR INFORM OR REFUSE OR FAILURE)
    private MessageTemplate respuestasDe(String emisor) {
        MessageTemplate performativas = MessageTemplate.or(
                MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.AGREE),
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM)),
                MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.REFUSE),
                        MessageTemplate.MatchPerformative(ACLMessage.FAILURE)));

        return MessageTemplate.and(
                MessageTemplate.and(
                        Mensajes.de(emisor),
                        MessageTemplate.MatchConversationId(idConversacion)),
                performativas);
    }

    // ---------- Estados ----------

    private void recibirPerfil(ACLMessage msg) throws Exception {

        PerfilCapturado pc =
                (PerfilCapturado) myAgent.getContentManager().extractContent(msg);
        PerfilMusical perfil = pc.getPerfil();

        String problema = perfil.validar();
        if (problema != null) {
            ventana.mostrarError(problema);
            return;
        }

        ventana.mostrarPerfil(perfil.toString());
        idConversacion = "consulta-" + System.currentTimeMillis();

        ACLMessage req = Mensajes.nuevo(ACLMessage.REQUEST, Mensajes.CATALOGO);
        req.setConversationId(idConversacion);
        myAgent.getContentManager().fillContent(req,
                new Action(Mensajes.aid(Mensajes.CATALOGO), new SolicitudCatalogo(perfil)));
        myAgent.send(req);

        ventana.mostrarEstado("Buscando canciones candidatas...");
        ventana.setNuevaConsultaHabilitada(false);
        System.out.println("[Gestor] Perfil enviado al catálogo: " + perfil);

        iniciarEspera(Estado.ESPERANDO_CATALOGO);
    }

    private void recibirDelCatalogo(ACLMessage msg) throws Exception {

        switch (msg.getPerformative()) {

            case ACLMessage.AGREE:
                ventana.mostrarEstado("El catálogo está clasificando canciones...");
                limite = System.currentTimeMillis() + TIMEOUT_MS;
                break;

            case ACLMessage.INFORM:
                ResultadoPlaylist r =
                        (ResultadoPlaylist) myAgent.getContentManager().extractContent(msg);

                ACLMessage req = Mensajes.nuevo(ACLMessage.REQUEST, Mensajes.GENERADOR);
                req.setConversationId(idConversacion);
                myAgent.getContentManager().fillContent(req,
                        new Action(Mensajes.aid(Mensajes.GENERADOR),
                                new SolicitudGenerar(r.getPlaylist())));
                myAgent.send(req);

                ventana.mostrarEstado("Armando la playlist...");
                iniciarEspera(Estado.ESPERANDO_GENERADOR);
                break;

            default:   // REFUSE o FAILURE
                fallar(msg.getContent());
        }
    }

    private void recibirDelGenerador(ACLMessage msg) throws Exception {

        switch (msg.getPerformative()) {

            case ACLMessage.AGREE:
                limite = System.currentTimeMillis() + TIMEOUT_MS;
                break;

            case ACLMessage.INFORM:
                ResultadoPlaylist r =
                        (ResultadoPlaylist) myAgent.getContentManager().extractContent(msg);
                ventana.mostrarPlaylist(r.getPlaylist().toJavaList());
                System.out.println("[Gestor] Playlist recibida del generador.");
                reiniciar();
                break;

            default:   // REFUSE o FAILURE
                fallar(msg.getContent());
        }
    }

    // ---------- Utilidades ----------

    private String formatear(Playlist p) {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (Cancion c : p.toJavaList()) {
            sb.append(i++).append(". ").append(c).append('\n');
        }
        return sb.toString();
    }

    private void iniciarEspera(Estado siguiente) {
        estado = siguiente;
        limite = System.currentTimeMillis() + TIMEOUT_MS;
    }

    private void fallar(String motivo) {
        ventana.mostrarError(motivo);
        reiniciar();
    }

    private void reiniciar() {
        estado = Estado.ESPERANDO_PERFIL;
        idConversacion = null;
        ventana.setNuevaConsultaHabilitada(true);
    }

    @Override
    public boolean done() {
        return false;   // el Gestor atiende consultas indefinidamente
    }
}