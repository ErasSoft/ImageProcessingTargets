/** # Hochschule Neubrandenburg
  * # Programm: Bildverarbeitung von Zielmarken
  * # Verfasser: Tino Schuldt
  * # Datum: 13.11.2013
  */

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class kreuzkorrelation extends ApplicationFrame{

  // Programm start ------------------------------------
  public static void main(String[] args) {
    new kreuzkorrelation();
  }

  // Variablen deklarieren ------------------------------------
  String file_output = "output.txt";
  boolean zeige_kreuz = false;
  protected BufferedImage img1, img2;
  int bild_breite;
  int bild_laenge;
  int muster_breite;
  int muster_laenge;
  double[] xlist;
  double[] ylist;

  // Setzen der Grafischen Elemente
  TextField txt_kf = new TextField("0.700",3);
  Label lbl_kf = new Label("Korrelationsfaktor:");
  Button startbutton = new Button("Start");
  Button filternbutton = new Button("Filtern");
  Button exportbutton = new Button("Export");
  TextField txt_muster = new TextField("muster.png",3);
  Label lbl_muster = new Label("Muster Bild:");
  TextField txt_bild = new TextField("bild.png",3);
  Label lbl_bild = new Label("Such Bild:");
  TextArea txt_output = new TextArea("", 5, 40, TextArea.SCROLLBARS_VERTICAL_ONLY);
  Button dialogmusterbutton = new Button(">");
  Button dialogbildbutton = new Button(">");
  FileDialog fd;

  public kreuzkorrelation() {
    super("Bildverarbeitung von Zielmarken");
    setSize(600, 600);
    center();
    setVisible(true);

    // Label, Textfelder, Buttons und Textarea erzeugen
    setLayout(null);
    lbl_muster.setFont(new Font("Arial",Font.BOLD,15));
    lbl_muster.setBackground(Color.white);
    lbl_muster.setBounds(10,30,90,22);
    txt_muster.setFont(new Font("Arial",Font.PLAIN,12));
    txt_muster.setBackground(Color.white);
    txt_muster.setBounds(105,30,320,22);
    lbl_bild.setFont(new Font("Arial",Font.BOLD,15));
    lbl_bild.setBackground(Color.white);
    lbl_bild.setBounds(10,52,90,22);
    txt_bild.setFont(new Font("Arial",Font.PLAIN,12));
    txt_bild.setBackground(Color.white);
    txt_bild.setBounds(105,52,320,22);
    dialogmusterbutton.setBounds(430, 30, 20, 22);
    dialogbildbutton.setBounds(430, 52, 20, 22);
    fd = new FileDialog(this, "Bild suchen");
    lbl_kf.setFont(new Font("Arial",Font.BOLD,15));
    lbl_kf.setBackground(Color.white);
    lbl_kf.setBounds(10,80,140,22);
    txt_kf.setFont(new Font("Arial",Font.PLAIN,12));
    txt_kf.setBackground(Color.white);
    txt_kf.setBounds(155,80,60,22);
    startbutton.setBounds(230, 78, 70, 22);
    filternbutton.setBounds(305, 78, 70, 22);
    filternbutton.setEnabled(false);
    exportbutton.setBounds(380, 78, 70, 22);
    exportbutton.setEnabled(false);
    txt_output.setFont(new Font("Arial",Font.PLAIN,12));
    txt_output.setBackground(Color.white);
    txt_output.setBounds(10,120,440,100);
    txt_output.setEditable(false);
    add(lbl_muster);
    add(txt_muster);
    add(lbl_bild);
    add(txt_bild);
    add(lbl_kf);
    add(txt_kf);
    add(startbutton);
    add(filternbutton);
    add(exportbutton);
    add(txt_output);
    add(dialogmusterbutton);
    add(dialogbildbutton);
    dialogmusterbutton.addActionListener(new ButtonListenerMusterDialog());
    dialogbildbutton.addActionListener(new ButtonListenerBildDialog());
    startbutton.addActionListener(new ButtonListenerStart());
    filternbutton.addActionListener(new ButtonListenerFiltern());
    exportbutton.addActionListener(new ButtonListenerExport());
  }

  // Wenn Button MusterDialog gedrückt wurde ------------------------------------
  class ButtonListenerMusterDialog implements ActionListener{
    @Override
    public void actionPerformed(ActionEvent arg0){
      fd.setVisible(true);
      if(fd.getFile() != null){
        txt_muster.setText(fd.getDirectory()+fd.getFile() );
        openpic_muster();
        zeige_kreuz = false;
        repaint();
      }
    }
  }
  // Wenn Button BildDialog gedrückt wurde ------------------------------------
  class ButtonListenerBildDialog implements ActionListener{
    @Override
    public void actionPerformed(ActionEvent arg0){
      fd.setVisible(true);
      if(fd.getFile() != null){
        txt_bild.setText(fd.getDirectory()+fd.getFile() );
        openpic_bild();
        zeige_kreuz = false;
        repaint();
      }
    }
  }

  // Wenn Button Start gedrückt wurde ------------------------------------
  class ButtonListenerStart implements ActionListener{
    @Override
    public void actionPerformed(ActionEvent arg0) {
      if(!berechne() ){
        // BEI FEHLER: Prüfen ob nichts in der Outputbox steht, wenn ja dann Export Button sperren
        if(txt_output.getText().compareTo("") == 0){
          filternbutton.setEnabled(false);
          exportbutton.setEnabled(false);
        }
      }
    }
  }

  // Wenn Button Export gedrückt wurde ------------------------------------
  class ButtonListenerExport implements ActionListener{
    @Override
    public void actionPerformed(ActionEvent arg0) {
      export();
    }
  }

  // Wenn Button Filtern gedrückt wurde ------------------------------------
  class ButtonListenerFiltern implements ActionListener{
    @Override
    public void actionPerformed(ActionEvent arg0) {
      filtern();
    }
  }

  // Ergebnis Daten exportieren ------------------------------------
  public boolean export(){
    PrintWriter f;
    try {
      f = new PrintWriter(
      new BufferedWriter(new FileWriter(file_output)));
      // Schreiben auf die ASCII-Datei "output.txt" --------------------------
      f.println(txt_output.getText() );
      // Schließen der ASCII-Datei "output.txt" ------------------------------
      f.close();
    }
    catch (IOException e) {
      System.err.println("Fehler beim exportieren der Daten!");
      return false;
    }
    return true;
  }

  // Pixelwerte lesen --------------------------------------
  public boolean lese_pixel(){
    if(txt_output.getText().compareTo("") != 0){
      String input = txt_output.getText();
      String[] zeile = input.split("\n");
      xlist = new double[zeile.length];
      ylist = new double[zeile.length];
        for(int i=0; i<zeile.length; i++){
          String[] spalte = zeile[i].split(" ");
          xlist[i] = Double.parseDouble(spalte[1] );
          ylist[i] = Double.parseDouble(spalte[3] );
        }
      return true;
    }
    return false;
  }

  // Ergebnis Daten filtern auf ein! Bestes Ergebnis ------------------------------------
  public boolean filtern(){
    if(txt_output.getText().compareTo("") != 0){
      String input = txt_output.getText();
      txt_output.setText("");
      String[] zeile = input.split("\n");
      double[] kflist = new double[zeile.length];
      for(int i=0; i<zeile.length; i++){
        String[] spalte = zeile[i].split(" ");
        xlist[i] = Double.parseDouble(spalte[1] );
        ylist[i] = Double.parseDouble(spalte[3] );
        kflist[i] = Double.parseDouble(spalte[5] );
      }

      boolean wert_filtern = false;
      for(int i=0; i<zeile.length; i++){
        for(int j=0; j<zeile.length; j++){
          if(xlist[i]+5 > xlist[j] && xlist[i]-5 < xlist[j] && ylist[i]+5 > ylist[j] && ylist[i]-5 < ylist[j]){
            if(kflist[i] < kflist[j]){
              wert_filtern = true;
              break;
            }
          }
        }
        if(!wert_filtern){
          txt_output.insert(zeile[i]+"\n", -1);
        }
        wert_filtern = false;
      }
      repaint();
      return true;
    }
    return false;
  }

  // Öffnen der Bilddatei Muster ------------------------------------
  public boolean openpic_muster(){
    String file_inputmuster = txt_muster.getText();

    try {
      img2 = ImageIO.read(new File(file_inputmuster));
      txt_muster.setBackground(Color.white);
    }
    catch(Exception exp) {
      System.err.println("Fehler beim Lesen des Musterbildes!");
      txt_muster.setBackground(Color.red);
      return false;
    }
    // Länge und Breite bestimmen
    try{
      muster_breite = img2.getHeight();
      muster_laenge = img2.getWidth();
      txt_muster.setBackground(Color.white);
    }
    catch(Exception exp){
      System.err.println("Fehler beim Lesen des Musterbildes!");
      txt_muster.setBackground(Color.red);
      return false;
    }
    return true;
  }

  // Öffnen der Bilddatei Bild ------------------------------------
  public boolean openpic_bild(){
    String file_inputbild = txt_bild.getText();

    try {
      img1 = ImageIO.read(new File(file_inputbild));
      txt_bild.setBackground(Color.white);
    }
    catch(Exception exp) {
      System.err.println("Fehler beim Lesen des Suchbildes!");
      txt_bild.setBackground(Color.red);
      return false;
    }
    // Länge und Breite bestimmen
    try{
      bild_breite = img1.getHeight();
      bild_laenge = img1.getWidth();
      txt_bild.setBackground(Color.white);
    }
    catch(Exception exp){
      System.err.println("Fehler beim Lesen des Suchbildes!");
      txt_bild.setBackground(Color.red);
      return false;
    }
    return true;
  }

  // Berechnung durchführen ------------------------------------
  public boolean berechne(){
    double korrelationsfaktor = Double.valueOf(txt_kf.getText()).doubleValue();  // KF aus Textbox auslesen
    if(korrelationsfaktor < 0.0 || korrelationsfaktor > 1.0){             // Nur im Bereich 0.0 bis 1.0 aktzeptieren
      txt_kf.setBackground(Color.red);
      return false;
    }
    else{
      txt_kf.setBackground(Color.white);
    }

    // Bilder öffnen/lesen (Wenn Fehler passiert, dann abbruch!)
    if(!openpic_muster() ){
      return false;
    }
    if(!openpic_bild() ){
      return false;
    }

    // Wenn alles okay war, weitermachen...
    txt_output.setText("");
    startbutton.setEnabled(false);
    filternbutton.setEnabled(false);
    exportbutton.setEnabled(false);

    int[] xwerte = new int[muster_breite*muster_laenge];
    int[] ywerte = new int[muster_breite*muster_laenge];

    // Messwerte aus der Matrix bestimmen ------------------------------------
    for(int ya=0; ya<bild_breite-muster_breite+1; ya++){
      int button_prozent = (100 * ya) / (bild_breite-muster_breite+1);
      startbutton.setLabel(button_prozent+"%");
      for(int xa=0; xa<bild_laenge-muster_laenge+1; xa++){
        int n = 0;
        for(int y=ya; y<muster_breite+ya; y++){
          for(int x=xa; x<muster_laenge+xa; x++){
            xwerte[n] = ((img1.getRGB(x,y)) & 0xff);
            ywerte[n] = ((img2.getRGB(x-xa,y-ya)) & 0xff);
            n++;
          }
        }

        // Korrelation berechnen ------------------------------------
        double xmittel = 0.0;
        double ymittel = 0.0;

        for(int i=0; i<xwerte.length; i++){                          // Mittelwerte berechnen
          xmittel += xwerte[i];
          ymittel += ywerte[i];
        }
        xmittel = (double) xmittel / xwerte.length;
        ymittel = (double) ymittel / ywerte.length;
                                                                     // Varianzen berechnen
        double[] xvar = new double[xwerte.length];                   // xvar = s1
        double[] yvar = new double[ywerte.length];                   // yvar = s2
        double[] zvar = new double[xwerte.length];                   // zvar = s12 & s21
        for(int i=0; i<xwerte.length; i++){
          xvar[i] = xmittel - xwerte[i];
          yvar[i] = ymittel - ywerte[i];
          zvar[i] = xvar[i] * yvar[i];
          xvar[i] *= xvar[i];
          yvar[i] *= yvar[i];
        }
        double xvarmittel = 0.0;
        double yvarmittel = 0.0;
        double zvarmittel = 0.0;
        for(int i=0; i<xwerte.length; i++){                          // Varianzen Mittelwerte berechnen
          xvarmittel += xvar[i];
          yvarmittel += yvar[i];
          zvarmittel += zvar[i];
        }
        xvarmittel = (double) xvarmittel / (xvar.length-1);
        yvarmittel = (double) yvarmittel / (yvar.length-1);
        zvarmittel = (double) zvarmittel / (zvar.length-1);
                                                                     // r = zvar / sqrt(s1*s2)
        double xyr = zvarmittel / Math.sqrt(xvarmittel * yvarmittel);

        //if(xyr >= korrelationsfaktor || xyr <= (korrelationsfaktor*-1) ){
        if(xyr >= korrelationsfaktor){
          int xpos = xa+(muster_breite/2);
          int ypos = ya+(muster_breite/2);
          txt_output.insert("X: " + xpos + "\t Y: " + ypos + "\t Korrelationsfaktor: " + xyr + "\n", -1 );
        }
      }
    }
    startbutton.setLabel("Start");
    startbutton.setEnabled(true);
    filternbutton.setEnabled(true);
    exportbutton.setEnabled(true);
    zeige_kreuz = true;
    repaint();
    return true;
  }

  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;

    if(!lese_pixel() ){           // Lese die Pixel Ergebnisse
      zeige_kreuz = false;        // Wenn keine Ausgabe da ist, dann zeige kein Kreuz
    }

    if(img2 != null){
      g2.drawImage(img2, 460, 30, muster_laenge, muster_breite, this);  // Ausgabe des Musters
    }
    if(img1 != null){
      g2.drawImage(img1, 10, 230, bild_laenge, bild_breite, this);      // Ausgabe des Bildes
    }

    if(zeige_kreuz){
      g2.setPaint(Color.red);
      for(int i=0; i<xlist.length; i++){
        Line2D l = new Line2D.Float();
        xlist[i] += 10;
        ylist[i] += 230;
        l.setLine(xlist[i], ylist[i]-5, xlist[i], ylist[i]+5);
        g2.draw(l);
        l.setLine(xlist[i]-5, ylist[i], xlist[i]+5, ylist[i]);
        g2.draw(l);
      }
    }
  }

  protected Shape getControlPoint(Point2D p) {
    int side = 4;
    return new Rectangle2D.Double(
    p.getX() - side / 2, p.getY() - side / 2, side, side);
  }

}

