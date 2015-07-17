/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interfaz;

import com.digitalpersona.onetouch.DPFPDataPurpose;
import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPSample;
import com.digitalpersona.onetouch.DPFPTemplate;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.capture.event.DPFPErrorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPErrorEvent;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;
import com.digitalpersona.onetouch.capture.event.DPFPSensorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPSensorEvent;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author machu_000
 */
public final class SalidaVisitante extends javax.swing.JFrame {

    /**
     * Creates new form ingresoVisitante
     */
    public SalidaVisitante() {
        try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
         JOptionPane.showMessageDialog(null, "Imposible modificar el tema visual", "Lookandfeel inválido.",
         JOptionPane.ERROR_MESSAGE);
         }
        initComponents();
        txtArea.setEditable(false);
        this.setLocationRelativeTo(null);
     }
    
 
    //Varible que permite iniciar el dispositivo de lector de huella conectado
// con sus distintos metodos.
private DPFPCapture Lector = DPFPGlobal.getCaptureFactory().createCapture();

//Varible que permite establecer las capturas de la huellas, para determina sus caracteristicas
// y poder estimar la creacion de un template de la huella para luego poder guardarla
private DPFPEnrollment Reclutador = DPFPGlobal.getEnrollmentFactory().createEnrollment();

//Esta variable tambien captura una huella del lector y crea sus caracteristcas para auntetificarla
// o verificarla con alguna guardada en la BD
private DPFPVerification Verificador = DPFPGlobal.getVerificationFactory().createVerification();

//Variable que para crear el template de la huella luego de que se hallan creado las caracteriticas
// necesarias de la huella si no ha ocurrido ningun problema
private DPFPTemplate template;
public static String TEMPLATE_PROPERTY = "template";

protected void Iniciar(){
   Lector.addDataListener(new DPFPDataAdapter() {
    @Override public void dataAcquired(final DPFPDataEvent e) {
    SwingUtilities.invokeLater(new Runnable() {	public void run() {
    EnviarTexto("La Huella Digital ha sido Capturada");
    ProcesarCaptura(e.getSample());
    }});}
   });

   Lector.addReaderStatusListener(new DPFPReaderStatusAdapter() {
    @Override public void readerConnected(final DPFPReaderStatusEvent e) {
    SwingUtilities.invokeLater(new Runnable() {	public void run() {
    EnviarTexto("El Sensor de Huella Digital esta Activado o Conectado");
    }});}
    @Override public void readerDisconnected(final DPFPReaderStatusEvent e) {
    SwingUtilities.invokeLater(new Runnable() {	public void run() {
    EnviarTexto("El Sensor de Huella Digital esta Desactivado o no Conectado");
    }});}
   });

   Lector.addSensorListener(new DPFPSensorAdapter() {
    @Override public void fingerTouched(final DPFPSensorEvent e) {
    SwingUtilities.invokeLater(new Runnable() {	public void run() {
    EnviarTexto("El dedo ha sido colocado sobre el Lector de Huella");
    }});}
    @Override public void fingerGone(final DPFPSensorEvent e) {
    SwingUtilities.invokeLater(new Runnable() {	public void run() {
    EnviarTexto("El dedo ha sido quitado del Lector de Huella");
    }});}
   });

   Lector.addErrorListener(new DPFPErrorAdapter(){
    public void errorReader(final DPFPErrorEvent e){
    SwingUtilities.invokeLater(new Runnable() {  public void run() {
    EnviarTexto("Error: "+e.getError());
    }});}
   });
}

 public DPFPFeatureSet featuresinscripcion;
 public DPFPFeatureSet featuresverificacion;

 public  void ProcesarCaptura(DPFPSample sample)
 {
 // Procesar la muestra de la huella y crear un conjunto de características con el propósito de inscripción.
 featuresinscripcion = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);

 // Procesar la muestra de la huella y crear un conjunto de características con el propósito de verificacion.
 featuresverificacion = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

 // Comprobar la calidad de la muestra de la huella y lo añade a su reclutador si es bueno
 if (featuresinscripcion != null)
     try{
     System.out.println("Las Caracteristicas de la Huella han sido creada");
     Reclutador.addFeatures(featuresinscripcion);// Agregar las caracteristicas de la huella a la plantilla a crear

     // Dibuja la huella dactilar capturada.
     Image image=CrearImagenHuella(sample);
     DibujarHuella(image);
     

     }catch (DPFPImageQualityException ex) {
     System.err.println("Error: "+ex.getMessage());
     }

     finally {
     EstadoHuellas();
     // Comprueba si la plantilla se ha creado.
	switch(Reclutador.getTemplateStatus())
        {
            case TEMPLATE_STATUS_READY:	// informe de éxito y detiene  la captura de huellas
	    stop();
            setTemplate(Reclutador.getTemplate());
	    EnviarTexto("La Plantilla de la Huella ha Sido Creada, ya puede Verificarla o Identificarla");
	  
            break;

	    case TEMPLATE_STATUS_FAILED: // informe de fallas y reiniciar la captura de huellas
	    Reclutador.clear();
            stop();
	    EstadoHuellas();
	    setTemplate(null);
	    JOptionPane.showMessageDialog(SalidaVisitante.this, "La Plantilla de la Huella no pudo ser creada, Repita el Proceso", "Inscripcion de Huellas Dactilares", JOptionPane.ERROR_MESSAGE);
	    start();
	    break;
	}
	     }
}
 
