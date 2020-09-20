package jav12Einsendeaufgaben.angestellterAnmeldung;

import jav12Einsendeaufgaben.angestellterAnmeldung.db.DbManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import jav12Einsendeaufgaben.angestellterAnmeldung.daten.Angestellter;
import jav12Einsendeaufgaben.angestellterAnmeldung.daten.Person;

public class Anmeldung extends JFrame implements ActionListener {

    // Fields
    private JLabel jlHeadline, jlFirstname, jlLastname, jlPassword, jlDbUsername;
    private JTextField jtfStatus, jtfFirstname, jtfLastname, jtfDBUsername;
    private JPasswordField jpfPassword;
    private JButton jbLogin, jbExit;
    private String defaultDBUser = "demo-user";

    private DbManager dbManager;

    private Angestellter angestellter;

    // Constructor
    public Anmeldung(String title) {
        super(title);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.createGUI();
        this.setSize(600, 400);
        this.setLocation(50, 50);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        new Anmeldung("Anmeldung");
    }

    public void createGUI() {
        // Labels
        jlHeadline = new JLabel("Personalverwaltung - Zugang nur für Abteilungsleiter", JLabel.CENTER);
        jlHeadline.setFont(new Font("Serif", Font.BOLD, 25));
        jlHeadline.setForeground(Color.BLUE);
        jlHeadline.setHorizontalTextPosition(SwingConstants.CENTER);
        jlFirstname = new JLabel("Vorname");
        jlLastname = new JLabel("Nachname");
        jlPassword = new JLabel("Password");
        jlDbUsername = new JLabel("DB-Benutzername");
        // Text Fields
        jtfFirstname = new JTextField();
        jtfLastname = new JTextField();
        jpfPassword = new JPasswordField();
        jtfDBUsername = new JTextField(defaultDBUser);
        jtfStatus = new JTextField("Status Field");
        jtfStatus.setFont(new Font("Monospaced", Font.PLAIN, 12));
        jtfStatus.setForeground(Color.WHITE);
        jtfStatus.setBackground(Color.BLACK);
        jtfStatus.setEditable(false);
        // Buttons
        jbLogin = new JButton("Anmelden");
        jbLogin.addActionListener(this);
        jbExit = new JButton("Beenden");
        jbExit.addActionListener(this);

        // Content Create
        JPanel jPanelHeadline = new JPanel(new GridLayout(1, 1, 20, 20));
        jPanelHeadline.add(jlHeadline);
        this.add(BorderLayout.NORTH, jPanelHeadline);

        JPanel jPanelMain = new JPanel(new GridLayout(1, 3));
        jPanelMain.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel jPanelLeft = new JPanel(new GridLayout(4, 1, 5, 5));
        jPanelLeft.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanelLeft.add(jlFirstname);
        jPanelLeft.add(jtfFirstname);
        jtfFirstname.setPreferredSize(new Dimension(20, 10));
        jPanelLeft.add(new JLabel());
        jPanelLeft.add(new JLabel());
        jPanelMain.add(jPanelLeft);

        JPanel jPanelCenter = new JPanel(new GridLayout(4, 1, 5, 5));
        jPanelCenter.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanelCenter.add(jlLastname);
        jPanelCenter.add(jtfLastname);
        jPanelCenter.add(jlDbUsername);
        jPanelCenter.add(jbLogin);
        jPanelMain.add(jPanelCenter);

        JPanel jPanelRight = new JPanel(new GridLayout(4, 1, 5, 5));
        jPanelRight.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 5));
        jPanelRight.add(jlPassword);
        jPanelRight.add(jpfPassword);
        jPanelRight.add(jtfDBUsername);
        jPanelRight.add(jbExit);
        jPanelMain.add(jPanelRight);

        this.add(BorderLayout.CENTER, jPanelMain);
        this.add(BorderLayout.SOUTH, jtfStatus);
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
        this.getDbManager();
        String name = jtfFirstname.getText();
        String lastName = jtfLastname.getText();
        Person person = new Person(name, lastName);
        if(person.retrieveObject(dbManager)) {
            Angestellter angestellter = new Angestellter(person);
            angestellter.retrieveObject(dbManager);
            jtfStatus.setText("Isim: " + angestellter.getPerson().getVorname() + " Abteilung: " + angestellter.getAbteilung());
        } else {
            jtfStatus.setText("Mit diesen Angaben ist niemand gefunden.");
        }
//        try{
//            String queryString = "SELECT * FROM personen WHERE vorname = '" + name + "' AND nachname = '" + lastName + "'";
//            Statement stmt = connection.createStatement();
//            ResultSet rs = stmt.executeQuery(queryString);
//            while(rs.next()){
//                String vorname = rs.getString("vorname");
//                String nachname = rs.getString("nachname");
//                jtfStatus.setText("Welcome " + vorname + " " + nachname);
//                System.out.println("Works!");
//            }
//        }catch(SQLException sqle){
//            jtfStatus.setText(sqle.getMessage());
//            System.out.println(sqle.getMessage());
//        }
//        char[] password = jpfPassword.getPassword();
//        String dbUser = jtfDBUsername.getText();
    }

    public void exitAction() {
        this.dispose();
        System.exit(0);
        System.out.println("Exit Button clicked");
    }

    public void getDbManager() {
        char[] passArray = jpfPassword.getPassword();
        try {
            if (dbManager == null) {
                dbManager = new DbManager("localhost", "demo-user", passArray);
                jtfStatus.setText("Connection created.");
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
