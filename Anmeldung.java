package jav12Einsendeaufgaben.angestellterAnmeldung;

import jav12Einsendeaufgaben.angestellterAnmeldung.db.DbManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import jav12Einsendeaufgaben.angestellterAnmeldung.daten.Angestellter;
import jav12Einsendeaufgaben.angestellterAnmeldung.daten.Person;
import jav12Einsendeaufgaben.angestellterAnmeldung.daten.Abteilung;

public class Anmeldung extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    // Fields
    private JLabel jlHeadline, jlFirstname, jlLastname, jlPassword, jlDbUsername;
    private JTextField jtfStatus, jtfFirstname, jtfLastname, jtfDBUsername;
    private JPasswordField jpfPassword;
    private JButton jbLogin, jbExit;

    private static String defaultDBUser = "demo-user";
    private DbManager dbManager;

    private Person person;
    private Angestellter angestellter;
    private Abteilung abteilung;

    private String firstName, lastName;
    public String getFirstName(){ return firstName; }
    public void setFirstName(String value){ firstName = value; }
    public String getLastName(){ return lastName; }
    public void setLastName(String value){ lastName = value; }

    // Constructor
    public Anmeldung(String title) {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.createGUI();
        this.setSize(600, 300);
        this.setLocation(100, 100);
        this.setVisible(true);
        jtfFirstname.requestFocus();
    }

    public static void main(String[] args) {
        new Anmeldung("Anmeldung");
    }

    public void createGUI() {
        // Labels
        jlHeadline = new JLabel("Personalverwaltung - Zugang nur für Abteilungsleiter", JLabel.CENTER);
        jlHeadline.setFont(new Font("Serif", Font.BOLD, 20));
        jlHeadline.setForeground(Color.BLUE);
        jlFirstname = new JLabel("Vorname:", JLabel.LEFT);
        jlLastname = new JLabel("Nachname:", JLabel.LEFT);
        jlPassword = new JLabel("Password:", JLabel.LEFT);
        jlDbUsername = new JLabel("DB-Benutzername:", JLabel.RIGHT);
        this.add(BorderLayout.NORTH, jlHeadline);
        // Text Fields
        jtfFirstname = new JTextField();
        jtfLastname = new JTextField();
        jpfPassword = new JPasswordField();
        jtfDBUsername = new JTextField(defaultDBUser);
        // Status Text Field
        jtfStatus = new JTextField("Status Field");
        jtfStatus.setFont(new Font("Monospaced", Font.PLAIN, 12));
        jtfStatus.setForeground(Color.WHITE);
        jtfStatus.setBackground(Color.BLACK);
        jtfStatus.setEditable(false);
        this.add(BorderLayout.SOUTH, jtfStatus);
        // Buttons
        jbLogin = new JButton("Anmelden");
        jbLogin.addActionListener(this);
        jbExit = new JButton("Beenden");
        jbExit.addActionListener(this);
        // Content Create
        JPanel jPanelMain = new JPanel(new GridLayout(4, 3, 15, 20));
        jPanelMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10 ));
        jPanelMain.add(jlFirstname);
        jPanelMain.add(jlLastname);
        jPanelMain.add(jlPassword);
        jPanelMain.add(jtfFirstname);
        jPanelMain.add(jtfLastname);
        jPanelMain.add(jpfPassword);
        jPanelMain.add(new JLabel(""));
        jPanelMain.add(jlDbUsername);
        jPanelMain.add(jtfDBUsername);
        jPanelMain.add(new JLabel(""));
        jPanelMain.add(jbLogin);
        jPanelMain.add(jbExit);
        this.add(BorderLayout.CENTER, jPanelMain);
    }

    // Action Methods
    @Override
    public void actionPerformed(ActionEvent ae) {
        Object eventTarget = ae.getSource();
        if (ae.getSource() == jbLogin) {
            this.loginAction();
        } else if (ae.getSource() == jbExit) {
            this.exitAction();
        }
    }

    public void loginAction() {
        String name = jtfFirstname.getText().trim();
        String lastName = jtfLastname.getText().trim();
        if (name.equals("") || lastName.equals("")) {
            System.out.println("Name or lastname is not provided.");
            jtfStatus.setText("You should provide name and lastname");
        } else {
            this.getDbManager();
            if (person == null || !person.getVorname().equals(name) || !person.getNachname().equals(lastName)) {
                person = new Person(name, lastName);
            }
            dbManager.startTransaction();
            jtfStatus.setText(dbManager.getMessage());
            // Is this person in DB?
            if ((person = (Person) person.retrieveObject(dbManager)) != null) {
                // Is this person Angestellter?
                if (angestellter == null || angestellter.getPerson().getID() != person.getID()) {
                    angestellter = new Angestellter(person);
                    if((angestellter = (Angestellter) angestellter.retrieveObject(dbManager)) != null){
                        // Is this Angestellter Abteilungsleiter?
                        if(abteilung == null || abteilung.getAngestellter().getID() != angestellter.getID()){
                            abteilung = new Abteilung(angestellter);
                            if((abteilung = (Abteilung) abteilung.retrieveObject(dbManager)) != null){
                                jtfStatus.setText("Zugang Erfolgreich!");
                                dbManager.endTransaction(true);
                            } else{
                                jtfStatus.setText("Zugang Verweigert: " + name + " " + lastName + " ist kein Abteilungsleiter");
                            }
                        }
                    }else{
                        jtfStatus.setText("Zugang Verweigert: " + name + " " + lastName + " ist kein Angestellter");
                        dbManager.endTransaction(true);
                    }
                }
            } else {
                jtfStatus.setText("Zugang Verweigert: " + name + " " + lastName + " ist kein Employee");
                dbManager.endTransaction(true);
            }
        }
    }

    public void exitAction() {
        this.dispose();
        System.exit(0);
    }

    public void getDbManager() {
        char[] passArray = jpfPassword.getPassword();
        try {
            if (dbManager == null) {
                jtfStatus.setText("Connection will be created");
                System.out.println("Connection will be created");
                dbManager = new DbManager("localhost", defaultDBUser, passArray);
                System.out.println("Connection created.");
                jtfStatus.setText("Connection created.");
                for (@SuppressWarnings("unused") char c : passArray)
                    c = 0;
            }
        } catch(ClassNotFoundException cnfe) {
            System.out.println("Anmeldung#getDbManager: " + cnfe.getMessage());
            jtfStatus.setText("Anmeldung#getDbManager: " + cnfe.getMessage());
        } catch(SQLException sqle) {
            System.out.println("Anmeldung#getDbManager: " + sqle.getMessage());
            jtfStatus.setText("Anmeldung#getDbManager: " + sqle.getMessage());
        }
    }

}