public  DPFPFeatureSet extraerCaracteristicas(DPFPSample sample, DPFPDataPurpose purpose){
     DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
     try {
      return extractor.createFeatureSet(sample, purpose);
     } catch (DPFPImageQualityException e) {
      return null;
     }
}

  public  Image CrearImagenHuella(DPFPSample sample) {
	return DPFPGlobal.getSampleConversionFactory().createImage(sample);
}

  public void DibujarHuella(Image image) {
        lblImagenHuella.setIcon(new ImageIcon(
        image.getScaledInstance(lblImagenHuella.getWidth(), lblImagenHuella.getHeight(), Image.SCALE_DEFAULT)));
        repaint();
 }

public  void EstadoHuellas(){
	EnviarTexto("Muestra de Huellas Necesarias para Guardar Template "+ Reclutador.getFeaturesNeeded());
}

public void EnviarTexto(String string) {
        txtArea.append(string + "\n");
}

public  void start(){
	Lector.startCapture();
	EnviarTexto("Utilizando el Lector de Huella Dactilar ");
}

public  void stop(){
        Lector.stopCapture();
        EnviarTexto("No se está usando el Lector de Huella Dactilar ");
}

    public DPFPTemplate getTemplate() {
        return template;
    }

    public void setTemplate(DPFPTemplate template) {
        DPFPTemplate old = this.template;
	this.template = template;
	firePropertyChange(TEMPLATE_PROPERTY, old, template);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtNombreVisitante = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtapellidoVisitante = new javax.swing.JTextField();
        txtFecha_naci = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtFecha_naci1 = new javax.swing.JTextField();
        jpResidencia = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jbResidencia = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        jtAutorizo = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jtAutorizo1 = new javax.swing.JTextField();
        jpObservaciones = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jbResidencia1 = new javax.swing.JComboBox();
        jLabel13 = new javax.swing.JLabel();
        jtPlaca = new javax.swing.JTextField();
        jbResidencia2 = new javax.swing.JComboBox();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel12 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jbIngresoVisitante = new javax.swing.JButton();
        jpHuellaCampos = new javax.swing.JPanel();
        jToggleButton1 = new javax.swing.JToggleButton();
        jbHuella = new javax.swing.JPanel();
        lblImagenHuella = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jToggleButton2 = new javax.swing.JToggleButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Ingreso Visitante");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Registo Nuevo Visitante", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(""), "Datos Basicos", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/icon_user.png"))); // NOI18N

        jLabel3.setText("Nombre");

        jLabel4.setText("Apellido");

        jLabel5.setText("Fecha Nacimiento");

        jLabel6.setText("Cedula");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtapellidoVisitante, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtNombreVisitante, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtFecha_naci, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtFecha_naci1, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtNombreVisitante, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtapellidoVisitante, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtFecha_naci, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtFecha_naci1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))))
                .addContainerGap(81, Short.MAX_VALUE))
        );

        jpResidencia.setBackground(new java.awt.Color(255, 255, 255));
        jpResidencia.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Datos Del Residencia a Visitar", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        jLabel8.setText("Casa");

        jbResidencia.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Casa 01", "Casa 02", "Casa 03", "Casa 04", "Casa 05", "Casa 06" }));

        jLabel9.setText("Quien Autorizo");

        jLabel10.setText("Cantidad Ingreso");

        javax.swing.GroupLayout jpResidenciaLayout = new javax.swing.GroupLayout(jpResidencia);
        jpResidencia.setLayout(jpResidenciaLayout);
        jpResidenciaLayout.setHorizontalGroup(
            jpResidenciaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpResidenciaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbResidencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jtAutorizo, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jtAutorizo1, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jpResidenciaLayout.setVerticalGroup(
            jpResidenciaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpResidenciaLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jpResidenciaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpResidenciaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel10)
                        .addComponent(jtAutorizo1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jpResidenciaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel8)
                        .addComponent(jbResidencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel9)
                        .addComponent(jtAutorizo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jpObservaciones.setBackground(new java.awt.Color(255, 255, 255));
        jpObservaciones.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Observaciones", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));

        jLabel11.setText("Visitante");

        jbResidencia1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Visitante", "Proveedor", "Moto", "Carro", "Camioneta", "Bicicleta", "Furgoneta", " " }));

        jLabel13.setText("Placa");

        jbResidencia2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Blanco", "Negro", "Rojo", "Azul", "Verde", "Gris", "Amarillo", " " }));

        jLabel14.setText("Color");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        jLabel12.setText("Observaciones");

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/icon_camara.png"))); // NOI18N
        jLabel15.setText("Agregar Foto Observaciones");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jpObservacionesLayout = new javax.swing.GroupLayout(jpObservaciones);
        jpObservaciones.setLayout(jpObservacionesLayout);
        jpObservacionesLayout.setHorizontalGroup(
            jpObservacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpObservacionesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpObservacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpObservacionesLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jbResidencia1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jbResidencia2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(38, 38, 38)
                        .addComponent(jLabel13))
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jpObservacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpObservacionesLayout.createSequentialGroup()
                        .addComponent(jtPlaca, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel12))
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        jpObservacionesLayout.setVerticalGroup(
            jpObservacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpObservacionesLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jpObservacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jbResidencia1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(jtPlaca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(jbResidencia2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addGap(18, 18, 18)
                .addGroup(jpObservacionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jpObservacionesLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jbIngresoVisitante.setText("Salida Visitante");

        jpHuellaCampos.setBackground(new java.awt.Color(255, 255, 255));

        jToggleButton1.setText("Capturar Huella");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jbHuella.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jbHuella.setLayout(new java.awt.BorderLayout());
        jbHuella.add(lblImagenHuella, java.awt.BorderLayout.CENTER);

        jLabel7.setText("Huella");

        jToggleButton2.setText("Verificar Datos Visitante");
        jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpHuellaCamposLayout = new javax.swing.GroupLayout(jpHuellaCampos);
        jpHuellaCampos.setLayout(jpHuellaCamposLayout);
        jpHuellaCamposLayout.setHorizontalGroup(
            jpHuellaCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpHuellaCamposLayout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(jpHuellaCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpHuellaCamposLayout.createSequentialGroup()
                        .addComponent(jToggleButton1)
                        .addGap(18, 18, 18)
                        .addComponent(jToggleButton2))
                    .addGroup(jpHuellaCamposLayout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addComponent(jbHuella, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jpHuellaCamposLayout.setVerticalGroup(
            jpHuellaCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpHuellaCamposLayout.createSequentialGroup()
                .addGroup(jpHuellaCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpHuellaCamposLayout.createSequentialGroup()
                        .addGap(47, 47, 47)
                        .addComponent(jLabel7))
                    .addGroup(jpHuellaCamposLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jbHuella, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpHuellaCamposLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jToggleButton1)
                    .addComponent(jToggleButton2))
                .addGap(35, 35, 35))
        );

        txtArea.setColumns(20);
        txtArea.setFont(new java.awt.Font("Lucida Sans", 1, 10)); // NOI18N
        txtArea.setRows(5);
        jScrollPane1.setViewportView(txtArea);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jpResidencia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jpHuellaCampos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jpObservaciones, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(357, 357, 357)
                .addComponent(jbIngresoVisitante)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(24, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jpHuellaCampos, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(13, 13, 13)
                .addComponent(jpResidencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jpObservaciones, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(56, 56, 56)
                .addComponent(jbIngresoVisitante))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 759, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        // TODO add your handling code here:
        Iniciar();
        start();
        EstadoHuellas();
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SalidaVisitante.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SalidaVisitante.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SalidaVisitante.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SalidaVisitante.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SalidaVisitante().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JPanel jbHuella;
    private javax.swing.JButton jbIngresoVisitante;
    private javax.swing.JComboBox jbResidencia;
    private javax.swing.JComboBox jbResidencia1;
    private javax.swing.JComboBox jbResidencia2;
    private javax.swing.JPanel jpHuellaCampos;
    private javax.swing.JPanel jpObservaciones;
    private javax.swing.JPanel jpResidencia;
    private javax.swing.JTextField jtAutorizo;
    private javax.swing.JTextField jtAutorizo1;
    private javax.swing.JTextField jtPlaca;
    private javax.swing.JLabel lblImagenHuella;
    private javax.swing.JTextArea txtArea;
    private javax.swing.JTextField txtFecha_naci;
    private javax.swing.JTextField txtFecha_naci1;
    private javax.swing.JTextField txtNombreVisitante;
    private javax.swing.JTextField txtapellidoVisitante;
    // End of variables declaration//GEN-END:variables
}
