package Ontologia;

import jade.content.AgentAction;

public class SolicitudGenerar implements AgentAction {

    private Playlist candidatas;

    public SolicitudGenerar() {
    }

    public SolicitudGenerar(Playlist candidatas) {
        this.candidatas = candidatas;
    }

    public Playlist getCandidatas() { return candidatas; }
    public void setCandidatas(Playlist candidatas) { this.candidatas = candidatas; }
}