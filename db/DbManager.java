package jav12Einsendeaufgaben.angestellterAnmeldung.db;

import jav12Einsendeaufgaben.angestellterAnmeldung.daten.Abteilung;
import jav12Einsendeaufgaben.angestellterAnmeldung.daten.Angestellter;
import jav12Einsendeaufgaben.angestellterAnmeldung.daten.Person;

import java.io.PrintStream;
import java.sql.*;
import java.util.List;

public class DbManager {

	/*  ******** Properties ********  */
	private Connection connection;
	public Connection getConnection() { return connection; }

	private String message = "";
	public String getMessage() { return message; }

	private List<PersistenzInterface> failedUpdateObjects;
	public List<PersistenzInterface> getFailedUpdateObjects() { return failedUpdateObjects; }

	private boolean log = true;
	public void setLog(boolean log) { this.log = log; }
	private PrintStream logOut = System.out;
	public void setLogOut(PrintStream printStrean) { this.logOut = printStrean; }

	/*  ******** Konstruktoren   ******** */
	public DbManager() {}

	public DbManager(String host, String user, char[] pwCharArray)
	throws ClassNotFoundException, SQLException {
		connection = ConnectionManager.getConnection(host,
				user, new String(pwCharArray));
	}

	/* ******* CRUD-Datenbank-Operationen *******
	 * Die jeweilige DB-Operation wird von einem PI-Objekt angestoßen. Der DbManager holt
	 * sich aus diesem piObjekt per Callback den erforderlichen SQL-String. */

	public boolean executeRetrieve(PersistenzInterface piObjekt) {
		try {
			String selectString = piObjekt.getRetrieveSQL();
			Statement stmt = connection.createStatement();
			// ausführen der Anweisung
			if (piObjekt.loadObjProps(stmt.executeQuery(selectString))){
				piObjekt.setPersistent(true);
				piObjekt.setModified(false);
				return true;
			} else {
				message = "Kein Objekt in der Datenbank gefunden";
				return false;
			}
		} catch (SQLException sqle) {
			message = "Lesen misslungen: " + sqle.getMessage();
			logOut.println("DbManager.executeRetrieve - " + sqle.toString());
			return false;
		}
	}

	public boolean executeUpdate(PersistenzInterface piObjekt) {
		String updateString = piObjekt.getUpdateSQL();
		try {
			Statement stmt = connection.createStatement();
			if (stmt.executeUpdate(updateString) > 0){
				piObjekt.setModified(false);
				return true;
			} else {
				message = "Kein Objekt in der Datenbank aktualisiert";
				return false;
			}
		} catch (SQLException sqle) {
			message = "Aktualisieren misslungen: " + sqle.getMessage();
			logOut.println("DbManager.executeUpdate - " + sqle.toString());
			return false;
		}
	}

	public boolean executeDelete(PersistenzInterface piObjekt) {
		String deleteString = piObjekt.getDeleteSQL();
		try {
			connection.setAutoCommit(true);
			Statement stmt = connection.createStatement();
			if (stmt.executeUpdate(deleteString) > 0){
				if(log)
					logOut.println("\texecuteDelete: Objekt " + piObjekt.toString() + " gelöscht");
				piObjekt.setPersistent(false);
				return true;
			} else {
				message += "\nKein Objekt in der DB gelöscht (Typ=" + piObjekt.getClass().getName() + ")";
				return false;
			}
		} catch (SQLException sqle) {
			message += "\nLöschen misslungen: " + sqle.getMessage();
			logOut.println("DbManager.executeDelete - " + sqle.toString());
			return false;
		}
	}

