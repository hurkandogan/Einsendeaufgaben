package jav12Einsendeaufgaben.frageZwei;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Anmeldung extends JFrame implements ActionListener {

    // Fields
    private JLabel jlHeadline, jlFirstname, jlLastname, jlPassword, jlDbUsername;
    private JTextField jtfStatus, jtfFirstname, jtfLastname, jtfDBUsername;
    private JPasswordField jpfPassword;
    private JButton jbLogin, jbExit;
    private String defaultDBUser = "demo-user";

    // Constructor
    public Anmeldung(String title) {
        super(title);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.createGUI();
        this.setSize(600, 400);
        this.setLocation(50, 50);
        this.setVisible(true);
    }

    public void createGUI(){
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
        JPanel jPanelHeadline = new JPanel(new GridLayout(1,1,20,20));
        jPanelHeadline.add(jlHeadline);
        this.add(BorderLayout.NORTH, jPanelHeadline);

        JPanel jPanelMain = new JPanel(new GridLayout(1,3));
        jPanelMain.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JPanel jPanelLeft = new JPanel(new GridLayout(4, 1, 5, 5));
        jPanelLeft.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        jPanelLeft.add(jlFirstname);
        jPanelLeft.add(jtfFirstname);
        jtfFirstname.setPreferredSize(new Dimension(20,10));
        jPanelLeft.add(new JLabel());
        jPanelLeft.add(new JLabel());
        jPanelMain.add(jPanelLeft);

        JPanel jPanelCenter = new JPanel(new GridLayout(4, 1, 5, 5));
        jPanelCenter.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        jPanelCenter.add(jlLastname);
        jPanelCenter.add(jtfLastname);
        jPanelCenter.add(jlDbUsername);
        jPanelCenter.add(jbLogin);
        jPanelMain.add(jPanelCenter);

        JPanel jPanelRight = new JPanel(new GridLayout(4, 1, 5, 5));
        jPanelRight.setBorder(BorderFactory.createEmptyBorder(5,20,5,5));
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
    public void actionPerformed(ActionEvent ae){
        Object eventTarget = ae.getSource();
        if(ae.getSource() == jbLogin) {
            this.loginAction();
        } else if(ae.getSource() == jbExit) {
            this.exitAction();
        }
    }

    public void loginAction(){
        String name = jtfFirstname.getText();
        String lastName = jtfLastname.getText();
        char[] password = jpfPassword.getPassword();
        String dbUser = jtfDBUsername.getText();
        jtfStatus.setText("Isim: " + name + " Soyisim: " + lastName);
    }

    public void exitAction(){
        System.out.println("Exit Button clicked");
    }

    public static void main(String[] args) {
        new Anmeldung("Anmeldung");
    }

}
