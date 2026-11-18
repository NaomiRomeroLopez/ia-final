package Ontologia;

import jade.content.Predicate;

public class PerfilCapturado implements Predicate {

    private PerfilMusical perfil;

    public PerfilCapturado() {
    }

    public PerfilCapturado(PerfilMusical perfil) {
        this.perfil = perfil;
    }

    public PerfilMusical getPerfil() { return perfil; }
    public void setPerfil(PerfilMusical perfil) { this.perfil = perfil; }
}