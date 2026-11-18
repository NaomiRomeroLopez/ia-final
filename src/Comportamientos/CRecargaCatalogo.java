package Comportamientos;

import Datos.RepositorioCanciones;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

// Revisa periódicamente si canciones.tsv cambió en disco y refresca el catálogo.
public class CRecargaCatalogo extends TickerBehaviour {

    private final RepositorioCanciones repo;

    public CRecargaCatalogo(Agent a, long periodoMs, RepositorioCanciones repo) {
        super(a, periodoMs);
        this.repo = repo;
    }

    @Override
    protected void onTick() {
        if (repo.recargarSiCambio()) {
            System.out.println("[Catálogo] canciones.tsv cambió; catálogo recargado ("
                    + repo.getCanciones().size() + " canciones).");
        }
    }
}