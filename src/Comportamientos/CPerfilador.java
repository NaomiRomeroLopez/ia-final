package Comportamientos;

import Comunicacion.Mensajes;
import Interfaz.FormularioPerfil;
import Ontologia.PerfilCapturado;
import Ontologia.PerfilMusical;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import javax.swing.SwingUtilities;

// Abre el formulario al iniciar y cada vez que el Gestor pide una nueva consulta.
public class CPerfilador extends CyclicBehaviour {

    // Solo escucha REQUEST del Gestor con nuestra ontología.
    private final MessageTemplate plantilla =
            Mensajes.de(Mensajes.GESTOR, ACLMessage.REQUEST);

    private FormularioPerfil formulario;

    public CPerfilador(Agent a) {
        super(a);
    }

    @Override
    public void onStart() {
        abrirFormulario();   // una sola vez, antes del primer action()
    }

    @Override
    public void action() {
        ACLMessage pedido = myAgent.receive(plantilla);

        if (pedido == null) {
            block();
            return;
        }

        abrirFormulario();   // "nueva-consulta"
    }

    private void abrirFormulario() {
        SwingUtilities.invokeLater(() -> {
            // Si ya hay un formulario abierto, solo se trae al frente.
            if (formulario != null && formulario.isDisplayable()) {
                formulario.toFront();
                return;
            }
            formulario = new FormularioPerfil(this::enviarPerfil);
            formulario.setVisible(true);
        });
    }

    private void enviarPerfil(PerfilMusical perfil) {

        String problema = perfil.validar();
        if (problema != null) {
            System.out.println("[Perfilador] Perfil rechazado: " + problema);
            abrirFormulario();
            return;
        }

        try {
            ACLMessage msg = Mensajes.nuevo(ACLMessage.INFORM, Mensajes.GESTOR);
            myAgent.getContentManager().fillContent(msg, new PerfilCapturado(perfil));
            myAgent.send(msg);
            System.out.println("[Perfilador] Perfil enviado al Gestor: " + perfil);
        } catch (Exception e) {
            System.out.println("[Perfilador] No se pudo codificar el perfil: " + e.getMessage());
        }
    }
}