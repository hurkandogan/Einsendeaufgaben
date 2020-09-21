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

    // DB Fields
    private static String defaultDBUser = "demo-user";
    private DbManager dbManager;

    private Person person;
    private Angestellter angestellter;
    private Abteilung abteilung;

    // Constructor
    public Anmeldung(String title) {
        super(title);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.createGUI();
        this.setSize(600, 300);
        this.setLocation(50, 50);
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
        String name = jtfFirstname.getText();
        String lastName = jtfLastname.getText();
        if (name.equals("") && lastName.equals("")) {
            System.out.println("Name and lastname is not provided.");
            jtfStatus.setText("You should provide name and lastname");
        } else {
            this.getDbManager();
            if (person == null) {
                person = new Person(name, lastName);
            }
            dbManager.startTransaction();
            if (person.retrieveObject(dbManager)) {
                if (angestellter == null) {
                    angestellter = new Angestellter(person);
                    angestellter.retrieveObject(dbManager);
                    jtfStatus.setText("Name: " + angestellter.getPerson().getVorname() + " Abteilung: " + angestellter.getAbteilung());
                    dbManager.endTransaction(true);
                } else {
                    jtfStatus.setText("This Person is not an Employee");
                    dbManager.endTransaction(true);
                }
            } else {
                jtfStatus.setText("Mit diesen Angaben ist niemand gefunden.");
                dbManager.endTransaction(true);
            }
            dbManager.endTransaction(true);
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
                dbManager = new DbManager("localhost", "demo-user", passArray);
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
