package Ontologia;

import jade.content.AgentAction;

public class SolicitudCatalogo implements AgentAction {

    private PerfilMusical perfil;

    public SolicitudCatalogo() {
    }

    public SolicitudCatalogo(PerfilMusical perfil) {
        this.perfil = perfil;
    }

    public PerfilMusical getPerfil() { return perfil; }
    public void setPerfil(PerfilMusical perfil) { this.perfil = perfil; }
}