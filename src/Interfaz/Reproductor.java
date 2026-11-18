package Interfaz;

import Ontologia.Cancion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Reproductor extends JFrame {

    // Carpeta que contiene las subcarpetas 000, 001, ... del dataset FMA.
    // Relativa a la raíz del proyecto, o absoluta (ej. "C:/datos/fma_small").
    private static final String RUTA_AUDIO = "Data/fma_small";

    public static final Color COLOR_BG = new Color(0xF8D7E3);          // Rosa suave exterior
    public static final Color COLOR_CARD = new Color(0xFFF5F8);        // Crema para tarjetas
    public static final Color COLOR_SIDEBAR = new Color(0xF3C0D3);     // Rosa suave barra lateral
    public static final Color COLOR_TEXT_MAIN = new Color(0x70525C);   // Marrón café oscuro
    public static final Color COLOR_TEXT_SUB = new Color(0x9E7885);    // Marrón medio
    public static final Color COLOR_ACCENT = new Color(0xE888A7);      // Rosa acento fuerte
    public static final Color COLOR_WHITE = new Color(0xFFFFFF);

    // Usados por FormularioPerfil
    public static final Color COLOR_CONTAINER = COLOR_CARD;
    public static final Color COLOR_BUTTON_BG = COLOR_ACCENT;
    public static final Color COLOR_CARD_BG = COLOR_WHITE;

    private final DefaultListModel<Cancion> songModel = new DefaultListModel<>();
    private final JList<Cancion> songList = new JList<>(songModel);
    private final JTextArea detalle = new JTextArea();
    private final JLabel estado = new JLabel("Esperando perfil...");
    private final JLabel songName = new JLabel("Resultado del catálogo");
    private final JLabel artistName = new JLabel("Consulta por cinco preferencias");

    private JButton btnNuevo;
    private JButton btnPlayPause;

    private final ReproductorAudio audio = new ReproductorAudio();
    private int indiceActual = -1;
    private String perfilTexto = "";

    private Runnable alNuevaConsulta = () -> { };

    public Reproductor() {
        setTitle("Reproductor Musical 🎀");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        add(crearPaginaReproductor());

        // Callbacks del audio (llegan desde el hilo de audio: pasar al hilo de Swing)
        audio.setAlTerminar(() -> SwingUtilities.invokeLater(this::siguiente));
        audio.setAlError(msg -> SwingUtilities.invokeLater(() -> estado.setText(msg)));

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                audio.detener();
            }
        });
    }

    // ---------- API usada por el Gestor ----------

    public void setAlNuevaConsulta(Runnable accion) {
        this.alNuevaConsulta = accion;
    }

    public void setNuevaConsultaHabilitada(boolean habilitada) {
        SwingUtilities.invokeLater(() -> btnNuevo.setEnabled(habilitada));
    }

    public void mostrarPerfil(String perfil) {
        SwingUtilities.invokeLater(() -> {
            perfilTexto = "Perfil recibido:\n" + perfil;
            detalle.setText(perfilTexto);
        });
    }

    public void mostrarEstado(String texto) {
        SwingUtilities.invokeLater(() -> estado.setText(texto));
    }

    /** Recibe la playlist final, la muestra y empieza a reproducir la primera canción. */
    public void mostrarPlaylist(List<Cancion> canciones) {
        SwingUtilities.invokeLater(() -> {
            audio.detener();
            songModel.clear();
            for (Cancion c : canciones) {
                songModel.addElement(c);
            }
            estado.setText(songModel.size() + " canciones listas");
            indiceActual = -1;
            if (!songModel.isEmpty()) {
                reproducirIndice(0);
            }
        });
    }

    public void mostrarError(String motivo) {
        SwingUtilities.invokeLater(() -> {
            audio.detener();
            songModel.clear();
            indiceActual = -1;
            btnPlayPause.setText("▶");
            estado.setText("Sin resultados");
            JOptionPane.showMessageDialog(this, motivo, "Resultado", JOptionPane.WARNING_MESSAGE);
        });
    }

    // ---------- Control de reproducción (siempre en el hilo de Swing) ----------

    private void reproducirIndice(int i) {
        if (i < 0 || i >= songModel.size()) return;

        Cancion c = songModel.get(i);
        Path archivo = ubicarArchivo(c);

        indiceActual = i;
        songList.setSelectedIndex(i);
        songList.ensureIndexIsVisible(i);
        songName.setText(c.getTitulo());
        artistName.setText(c.getArtista());
        detalle.setText(perfilTexto + "\n\nReproduciendo:\n" + c);

        if (!Files.exists(archivo)) {
            audio.detener();
            btnPlayPause.setText("▶");
            estado.setText("No se encontró el audio");
            JOptionPane.showMessageDialog(this,
                    "No se encontró el archivo de audio:\n" + archivo.toAbsolutePath()
                            + "\n\nRevisa la constante RUTA_AUDIO en Reproductor.java.",
                    "Audio no encontrado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        audio.reproducir(archivo);
        btnPlayPause.setText("⏸");
        estado.setText("Reproduciendo " + (i + 1) + " de " + songModel.size());
    }

    /** FMA guarda los audios como RUTA_AUDIO/000/000002.mp3 (id con 6 dígitos). */
    private Path ubicarArchivo(Cancion c) {
        String id = c.getId().trim();
        String seis;
        try {
            seis = String.format("%06d", Integer.parseInt(id));
        } catch (NumberFormatException e) {
            seis = id;
        }
        String carpeta = seis.length() >= 3 ? seis.substring(0, 3) : seis;
        return Paths.get(RUTA_AUDIO, carpeta, seis + ".mp3");
    }

    private void alternarPausa() {
        if (songModel.isEmpty()) return;

        if (audio.estaReproduciendo()) {
            audio.pausar();
            btnPlayPause.setText("▶");
            estado.setText("Pausado");
        } else if (audio.estaPausado()) {
            audio.reanudar();
            btnPlayPause.setText("⏸");
            estado.setText("Reproduciendo " + (indiceActual + 1) + " de " + songModel.size());
        } else {
            reproducirIndice(indiceActual >= 0 ? indiceActual : 0);
        }
    }

    private void siguiente() {
        if (indiceActual + 1 < songModel.size()) {
            reproducirIndice(indiceActual + 1);
        } else {
            audio.detener();
            btnPlayPause.setText("▶");
            estado.setText("Playlist terminada");
        }
    }

    private void anterior() {
        if (songModel.isEmpty()) return;
        reproducirIndice(indiceActual > 0 ? indiceActual - 1 : 0);
    }

    // ---------- Construcción de la interfaz ----------

    private JPanel crearPaginaReproductor() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(COLOR_BG);

        PanelRedondeado sidebar = new PanelRedondeado(0, COLOR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(new EmptyBorder(25, 15, 20, 15));

        JPanel menuPanel = new JPanel(new GridLayout(5, 1, 0, 10));
        menuPanel.setOpaque(false);

        JLabel brand = new JLabel("Reproductor 🎀");
        brand.setFont(new Font("SansSerif", Font.BOLD, 20));
        brand.setForeground(COLOR_TEXT_MAIN);
        brand.setBorder(new EmptyBorder(0, 5, 15, 0));

        JButton btnPlay = crearBotonSidebar("▶️  Reproduciendo", true);
        btnNuevo = crearBotonSidebar("⟲  Nueva consulta", false);
        btnNuevo.addActionListener(e -> alNuevaConsulta.run());

        menuPanel.add(brand);
        menuPanel.add(btnPlay);
        menuPanel.add(btnNuevo);

        sidebar.add(menuPanel, BorderLayout.NORTH);

        JPanel contentArea = new JPanel(new BorderLayout(15, 15));
        contentArea.setOpaque(false);
        contentArea.setBorder(new EmptyBorder(20, 20, 15, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        estado.setFont(new Font("SansSerif", Font.PLAIN, 13));
        estado.setForeground(COLOR_TEXT_SUB);
        topBar.add(estado, BorderLayout.EAST);
        contentArea.add(topBar, BorderLayout.NORTH);

        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 15, 0));
        centerGrid.setOpaque(false);

        PanelRedondeado cardIzquierda = new PanelRedondeado(25, COLOR_CARD);
        cardIzquierda.setLayout(new BorderLayout());
        cardIzquierda.setBorder(new EmptyBorder(15, 15, 15, 15));

        detalle.setEditable(false);
        detalle.setLineWrap(true);
        detalle.setWrapStyleWord(true);
        detalle.setBackground(COLOR_CARD);
        detalle.setForeground(COLOR_TEXT_MAIN);
        detalle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        detalle.setText("Completa el formulario para ver tus canciones...");
        cardIzquierda.add(new JScrollPane(detalle), BorderLayout.CENTER);

        PanelRedondeado cardDerecha = new PanelRedondeado(25, COLOR_CARD);
        cardDerecha.setLayout(new BorderLayout(10, 10));
        cardDerecha.setBorder(new EmptyBorder(15, 15, 15, 15));

        PanelRedondeado headerPlaylist = new PanelRedondeado(15, COLOR_WHITE);
        headerPlaylist.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel lblPlaylist = new JLabel("Playlist sugerida");
        lblPlaylist.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblPlaylist.setForeground(COLOR_TEXT_MAIN);
        headerPlaylist.add(lblPlaylist);

        songList.setBackground(COLOR_CARD);
        songList.setForeground(COLOR_TEXT_MAIN);
        songList.setFont(new Font("SansSerif", Font.PLAIN, 12));
        songList.setSelectionBackground(COLOR_ACCENT);
        songList.setSelectionForeground(COLOR_WHITE);
        songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        songList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                Cancion c = (Cancion) value;
                super.getListCellRendererComponent(list,
                        (index + 1) + ". " + c.getTitulo() + " - " + c.getArtista()
                                + "  [" + c.getPuntuacion() + "]",
                        index, isSelected, cellHasFocus);
                setBorder(new EmptyBorder(3, 6, 3, 6));
                return this;
            }
        });
        // Doble clic: reproducir la canción elegida
        songList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    reproducirIndice(songList.getSelectedIndex());
                }
            }
        });

        JScrollPane scrollList = new JScrollPane(songList);
        scrollList.setBorder(null);

        cardDerecha.add(headerPlaylist, BorderLayout.NORTH);
        cardDerecha.add(scrollList, BorderLayout.CENTER);

        centerGrid.add(cardIzquierda);
        centerGrid.add(cardDerecha);
        contentArea.add(centerGrid, BorderLayout.CENTER);

        PanelRedondeado playerPanel = new PanelRedondeado(30, COLOR_CARD);
        playerPanel.setLayout(new BorderLayout(15, 0));
        playerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel songInfo = new JPanel(new GridLayout(2, 1));
        songInfo.setOpaque(false);

        songName.setFont(new Font("SansSerif", Font.BOLD, 13));
        songName.setForeground(COLOR_TEXT_MAIN);

        artistName.setFont(new Font("SansSerif", Font.PLAIN, 11));
        artistName.setForeground(COLOR_TEXT_SUB);

        songInfo.add(songName);
        songInfo.add(artistName);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        controls.setOpaque(false);

        JButton btnPrev = crearControlBoton("⏮");
        btnPrev.addActionListener(e -> anterior());

        btnPlayPause = crearControlBoton("▶");
        btnPlayPause.addActionListener(e -> alternarPausa());

        JButton btnNext = crearControlBoton("⏭");
        btnNext.addActionListener(e -> siguiente());

        controls.add(btnPrev);
        controls.add(btnPlayPause);
        controls.add(btnNext);

        playerPanel.add(songInfo, BorderLayout.WEST);
        playerPanel.add(controls, BorderLayout.CENTER);

        page.add(sidebar, BorderLayout.WEST);
        page.add(contentArea, BorderLayout.CENTER);
        page.add(playerPanel, BorderLayout.SOUTH);

        return page;
    }

    // ---------- Componentes reutilizables ----------

    /** Botón rosa usado por FormularioPerfil. */
    public static JButton createStyledButton(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(COLOR_WHITE);
        btn.setBackground(COLOR_ACCENT);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 30, 10, 30));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton crearBotonSidebar(String texto, boolean activo) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", activo ? Font.BOLD : Font.PLAIN, 13));
        btn.setForeground(activo ? COLOR_ACCENT : COLOR_TEXT_MAIN);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton crearControlBoton(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setForeground(COLOR_TEXT_MAIN);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static class PanelRedondeado extends JPanel {
        private final int radio;
        private final Color colorFondo;

        public PanelRedondeado(int radio, Color colorFondo) {
            this.radio = radio;
            this.colorFondo = colorFondo;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(colorFondo);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radio, radio);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}