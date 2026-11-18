package Comunicacion;

import Ontologia.MusicaOntology;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public final class Mensajes {

    public static final String GESTOR = "AgenteGestor";
    public static final String PERFILADOR = "AgentePerfilador";
    public static final String CATALOGO = "AgenteCatalogo";
    public static final String GENERADOR = "AgenteGenerador";

    private Mensajes() {
    }

    /** Llamar en el setup() de cada agente. */
    public static void registrar(Agent a) {
        a.getContentManager().registerLanguage(new SLCodec());
        a.getContentManager().registerOntology(MusicaOntology.getInstance());
    }

    public static AID aid(String nombreLocal) {
        return new AID(nombreLocal, AID.ISLOCALNAME);
    }

    /** Plantilla: emisor AND ontología. */
    public static MessageTemplate de(String emisor) {
        return MessageTemplate.and(
                MessageTemplate.MatchSender(aid(emisor)),
                MessageTemplate.MatchOntology(MusicaOntology.ONTOLOGY_NAME));
    }

    /** Plantilla: emisor AND ontología AND performativa. */
    public static MessageTemplate de(String emisor, int performativa) {
        return MessageTemplate.and(
                de(emisor),
                MessageTemplate.MatchPerformative(performativa));
    }

    public static ACLMessage nuevo(int performativa, String receptor) {
        ACLMessage m = new ACLMessage(performativa);
        m.addReceiver(aid(receptor));
        m.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
        m.setOntology(MusicaOntology.ONTOLOGY_NAME);
        return m;
    }

    public static ACLMessage respuesta(ACLMessage original, int performativa) {
        ACLMessage r = original.createReply();   // conserva el conversation-id
        r.setPerformative(performativa);
        r.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
        r.setOntology(MusicaOntology.ONTOLOGY_NAME);
        return r;
    }
}