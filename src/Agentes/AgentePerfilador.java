package Agentes;

import Comportamientos.CPerfilador;
import Comunicacion.Mensajes;
import jade.core.Agent;

public class AgentePerfilador extends Agent {
    @Override
    protected void setup() {
        Mensajes.registrar(this);
        addBehaviour(new CPerfilador(this));
    }
}