package Comportamientos;

import Comunicacion.Mensajes;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CGenerador extends CyclicBehaviour {

    private final MessageTemplate plantilla =
            Mensajes.de(Mensajes.GESTOR, ACLMessage.REQUEST);

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive(plantilla);

        if (msg == null) {
            block();
            return;
        }

        // Cada solicitud se delega a un comportamiento de una sola ejecución.
        myAgent.addBehaviour(new OGenerarPlaylist(myAgent, msg));
    }
}