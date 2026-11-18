package Agentes;

import Comportamientos.CCatalogo;
import Comportamientos.CRecargaCatalogo;
import Comunicacion.Mensajes;
import Datos.RepositorioCanciones;
import jade.core.Agent;

public class AgenteCatalogo extends Agent {

    @Override
    protected void setup() {
        Mensajes.registrar(this);

        RepositorioCanciones repo = new RepositorioCanciones("src/data/canciones.tsv");
        repo.recargarSiCambio();   // carga inicial

        System.out.println("Agente Catálogo iniciado: " + repo.getCanciones().size()
                + " canciones" + (repo.getError() != null ? " (" + repo.getError() + ")" : ""));

        addBehaviour(new CCatalogo(this, repo));                // atiende solicitudes
        addBehaviour(new CRecargaCatalogo(this, 5000, repo));   // revisa el archivo cada 5 s
    }
}