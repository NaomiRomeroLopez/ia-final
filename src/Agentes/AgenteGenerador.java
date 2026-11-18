package Agentes;

import Comportamientos.CGenerador;
import Comunicacion.Mensajes;
import jade.core.Agent;

public class AgenteGenerador extends Agent {

    @Override
    protected void setup() {
        Mensajes.registrar(this);
        System.out.println("Agente Generador iniciado...");
        addBehaviour(new CGenerador());
    }
}