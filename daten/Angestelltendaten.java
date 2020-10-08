package jav12Einsendeaufgaben.angestellterAnmeldung.daten;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jav12Einsendeaufgaben.angestellterAnmeldung.db.DbManager;
import jav12Einsendeaufgaben.angestellterAnmeldung.daten.Abteilung;

public class Angestelltendaten extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	// GUI-Elemente
	private JTextField jtfVorname, jtfNachname, jtfGehalt, jtfAusscheidedatum, jtfStatus;
	private JButton jbSuchen, jbClose, jbEintragen;
	private JComboBox jcbAbteilungen;

	private DbManager dbManager;
	private Person person;
	private Angestellter angestellter;
	private Abteilung abteilung;
	private Vector<String> abteilungsnamen;

	public Angestelltendaten() {}

	public Angestelltendaten(Frame owner, DbManager dbManager) {
		super(owner, "Angestelltendaten bearbeiten", true);
		this.dbManager = dbManager;
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				Angestelltendaten.this.closeActionPerformed();
			}
		}); // TODO: Bu method Anmeldung penceresi icinde yazilmali
		this.createGUI();
		this.setSize(600,300);
		// setVisible-Aufruf durch "owner"!
		Point ownerLoc = owner.getLocation();
		this.setLocation(ownerLoc.x + 40, ownerLoc.y + 40);
		jtfStatus.setText("Beginnen Sie mit der Suche nach einem Angestellten");
	}

	public void createGUI() {
		Color colorWeinrot=new Color(193, 30, 91);
		// Labels
		JLabel jlabVorname=new JLabel("Vorname", JLabel.LEFT);
		JLabel jlabNachname=new JLabel("Nachname", JLabel.LEFT);
		JLabel jlabGehalt=new JLabel("Gehalt (in $)", JLabel.LEFT);
		JLabel jlabAusscheidedatum=new JLabel("ausgeschieden: YYYY-MM-DD", JLabel.LEFT);
		JLabel jlabAbteilung=new JLabel("Abteilung", JLabel.LEFT);
		// Textfelder
		jtfVorname=new JTextField("", 15);
		jtfNachname=new JTextField("");
		jtfGehalt=new JTextField("");
		jtfAusscheidedatum = new JTextField("");
		jtfStatus=new JTextField("Statuszeile");
		jtfStatus.setFont(new Font("Monospaced", Font.PLAIN, 12));
		jtfStatus.setForeground(Color.white);
		jtfStatus.setBackground(Color.black);
		jtfStatus.setEditable(false);
		// Schaltflächen
		jbSuchen = new JButton("Suchen");
		jbSuchen.addActionListener(this);
		jbEintragen=new JButton("Daten ändern");
		jbEintragen.addActionListener(this);
		jbClose=new JButton("Schließen");
		jbClose.setForeground(colorWeinrot);
		jbClose.addActionListener(this);
		// JComboBox Abteilungen
		abteilung = new Abteilung();
		abteilungsnamen = dbManager.getAbteilungsnamen();
		jcbAbteilungen = new JComboBox(abteilungsnamen);
		// Layout-Zusammenbau - BorderLayout ist default - alle Objekte in einem Panel
		JPanel jtfPanel=new JPanel(new GridLayout(6,3, 10, 10));
		jtfPanel.setBorder(BorderFactory.createEmptyBorder(15,20,15,20));
		jtfPanel.add(jlabVorname);
		jtfPanel.add(jlabNachname);
		jtfPanel.add(new Label(""));

		jtfPanel.add(jtfVorname);
		jtfPanel.add(jtfNachname);
		jtfPanel.add(jbSuchen);

		jtfPanel.add(jlabGehalt);
		jtfPanel.add(jtfGehalt);
		jtfPanel.add(new Label(""));

		jtfPanel.add(jlabAusscheidedatum);
		jtfPanel.add(jtfAusscheidedatum);
		jtfPanel.add(new Label(""));

		jtfPanel.add(jlabAbteilung);
		jtfPanel.add(jcbAbteilungen);
		jtfPanel.add(new Label(""));

		jtfPanel.add(new JLabel(""));
		jtfPanel.add(jbEintragen);
		jtfPanel.add(jbClose);
		this.add(BorderLayout.CENTER, jtfPanel);
		this.add(BorderLayout.SOUTH, jtfStatus);

		jtfVorname.requestFocus();
	}

	public void actionPerformed(ActionEvent ae) {
		Object eventTarget = ae.getSource();
		if(eventTarget == jbSuchen)
			this.sucheAngestellten();
		if (eventTarget == jbEintragen)
			aktualisierenActionPerformed();
		if (eventTarget == jbClose)
			closeActionPerformed();
	}

	private void sucheAngestellten() {
		if(checkInput()){
			String vorname = jtfVorname.getText().trim();
			String nachname = jtfNachname.getText().trim();
			person = new Person(vorname, nachname);
			person = (Person) person.retrieveObject(dbManager);
			if(person == null) {
				System.out.println("Person is not found!");
				jtfStatus.setText("Person is not found!");
				this.resetTextfelder();
				return;
			} else {
				System.out.println("Person found!");
				jtfStatus.setText("Person found!");
			}
			angestellter = new Angestellter(person);
			angestellter = (Angestellter) angestellter.retrieveObject(dbManager);
				try {
					if(isAngestellterFound(angestellter)) {
						System.out.println("Works: " + angestellter.toString());
						System.out.println("Works2: " + angestellter.getAbteilung().getName());
						angestellter.setAbteilung(dbManager);
						zeigeDaten();
					} else {
						this.resetTextfelder();
					}
				}catch(NullPointerException npe) {
					this.resetTextfelder();
					System.out.println(npe.toString());
				}
		} else {
			jtfStatus.setText("Vorname und Nachname müssen eingegeben werden!");
			System.out.println("Vorname und Nachname müssen eingegeben werden!");
		}
	}
	private boolean isAngestellterFound(Angestellter angestellter) {
		return angestellter.getGeschlecht() != null || angestellter.getGehalt() != 0 || angestellter.getAbtID() != 0
				|| angestellter.getEinstellungsDatum() != null;
	}

	/* setzt die GUI-Eingabe- bzw. Auswahlkomponenten (ohne Namensfelder) auf default zurück. */
	private void resetTextfelder() {
		jtfGehalt.setText("");
		jtfAusscheidedatum.setText("");
		jcbAbteilungen.setSelectedIndex(0);
	}

	/* Fällt ausgewählte Angestelltendaten in die Maske */
	public void zeigeDaten() {
		if(angestellter != null) {
			jtfGehalt.setText(""+angestellter.getGehalt());
			java.sql.Date date = angestellter.getAusscheidedatum();
			if(date != null)
				jtfAusscheidedatum.setText(date.toString());
			jcbAbteilungen.setSelectedItem(angestellter.getAbteilung().getName());
		}
	}

	/* Liest die Daten (ohne Name) aus den Textfeldern, prüft
	 * das Zahlenformat zu "Gehalt" sowie das Datumsformat.
	 * @return true, falls kein Fehler auftrat, sonst false */
	private boolean leseTextfelder() {
		if(angestellter != null) {
			String gehaltStr = jtfGehalt.getText().trim();
			gehaltStr = gehaltStr.replace(',', '.');
			try {
				float gehalt = Float.parseFloat(gehaltStr);
				angestellter.setGehalt(gehalt);
			} catch(NumberFormatException nfe) {
				jtfStatus.setText("Fehler im Zahlenformat f�r Gehalt");
				return false;
			}
			String datumStr = jtfAusscheidedatum.getText();
			// Kontrolle auf k e i n Datum
			if(datumStr.equals(""))
				angestellter.setAusscheidedatum(null);
			// Kontrolle korrektes Datumsformat
			else if(datumStr.length() == 10) {
				int firstPoint = datumStr.indexOf('-');
				int secondPoint = datumStr.lastIndexOf('-');
				/* vgl. zur absonderlichen Codierung des java.sql.Date-Objekts (deprecated Methode!)
				 * die Dokumentation zum hier verwendeten Date-Konstruktor! */
				int year = Integer.parseInt(datumStr.substring(0, firstPoint))-1900;
				int month = Integer.parseInt(datumStr.substring(firstPoint+1, secondPoint))-1;
				int day = Integer.parseInt(datumStr.substring(secondPoint+1));
				angestellter.setAusscheidedatum(new java.sql.Date(year, month, day));
				// Kontrolle:
				System.out.println(year + "-" + month + "-" + day);
			} else {
				jtfStatus.setText("Fehler im Datumsformat");
				return false;
			}
			Abteilung abteilung = new Abteilung((String)jcbAbteilungen.getSelectedItem());
			abteilung = (Abteilung) abteilung.retrieveObject(dbManager);
			angestellter.setAbteilung(abteilung);
			angestellter.setAbtID(abteilung.getID());
			return true;
		}
		return false;
	}

	private boolean checkInput(){
		// teste Vollständigkeit und Korrektheit der Namensdatenfelder
		return (checkSingleInput(jtfNachname.getText())
			&& checkSingleInput(jtfVorname.getText())
		);
	}
	private boolean checkSingleInput(String instring){
		// teste Korrektheit des Datenfeldes
		return ((instring != null) && (!instring.equals("")));
	}

	private void closeActionPerformed() {
		this.setVisible(false);
		this.dispose();
		if(onlyTest)
			System.exit(0);
	}
	/* In Gegensatz zu Lektion 5 werden hier updates einzeln ausgeführt.
	 * "updateAll" wäre aber auch verwendbar, wenn dies beim EventHandling der beiden
	 * Buttons "Daten ändern" und "Schließen" entsprechend codiert wird. */
	private void aktualisierenActionPerformed() {
		if(this.leseTextfelder()) {
			angestellter.updateObject(dbManager);
		} else
			return;	// dadurch bleiben Fehlermeldungen beim Update in der Statusleiste
		java.util.Date date = new java.util.Date();
		jtfStatus.setText("Daten geändert (" + date.toLocaleString() + ")"); // deprecated, aber brauchbar
	}

	/* *************** Testbereich **************** */
	private static boolean onlyTest = false;

	public static void main(String[] args) {
		onlyTest = true;
		DbManager dbManager = null;
		try {
			if (dbManager == null) {
				System.out.println("Verbindung wird aufgebaut");
				dbManager = new DbManager("localhost", "demo-user", new char[0]);
				System.out.println("Verbindung erfolgreich aufgebaut");
			}
		} catch (ClassNotFoundException cnfe) {
			System.out.println(cnfe.toString());
		} catch (SQLException sqle) {
			System.out.println(sqle.toString());
		}
		Angestelltendaten ad = new Angestelltendaten(new Frame(), dbManager);
		ad.setVisible(true);
	}
}
