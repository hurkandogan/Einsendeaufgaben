package jav12Einsendeaufgaben.angestellterAnmeldung.db;

import jav12Einsendeaufgaben.angestellterAnmeldung.daten.Abteilung;
import jav12Einsendeaufgaben.angestellterAnmeldung.daten.Angestellter;
import jav12Einsendeaufgaben.angestellterAnmeldung.daten.Person;

import javax.sound.midi.SysexMessage;
import java.io.PrintStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DbManager {

	/*  ******** Properties ********  */
	private Connection connection;
	public Connection getConnection() { return connection; }

	private String message = "";
	public String getMessage() { return message; }

	private PiPuffer piPuffer;

	private List<PersistenzInterface> failedUpdateObjects;
	public List<PersistenzInterface> getFailedUpdateObjects() { return failedUpdateObjects; }

	private boolean log = true;
	public void setLog(boolean log) { this.log = log; }

	/*  ******** Konstruktoren   ******** */
	public DbManager() {}

	public DbManager(String host, String user, char[] pwCharArray) throws ClassNotFoundException, SQLException {
		connection = ConnectionManager.getConnection(host, user, new String(pwCharArray));
	}

	/* ******* CRUD-Datenbank-Operationen *******
	 * Die jeweilige DB-Operation wird von einem PI-Objekt angestoßen. Der DbManager holt
	 * sich aus diesem piObjekt per Callback den erforderlichen SQL-String. */

	public boolean executeInsert(PersistenzInterface piObjekt) {
		try {
			String insertString = piObjekt.getInsertSQL();
			Statement stmt = connection.createStatement();

			if (stmt.executeUpdate(insertString, Statement.RETURN_GENERATED_KEYS) > 0){
				ResultSet rs = stmt.getGeneratedKeys();
				if(rs.next()){
					piObjekt.setID(rs.getInt(1));
				}
				this.objektPuffern(piObjekt);
				piObjekt.setModified(false);
				piObjekt.setPersistent(true);
				rs.close();
			} else {
				message = "Kein Objekt in DB eingefuegt (Typ: " + piObjekt.getClass().getName() + ")";
				System.out.println(message);
			}
		} catch (SQLException sqle) {
			message = "DbManager.executeInsert: Einfügen misslungen. SQLException-Message:\n\t" + sqle.getMessage();
			System.out.println(message);
			return false;
		}
		return true;
	}

	public PersistenzInterface executeRetrieve(PersistenzInterface piObjekt) {
		try {
			if (piObjekt.getPufferKey() != null) { // Checking Puffer if there is an Object
				PersistenzInterface piObjPuffer = piPuffer.get(piObjekt);
				if(piObjPuffer != null){
					if(log){
						System.out.println("PiPuffer returns Puffer-Object (=piObject? " +
								piObjekt.equals(piObjPuffer) + "): " +
								piObjPuffer.toString() + ", persistent: " +
								piObjPuffer.isPersistent() + ", modified: " +
								piObjPuffer.isModified() + ", key: " +
								piObjPuffer.getPufferKey());
					}
					return piObjPuffer;
				} else {
					if (log) {
						System.out.println("DbManager: piObjekt is not yet in Puffer (" + piObjekt.toString() + " / key: " + piObjekt.getPufferKey());
					}
				}
			} else {
				message = "No PufferKey available.";
				System.out.println(message);
				return null;
			}
			String selectString = piObjekt.getRetrieveSQL();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(selectString);
			if (piObjekt.loadObjProps(rs)){
				piObjekt.setPersistent(true);
				piObjekt.setModified(false);
				this.objektPuffern(piObjekt);
				rs.close();
				return piObjekt;
			} else {
				message = piObjekt.getMessage();
				System.out.println(message);
				return null;
			}
		} catch (SQLException sqle) {
			message = "Lesen misslungen: " + sqle.getMessage();
			System.out.println("DbManager.executeRetrieve - " + sqle.toString());
			return null;
		}
	}

	public boolean executeUpdate(PersistenzInterface piObjekt) {
		String updateString = piObjekt.getUpdateSQL();
		try {
			Statement stmt = connection.createStatement();
			int rowCount = stmt.executeUpdate(updateString);
			if(log){
				System.out.println("DB-Update for " + piObjekt.getPufferKey() + ", Row Count: " + rowCount);
			}
			PersistenzInterface obj = piPuffer.get(piObjekt);
			PersistenzInterface objCache = piPuffer.getDbCache(piObjekt);
			if(log) {
				System.out.println("Object: " + obj + ", objCache: " + objCache);
				System.out.println("Update Object = cacheObject? " + (obj.equals(objCache)) + " / " + obj.toString() + " / " + objCache.toString());
			}
			if (rowCount > 0) {
				piObjekt.setModified(false);
				return true;
			} else {
				message = "No Object updated.";
				return false;
			}
		} catch (SQLException sqle) {
			message = "Aktualisieren misslungen: " + sqle.getMessage();
			System.out.println("DbManager.executeUpdate - " + sqle.toString());
			return false;
		}
	}

	public boolean executeDelete(PersistenzInterface piObjekt) {
		String deleteString = piObjekt.getDeleteSQL();
		try {
			Statement stmt = connection.createStatement();
			if (stmt.executeUpdate(deleteString) > 0){
				System.out.println("\texecuteDelete: Objekt " + piObjekt.toString() + " gelöscht");
				piObjekt.setPersistent(false);
				piPuffer.remove(piObjekt);
				return true;
			} else {
				message += "\nKein Objekt in der DB gelöscht (Typ=" + piObjekt.getClass().getName() + ")";
				System.out.println(message);
				return false;
			}
		} catch (SQLException sqle){
			message += "\nLöschen misslungen: " + sqle.getMessage();
			System.out.println("DbManager.executeDelete - " + sqle.toString());
			return false;
		}
	}

	public boolean executeUpdateAll(){
		if(!this.startTransaction()){
			return false;
		}
		Iterator<PersistenzInterface> piObjekte = piPuffer.values().iterator();
		Statement stmt = null;
		if(log){
			System.out.println("DbManager#executeUpdateAll: Ojekte erhalten DB-Update");
		}
		int counter = 0;
		while (piObjekte.hasNext()){
			PersistenzInterface piObjekt = piObjekte.next();
			if(piObjekt.isModified() && (piPuffer.getDbCache(piObjekt)) != null){
				try{
					String selectString = piObjekt.getRetrieveSQL();
					stmt = connection.createStatement();
					ResultSet rs = stmt.executeQuery(selectString);
					PersistenzInterface piDB = null;
					try{
						piDB = piObjekt.getClass().newInstance();
						piDB.loadObjProps(rs);
						rs.close();
					}catch(InstantiationException ie){
						message = "DbManager#executeUpdateAll: " + piObjekt.getPufferKey() + ": Das Objekt konnte nicht instanziiert werden.";
						System.out.println(message + " " + ie.toString());
						this.endTransaction(false);
					}catch(IllegalAccessException iae){
						message = "DbManager#executeUpdateAll: " + piObjekt.getPufferKey() + ": Kein Zuggriff auf das Objekt.";
						System.out.println(message + " " + iae.toString());
						this.endTransaction(false);
					}
					if(piDB != null) {
						boolean gleich = piDB.equals(piPuffer.getDbCache(piObjekt));
						if (!gleich) {
							message = piObjekt.getPufferKey() + ": Das Objekt wurde von Dritten in der Datenbank geändert.";
							if (log) {
								System.out.println(message + " Objekt: " + piObjekt + "/ Cache Objekt: " + piPuffer.getDbCache(piObjekt));
							}
							if (failedUpdateObjects == null) {
								failedUpdateObjects = new ArrayList<PersistenzInterface>();
							}
							failedUpdateObjects.add(piObjekt);
							continue;
						} else {
							if (log) {
								System.out.println("Aktuelles DBobj = cacheObj? " + gleich + ", upzudatendes Objekt verändert? " + piObjekt.isModified());
							}
						}
					}
					String updateString = piObjekt.getUpdateSQL();
					if(log){
						System.out.println(updateString);
					}
					stmt = connection.createStatement();
					int rowCount = stmt.executeUpdate(updateString);
					if(log){
						System.out.println("DB-Update result for " + piObjekt.getPufferKey() + ": rowCount = " + rowCount);
					}
					if(rowCount > 0){
						counter++;
						piObjekt.setModified(false);
						piPuffer.putDbCache(piObjekt);
					}else{
						message = "Das Objekt " + piObjekt.getPufferKey() + " wurde nicht aktualisiert.";
					}
				}catch(SQLException sqle){
					this.endTransaction(false);
					message = "DbManager#executeUpdateAll: " + sqle.getMessage();
					System.out.println(message);
					return false;
				}
			}
		}
		if(log){
			System.out.println("DbManager#executeUpdateAll: " + counter + " Objekte in DB aktualisiert.");
		}
		if(!this.endTransaction(true)){
			return false;
		} else {
			if(failedUpdateObjects != null && failedUpdateObjects.size() > 0) {
				System.out.println("Folgende Objekte konnten nicht upgedatet werden.");
				Iterator<PersistenzInterface> iterator = failedUpdateObjects.iterator();
				while(iterator.hasNext()){
					System.out.println("\t" + iterator.next().getPufferKey());
				}
			}
			return true;
		}
	}

	/**
	 * @param piObject
	 * @return void
	 */

	public void objektPuffern(PersistenzInterface piObject) {
		piPuffer.put(piObject);
		PersistenzInterface piClone = null;
		try {
			piClone = piObject.clone();
		} catch(CloneNotSupportedException cnse) {
			System.out.println("DbManager#ObjektPuffern: " + cnse.getMessage());
		}
		piPuffer.putDbCache(piClone);
	}

	/* *** Transaktionsmethoden *** */
	public boolean startTransaction() {
		try {
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			return true;
		} catch (SQLException sqle) {
			message = sqle.toString();
			System.out.println("DbManager#startTransaction: " + sqle.toString());
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
			System.out.println("DbManager.endTransaction - " + sqle.toString());
			return false;
		}
	}

	public void closeConnection(){
		ConnectionManager.closeConnection();
	}

	public void cleanup() { 	// neu ab Lektion 5, Wiederholungsaufgaben */
		ConnectionManager.releaseConnection(connection);
		int size = piPuffer.size();
		this.listPufferElemente("DbManager#cleanup: vorher:");
		piPuffer.clear();
		this.listPufferElemente("DbManager#cleanup: nachher:");
		System.out.println("Elements are deleted from PiPuffer");
		ConnectionManager.closeConnection();
	}

	public void listPufferElemente(String message){
		if(piPuffer.isEmpty()){
			System.out.println("PiPuffer is alread empty!");
		}else{
			System.out.println(message + " List of PiPuffer Elements: ");
			Iterator<PersistenzInterface> iterator = piPuffer.values().iterator();
			while(iterator.hasNext()){
				PersistenzInterface piObjekt = iterator.next();
				System.out.println("\n" + piObjekt.toString());
			}
		}
	}
}
