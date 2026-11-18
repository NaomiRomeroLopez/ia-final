package Ontologia;

import jade.content.lang.sl.SLCodec;
import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.ConceptSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PredicateSchema;
import jade.content.schema.PrimitiveSchema;

public class MusicaOntology extends Ontology {

    public static final String ONTOLOGY_NAME = "Ontologia-Musical";

    // Conceptos
    public static final String PERFIL = "PerfilMusical";
    public static final String CANCION = "Cancion";
    public static final String PLAYLIST = "Playlist";

    // Mensajes (predicados y acciones)
    public static final String PERFIL_CAPTURADO = "PerfilCapturado";
    public static final String SOLICITUD_CATALOGO = "SolicitudCatalogo";
    public static final String SOLICITUD_GENERAR = "SolicitudGenerar";
    public static final String RESULTADO_PLAYLIST = "ResultadoPlaylist";

    // Atributos del perfil
    public static final String GENERO = "genero";
    public static final String IDIOMA = "idioma";
    public static final String POPULARIDAD = "popularidad";
    public static final String DURACION = "duracion";
    public static final String EMOCION = "emocion";

    // Atributos de canción
    public static final String ID = "id";
    public static final String TITULO = "titulo";
    public static final String ARTISTA = "artista";
    public static final String ESCUCHAS = "escuchas";
    public static final String PUNTUACION = "puntuacion";

    // Atributos de playlist
    public static final String NOMBRE = "nombre";
    public static final String PERFIL_PLAYLIST = "perfil";
    public static final String CANCIONES = "canciones";

    // Atributos de los mensajes
    public static final String SLOT_PERFIL = "perfil";
    public static final String SLOT_CANDIDATAS = "candidatas";
    public static final String SLOT_PLAYLIST = "playlist";

    private static final MusicaOntology INSTANCE = new MusicaOntology();

    public static MusicaOntology getInstance() {
        return INSTANCE;
    }

    private MusicaOntology() {

        super(ONTOLOGY_NAME, BasicOntology.getInstance());

        try {

            // --------------------------------
            // PERFIL MUSICAL
            // --------------------------------
            ConceptSchema perfilSchema = new ConceptSchema(PERFIL);
            add(perfilSchema, PerfilMusical.class);

            perfilSchema.add(GENERO, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            perfilSchema.add(IDIOMA, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            perfilSchema.add(POPULARIDAD, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            perfilSchema.add(DURACION, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            perfilSchema.add(EMOCION, (PrimitiveSchema) getSchema(BasicOntology.STRING));

            // --------------------------------
            // CANCIÓN
            // --------------------------------
            ConceptSchema cancionSchema = new ConceptSchema(CANCION);
            add(cancionSchema, Cancion.class);

            cancionSchema.add(ID, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cancionSchema.add(TITULO, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cancionSchema.add(ARTISTA, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cancionSchema.add(GENERO, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cancionSchema.add(IDIOMA, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cancionSchema.add(ESCUCHAS, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cancionSchema.add(DURACION, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cancionSchema.add(EMOCION, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cancionSchema.add(PUNTUACION, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));

            // --------------------------------
            // PLAYLIST (lista anidada de Cancion)
            // --------------------------------
            ConceptSchema playlistSchema = new ConceptSchema(PLAYLIST);
            add(playlistSchema, Playlist.class);

            playlistSchema.add(NOMBRE, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            playlistSchema.add(PERFIL_PLAYLIST, perfilSchema);
            playlistSchema.add(CANCIONES, cancionSchema, 0, ObjectSchema.UNLIMITED);

            // --------------------------------
            // MENSAJES (
            // --------------------------------
            PredicateSchema perfilCapturado = new PredicateSchema(PERFIL_CAPTURADO);
            add(perfilCapturado, PerfilCapturado.class);
            perfilCapturado.add(SLOT_PERFIL, perfilSchema);

            AgentActionSchema solCatalogo = new AgentActionSchema(SOLICITUD_CATALOGO);
            add(solCatalogo, SolicitudCatalogo.class);
            solCatalogo.add(SLOT_PERFIL, perfilSchema);

            AgentActionSchema solGenerar = new AgentActionSchema(SOLICITUD_GENERAR);
            add(solGenerar, SolicitudGenerar.class);
            solGenerar.add(SLOT_CANDIDATAS, playlistSchema);

            PredicateSchema resultado = new PredicateSchema(RESULTADO_PLAYLIST);
            add(resultado, ResultadoPlaylist.class);
            resultado.add(SLOT_PLAYLIST, playlistSchema);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SLCodec getCodec() {
        return new SLCodec();
    }
}