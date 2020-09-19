package jav12Einsendeaufgaben.angestellterAnmeldung.db;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DbManager {

	/*  *** Properties ***  */
	private Connection connection;
	public Connection getConnection() { return connection; }

	private String message = "";
	public String getMessage() { return message; }

	/* neue Properties in Lektion 5: */
	private jav12Einsendeaufgaben.angestellterAnmeldung.db.PiPuffer piPuffer;
	private List<PersistenzInterface> failedUpdateObjects;
	public List<PersistenzInterface> getFailedUpdateObjects() { return failedUpdateObjects; }

	private boolean log = true;
	public void setLog(boolean log) { this.log = log; }
	private PrintStream logOut = System.out;
	public void setLogOut(PrintStream printStrean) { this.logOut = printStrean; }

	/*  *** Konstruktoren ***  */
	public DbManager() {}

	public DbManager(String host, String user, char[] pwCharArray)
	throws ClassNotFoundException, SQLException {
		connection = ConnectionManager.getConnection(host,
				user, new String(pwCharArray));
		piPuffer = new jav12Einsendeaufgaben.angestellterAnmeldung.db.PiPuffer();	// neu in Lektion 5
		/* Transaktionsstart (und -ende) seit Wiederholungsaufgaben Lektion 4
		 * je nach Bedarf in den die Transaktion auslösenden Methoden. */
	}

	/* *** die vier Datenbank-Operationen ***
	 * Die jeweilige DB-Operation wird vom PersistenzInterface-Objekt angestoßen. Der
	 * DbManager holt sich aus diesem piObjekt den erforderlichen SQL-String. */

	public boolean executeInsert(PersistenzInterface piObjekt) {
		try {
			String insertString = piObjekt.getInsertSQL();
			Statement stmt = connection.createStatement();
			/* "PersistenzInterface muss eine id-Property vorgeben,
			 * damit hier einheitlich die id unmittelbar zurückgeholt werden kann.
			 * TODO: bei Arbeit mit Bestellungen muss hier die  Tabelle "bestellungen"
			 * ausgenommen werden, da hier kein id-Attribut generiert, sondern
			 * ein zusammengesetzter Primärschlüssel verwendet wird!
			 * Ansonsten:
			 * unmittelbarer Abruf des per autoincrement generierten Primärschlüssels:
			 * (execute-Update-Variante) */
			if(stmt.executeUpdate(insertString, Statement.RETURN_GENERATED_KEYS) > 0){
				ResultSet rs = stmt.getGeneratedKeys();
				if(rs.next())	// hier gibts nur einen einzigen autoincrement-PK
					piObjekt.setId(rs.getInt(1));
				// das Objekt auch im Puffer sichern:
				this.objektPuffern(piObjekt);
				// Persistenzeigenschaften setzen
				piObjekt.setModified(false);
				piObjekt.setPersistent(true);
				rs.close();
			} else {
				message = "Kein Objekt vom Typ \"" + piObjekt.getClass().getName() + "\" eingefuegt!";
			}
		} catch (SQLException sqle) {
			message = "DbManager.executeInsert: Einfügen misslungen. SQLException-Message:\n\t" + sqle.getMessage();
			logOut.println(message);
			return false;
		}
		return true;
	}
	public PersistenzInterface executeRetrieve(PersistenzInterface piObjekt) {
		try {
			// umgekehrte Reihenfolge: erst Puffercheck, dann ggf. DB
			if(piObjekt.getPufferKey() != null) {// sonst wurde Implementierung vergessen
				// gibt es das Objekt bereits im Puffer?
				PersistenzInterface piObjPuffer = piPuffer.get(piObjekt);
				if(piObjPuffer != null) {
					if(log) {
						logOut.println("PiPuffer returns Puffer-Objekt(=piObject? "
							+ piObjekt.equals(piObjPuffer)
							+ "): " + piObjPuffer.toString()
							+ ", persistent=" + piObjPuffer.isPersistent()
							+ ", modified=" + piObjPuffer.isModified()
							+ ", key=" + piObjPuffer.getPufferKey());
					}
					// das Puffer-Objekt zurückgeben:
					return piObjPuffer;
				} else {
					if(log) {
						logOut.println("DbManager: piObjekt noch nicht im Puffer (" + piObjekt.toString() + " / key=" + piObjekt.getPufferKey() + ")");
					}
				}
			} else {
				message = "kein PufferKey verfügbar!";
				return null;
			}
			String selectString = piObjekt.getRetrieveSQL();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(selectString);
			if (piObjekt.loadObjProps(rs)){
				if(log) {
					logOut.println("DbManager: piObjekt aus Db geladen (" + selectString + ")"); //+ piObjekt.toString() + ")");
				}
				piObjekt.setPersistent(true);
				piObjekt.setModified(false);
				this.objektPuffern(piObjekt);
				rs.close();	// dann erst wird autocommit (falls eingestellt) ausgeführt
				return piObjekt;
			} else {
				message = piObjekt.getMessage();
				if(log) {
					logOut.println("DbManager: " + message);
				}
				rs.close();	// dann erst wird autocommit (falls eingestellt) ausgeführt
				return null;
			}
		} catch (SQLException sqle) {
			message = "Lesen misslungen: " + sqle.getMessage();
			logOut.println("DbManager.executeSelect - " + sqle.toString());
			return null;
		}
	}

	/**
	 * @param piObjekt
	 */
	private void objektPuffern(PersistenzInterface piObjekt) {
		// erst das Originalobjekt in den Puffer (unten kommt noch das geklonte):
		piPuffer.put(piObjekt);
		// clonen herausgezogen für debugging:
		PersistenzInterface piClone = null;
		try {
			//piPuffer.putDbCache(piObjekt.clone());
			piClone = piObjekt.clone();
		} catch (CloneNotSupportedException cnse) {
			logOut.println("DbManager.executeSelect - " + cnse.toString());
		}
		piPuffer.putDbCache(piClone);
	}

	/* mit "executeUpdateAll" ist das einfache update für die meisten
	 * Fälle überflüsig geworden, wird aber aus systematischen Gründen beibehalten */
	public boolean executeUpdate(PersistenzInterface piObjekt) {
		String updateString = piObjekt.getUpdateSQL();
		try {
			Statement stmt = connection.createStatement();
			int rowCount = stmt.executeUpdate(updateString);
			// Kontrolle:
			if(log)
				logOut.println("DB-UPDATE für " + piObjekt.getPufferKey() + ", rowCount=" + rowCount);
			PersistenzInterface obj = piPuffer.get(piObjekt);
			PersistenzInterface objCache = piPuffer.getDbCache(piObjekt);
			if(log) {
				logOut.println("obj=" + obj + ", objCache=" + objCache);
				logOut.println("\tupdate-Objekt = cacheObjekt? "
						+ (obj.equals(objCache))
						+ "(" + obj.toString() + "/"
						+  objCache.toString() );
			}
			if (rowCount > 0){
				piObjekt.setModified(false);
				return true;
			} else {
				message = "Kein Objekt aktualisiert";
				return false;
			}

		} catch (SQLException sqle) {
			message = "Aktualisieren misslungen: " + sqle.getMessage();
			logOut.println("DbManager.executeUpdate - " + sqle.toString());
			return false;
		}
	}

	/* Da DELETE unmittelbar ausgeführt werden soll, könnte auch gleich ein updateAll
	 * aller bereits vollzogenen Datenänderungen angehängt werden, was hier am
	 * Ende der Methode codiert ist. */
	public boolean executeDelete(PersistenzInterface piObjekt) {
		String deleteString = piObjekt.getDeleteSQL();
		try {
			Statement stmt = connection.createStatement();
			if (stmt.executeUpdate(deleteString) > 0){
				logOut.println("\texecuteDelete: Objekt " + piObjekt.getPufferKey() + " gelöscht");
				piObjekt.setPersistent(false);
				// Objekt auch aus dem Puffer entfernen
				piPuffer.remove(piObjekt);
			} else {
				message = "Kein Objekt gelöscht";
			}
		} catch (SQLException sqle) {
			message = "Löschen misslungen: " + sqle.getMessage();
			logOut.println("DbManager.executeDelete - " + sqle.toString());
			return false;
		}
		// gleich "updateAll" anhängen
		if(this.executeUpdateAll())
			return true;
		else
			return false;
	}

	/* neue Methode ab Lektion 5, die alle updates für alle geänderten
	 * Objekte im Puffer ausführt */
	public boolean executeUpdateAll() {
		if(!this.startTransaction()){
			return false;
		}
		/* alle Objekte aus dem Puffer holen: */
		Iterator<PersistenzInterface> piObjekte = piPuffer.values().iterator();
		Statement stmt = null;
		if(log) {
			logOut.println("executeUpdateAll: Objekte erhalten DB-Update:");
		}
		int counter = 0;
		while(piObjekte.hasNext()) {
			PersistenzInterface piObjekt = piObjekte.next();
			/* nur veränderte Objekte updaten, außerdem Objekte
			 * ohne DbCache (INSERT oder DELETE) ausschließen  */
			if(piObjekt.isModified() && (piPuffer.getDbCache(piObjekt) != null)) {
				try {
					/* zunächst Original abrufen und mit dem cache-Objekt vergleichen: */
					String selectString = piObjekt.getRetrieveSQL();
					stmt = connection.createStatement();
					ResultSet rs = stmt.executeQuery(selectString);
					PersistenzInterface piDB;	// zur Kontrolle nachgeladenes Objekt aus der DB
					try {
						piDB = piObjekt.getClass().newInstance();	// einfaches Objekt benötigt parameterlosen Konstruktor
						piDB.loadObjProps(rs); 	// Objekt füllen
						rs.close();
					} catch (InstantiationException ie) {
						message = "DbManger.executeUpdateAll - " + piObjekt.getPufferKey()
								+ ": Das Objekt konnte nicht instanziiert werden --> rollback";
						logOut.println(message + "\n\t" + ie.toString());
						this.endTransaction(false);
						return false;
					} catch (IllegalAccessException iae) {
						message = piObjekt.getPufferKey() + ": Kein Zugriff auf das Objekt --> rollback";
						logOut.println(message + "\n\t" + iae.toString());
						this.endTransaction(false);
						return false;
					}
					/* prüfen, ob sich das originale DB-Objekt inzwischen geändert hat: */
					boolean gleich = piDB.equals(piPuffer.getDbCache(piObjekt));
					if(!gleich) {
						message = piObjekt.getPufferKey() + ": Das Objekt wurde von Dritten in der Datenbank geändert";
						if(log) {
							logOut.println(message + "\n\tObjekt=\"" + piObjekt + "\" / \"Cache-Objekt=" + piPuffer.getDbCache(piObjekt) + "\"");
						}
						/* TODO Konzeptentscheidung: Alles wieder rückgängig machen? Oder nur
						 * dies update, und das Objekt zur erneuten Bearbeitung vermerken / anbieten?
						 * Die Liste "failedUpdateObjects" könnte ein Ansatzpunkt sein, Objekte zu
						 * sammeln, deren Update fehlgeschlagen ist. Dann wird hier auf das Transaktions-
						 * ende verzichtet (die zwei folgenden Anweisungen entfernen!). */
//						this.endTransaction(false);
//						return false;

						if(failedUpdateObjects == null)
							failedUpdateObjects = new ArrayList<PersistenzInterface>();
							failedUpdateObjects.add(piObjekt);
							continue;
					} else {
						if(log) {
							logOut.println("\taktuelles DBObj=cacheDbObj? " + gleich + ", upzudatendes Objekt verändert? " + piObjekt.isModified());
						}
					}
					String updateString = piObjekt.getUpdateSQL();
					if(log) {
						logOut.println("\t" + updateString);
					}
					stmt = connection.createStatement();
					int rowCount = stmt.executeUpdate(updateString);
					if(log) {
						logOut.println("\tDB-UPDATE-Result für " + piObjekt.getPufferKey() + ": rowCount=" + rowCount + "\n");
					}
					if (rowCount > 0){
						counter++;	// Zähler für aktualisierte Objekte
						piObjekt.setModified(false);
						/* noch das DbCache-Objekt entsprechend dem DB-Update aktualisieren: */
						piPuffer.putDbCache(piObjekt);
					} else {
						message = "Das Objekt " + piObjekt.getPufferKey() + " wurde n i c h t aktualisiert";
					}
				} catch (SQLException sqle) {
					this.endTransaction(false);
					message = "DbManager - " + sqle.getMessage();
					logOut.println("DbManager.executeUpdateAll - " + sqle.toString());
					return false;
				}
			}
		}
		if(log) {
			logOut.println("\texecuteUpdateAll: " + counter + " Objekte in DB aktualisiert\n");
		}

		if(!this.endTransaction(true)){
			return false;
		}else{
			if(failedUpdateObjects != null && failedUpdateObjects.size() > 0){
				logOut.println("Folgende Objekte konnten nicht upgedatet werden: ");
				Iterator<PersistenzInterface> iterator = failedUpdateObjects.iterator();
				while(iterator.hasNext()){
					logOut.println("\t" + iterator.next().getPufferKey());
				}
			}
			return true;
		}
	}

	/* ******** Transaktionsmethoden ********
	 * "startTransaction" und "endTransaction" ab Wiederholungsaufgabe 4.2
	 * implementiert, "commit" ist damit weggefallen. */
	public boolean startTransaction() {
		try {
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(
					Connection.TRANSACTION_SERIALIZABLE);
			return true;
		} catch (SQLException sqle) {
			message = sqle.toString();
			logOut.println("DbManager.startTransaction - " + sqle.toString());
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
			logOut.println("DbManager.endTransaction - " + sqle.toString());
			return false;
		}
	}
	/* Dient lediglich dem Aufräumen beim Verlassen des Ladens, weil der
	 * Connectionmanager nicht als eigenständige Anwendung läuft.
	 * Aufruf in "Eingang.ausgangActionPerformed". */
	public void closeConnection() {
		ConnectionManager.closeConnection();
	}

	/* TODO: neu ab Lektion 5 / Wiederholungsaufgabe 5. - ersetzt die Methode
	 * "closeConnection" aus Lektion 4*/
	public void cleanup() {
		// Implementierung der Aufgabe unter Nutzung von listPufferElemente
		ConnectionManager.releaseConnection(connection);
		int size = piPuffer.size();
		this.listPufferElemente("v o r DbManager.cleanUp(): ");
		piPuffer.clear();
		this.listPufferElemente("n a c h DbManager.cleanup: ");
		System.out.println("\t " + size + " Elemente aus dem PiPuffer entfernt.");
		ConnectionManager.closeConnection();
	}

	/* Hilfsmethode für "cleanup" */
	private void listPufferElemente(String message) {
		if(piPuffer.isEmpty()){
			System.out.println(message + " Der PiPuffer enthält keine Elemente");
		}else{
			System.out.println(message + " Auflistung der PiPuffer-Elemente");
			Iterator<PersistenzInterface> iterator = piPuffer.values().iterator();
			while(iterator.hasNext()){
				PersistenzInterface piObjekt = iterator.next();
				System.out.println("\t " + piObjekt.toString());
			}
		}
	}
}