	public boolean executeInsert(PersistenzInterface piObjekt) {
		try {
			connection.setAutoCommit(true);
			String insertString = piObjekt.getInsertSQL();
			Statement stmt = connection.createStatement();
			if (stmt.executeUpdate(insertString) > 0){
				piObjekt.setModified(false);
				piObjekt.setPersistent(true);
				return true;
			} else {
				message = "Kein Objekt in DB eingefuegt (Typ: " + piObjekt.getClass().getName() + ")";
				return false;
			}
		} catch (SQLException sqle) {
			message = "DbManager.executeInsert: Einfügen misslungen. SQLException-Message:\n\t" + sqle.getMessage();
			logOut.println(message);
			return false;
		}
	}
	/* Die Methode wird in Lektion 6 zur Ausführung von Bestellungen ergänzt.
	 * Wesentlich ist hier, dass die Verfügbarkeit geprüft wird und die Bestellung
	 * eines Artikels, der nicht mehr verfügbar ist (Bestellung eines Dritten
	 * während der Erstellung dieser Bestellung) nicht ausgeführt wird. */
//	public boolean executeBestellung(Bestellung bestellung,
//			List<Bestellposition> bestellListe) {
//		boolean returnValue = true;
//		String insertString = "INSERT INTO bestellpositionen VALUES("
//			+ bestellung.getId() + ", ?, ?, NULL);"; 	// Parameter: artikelID, menge
//		String updateString = "UPDATE artikel SET menge=? WHERE id=?;";	// verfügbare Menge herabsetzen
//		String selectString = "SELECT menge FROM artikel WHERE id=?";
//		ResultSet rsArtikel = null;
//		StringBuffer sb = new StringBuffer();;
//		this.startTransaction();	// Ausführung der Bestellung als Transaktion
//		try {
//			PreparedStatement prepStmt = null;
//			for(int i=0; i<bestellListe.size(); i++){
//				prepStmt = this.getConnection().prepareStatement(selectString);
//				Artikel artikel = bestellListe.get(i).getArtikel();
//				// 1. aktuelle Artikelmenge abrufen
//				prepStmt.setInt(1, artikel.getId());
//				rsArtikel = prepStmt.executeQuery();
//				if(rsArtikel.next()) {
//					int mengeInDb = rsArtikel.getInt(1);
//					int mengeBest = bestellListe.get(i).getMenge();
//					if(mengeInDb >= mengeBest) {	 // Lagermenge reicht aus
//						if(mengeBest > 0) {
//							// 2. Bestellposition speichern
//							prepStmt = this.getConnection().prepareStatement(insertString);
//							prepStmt.setInt(1, artikel.getId());
//							prepStmt.setInt(2, mengeBest);
//							prepStmt.executeUpdate();
//							// 3. Artikel-Tabelle - menge herabsetzen
//							prepStmt = this.getConnection().prepareStatement(updateString);
//							prepStmt.setInt(1, mengeInDb - mengeBest);
//							prepStmt.setInt(2, artikel.getId());
//							prepStmt.executeUpdate();
//							if(log)
//								logOut.println("bestellt: " + bestellListe.get(i).toString());
//						}
//					} else {
//						/* Wir belassen es hier bei einer einfachen Mitteilung,
//						 * die zu allen Artikeln, deren Bestellung nicht ausgeführt werden
//						 * konnte, dem StringBuffer-Objekt angehängt wird.
//						 * TODO: Ausbau der Anwendung, z.B. in der Form, dass die nicht
//						 * bestellbaren Artikel mit aktualisiertem Lagerbestand erneut angeboten werden. */
//						sb.append("Artikel \"");
//						sb.append(artikel.getName());
//						sb.append("\" (");
//						sb.append(artikel.getId());
//						sb.append("): Die bestellte Menge=");
//						sb.append(mengeBest);
//						sb.append(" übersteigt die noch verfügbare Menge=");
//						sb.append(mengeInDb);
//						sb.append("\nDie Bestellung dieses Artikels wurde nicht ausgeführt\n\n.");
//						returnValue = false;
//					}
//				}
//			}
//			rsArtikel.close();
//			message = sb.toString();
//			this.endTransaction(true);	// Transaktionsende mit "commit"
//		} catch (SQLException sqle) {
//			this.endTransaction(true);	// Transaktionsende mit "rollback"
//			sb.append("DbManager.executeBestellung\n\t");
//			sb.append(sqle.toString());
//			sb.append("\n");
//			message = sb.toString();
//			logOut.println(message);
//			returnValue = false;
//		}
//		return returnValue;
//	}

	/* *** Transaktionsmethoden *** */
	public boolean startTransaction() {
		try {
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(
					Connection.TRANSACTION_SERIALIZABLE);
			return true;
		} catch (SQLException sqle) {
			message = sqle.toString();
			logOut.println("DbManager.startTransaction - "
					+ sqle.toString());
			return false;
		}
	}
	public boolean endTransaction(boolean commit) {
		try {
			if(commit){
				connection.commit();
			} else {
				connection.rollback();
			}
			// autocommit zurücksetzen
			connection.setAutoCommit(true);
			// in beiden Fällen, wenn erfolgreich:
			return true;
		} catch (SQLException sqle) {
			message = sqle.toString();
			logOut.println("DbManager.endTransaction - "
					+ sqle.toString());
			return false;
		}
	}
	public void cleanup() { 	// neu ab Lektion 5, Wiederholungsaufgaben */
		ConnectionManager.releaseConnection(connection);
		ConnectionManager.closeConnection();
	}
}
