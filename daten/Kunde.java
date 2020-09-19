package jav12Einsendeaufgaben.angestellterAnmeldung.daten;

import jav12Einsendeaufgaben.angestellterAnmeldung.db.DbManager;
import jav12Einsendeaufgaben.angestellterAnmeldung.db.PersistenzInterface;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Kunde implements PersistenzInterface {

	private String message = "";
	public String getMessage() { return message; }

	/* Properties "PersistenzInterface" */
	private boolean persistent;
	public boolean isPersistent() { return persistent; 	}
	public void setPersistent(boolean newValue) { persistent = newValue; 	}

	private boolean modified;
	public void setModified(boolean newValue) { modified = newValue; 	}
	public boolean isModified() { return modified; 	}

	/* vollständige Property zwecks Debugging - set-Aufruf im jeweiligen
	 * Konstruktor, setzen nur dann, wenn die Property == null ist */
	private String pufferKey;
	public void setPufferKey(String pufferKey) {
		// absichern gegen Veränderungen des keys zur Laufzeit:
		if(this.pufferKey == null)
			this.pufferKey = pufferKey;
	}
	public String getPufferKey() {
		return pufferKey;
	}

	/* Properties Kundendaten */
	private int id;
	public int getId() { return id; }
	public void setId(int id) { this.id = id;  this.setModified(true); }

	private String firma;
	public String getFirma() { return firma; }
	public void setFirma(String firma) { this.firma = firma;  this.setModified(true); }

	/* Der Kunde referenziert ein Person-Objekt, keine ID wie in der Datenbank */
	private Person person;
	public Person getPerson() { return person; }
	public void setPerson(Person person) { this.person = person;  this.setModified(true); }

	/* Konstruktoren - der parameterlose Konstruktor ist für die Pufferverwaltung erforderlich! */
	public Kunde() {}

//	public Kunde(int id) {
//		this.id = id;
//		this.setPufferKey(new Integer(this.getPerson().getId()).toString());
//	}
	public Kunde(Person person) {
		this.person = person;
		this.setModified(false);
		this.setPersistent(false);
		this.setPufferKey(new Integer(this.getPerson().getId()).toString());
	}

	/* DB-Operationen - PersistenzInterface-Methoden */
	/**
	 * Ein Kunde kann entweder über seine ID oder über seine PersonID
	 * in der Datenbank gefunden und gelesen werden. Über die PersonID
	 * wird nur gesucht, wenn im Person-Objekt "id" noch nicht gesetzt
	 * ist.
	 * Änderung des Rückgabetyps (boolean --> PersistenzInterface) in Lektion 5
	 * @see kapitel5.db.PersistenzInterface#retrieveObject(java.sql.Connection)
	 */
	public PersistenzInterface retrieveObject(DbManager dbManager) {
		PersistenzInterface piObj = null;
		if(this.isPersistent()) {
			message = "retrieveObject: Der Kunde wurde schon aus der DB gelesen";
			return this;
		} else {
			if((piObj = dbManager.executeRetrieve(this)) == null) {
				message = "retrieveObject: Kein Kunde in der Datenbank";
				return null;
			} else {
				return piObj;
			}
		}
	}
	/**
	 * Ein neuer Kunde kann nur in die DB eingefügt werden, wenn seine personID
	 * bekannt ist. "personID" referenziert die Personendaten in "Person"
	 *
	 * @see jav12Einsendeaufgaben.angestellterAnmeldung.db.PersistenzInterface#insertObject(java.sql.Connection)
	 */
	public boolean insertObject(DbManager dbManager) {
		if(this.isPersistent()) {
			message = "insertObject: Nichts zu tun - Kunde " + this.getPerson().getVorname()
					+ " " + this.getPerson().getNachname() + " ist gespeichert";
			return false;
		} else if(this.getPerson().getId() > 0){
			/* zunächst Existenzprüfung - ebenfalls mit dem DbManager:
			 * (nun bei veränderter Methodenrückgabe) */
			if(dbManager.executeRetrieve(this) != null){
				/* es gibt diese Person bereits in der DB und ggf. auch im Puffer */
				message = "insertObject: Kunde " + this.getPerson().getVorname()
					+ " " + this.getPerson().getNachname() + " existiert bereits in DB";
				return false;
			}
			if(dbManager.executeInsert(this)) {
				message = "Kunde " + this.getPerson().getVorname()
					+ " " + this.getPerson().getNachname() + " in DB eingefügt.";
				return true;
			} else {
				message = "insertObject: Fehler beim Einfügen des Kunden " + this.getPerson().getVorname()
					+ " " + this.getPerson().getNachname();
				return false;
			}
		} else
			message = "Der Kunde \""  + this.getPerson().getVorname()
			+ " " + this.getPerson().getNachname() + "\" hat noch keine PersonID (id=" + this.getPerson().getId() + ")";
		return false;
	}
	/* Implementierung in den Wiederholungsaufgaben zu Lektion 3 */
	public boolean updateObject(DbManager dbManager) {
		System.out.println("Kunde.updateObject: Alle Änderungen sollten ab Lektion 5  "
				+ " gebündelt erledigt werden.");
		// nur persistene und veränderte Objekte müssen aktualisiert werden
		if(this.isPersistent() && this.isModified()) {
			if(this.getPerson() != null) {
				if(dbManager.executeUpdate(this)) {
					message = "Kunden-Update erfolgreich";
					return true;
				} else {
					message = "updateObject: Fehler beim Aktualisieren der Kundendaten in DB";
					return false;
				}
			} else
				message = "updateObject: Person-Referenz des Kunden ist null";
		}
		message = "updateObject: nichts zu tun - Kunde entweder transient oder unverändert";
		return false;
	}
	/* Implementierung in den Wiederholungsaufgaben zu Lektion 3 */
	public boolean deleteObject(DbManager dbManager) {
		if(this.isPersistent()) {
			if(dbManager.executeDelete(this))
				return true;
			else {
				message = "deleteObject: Fehler beim Versuch, den Kunden zu löschen";
				return false;
			}
		} else
			message = "deleteObject: Kunde ist transient, kann daher nicht in der DB gelöscht werden";
		return false;
	}

	/* SQL-String-Methode von "PersistentInterface" - ab Lektion 4 */
	public String getInsertSQL() {
		String insertString = "INSERT INTO kunden VALUES(Null, '"
			+ this.getFirma() + "', " + this.getPerson().getId() + ")";
		return insertString;
	}
	public String getRetrieveSQL() {
		String queryString = null;
		if(this.getId() > 0) { 	// noch nicht gelesen, aber schon in DB (id ist dann > 0)
			queryString = "SELECT * FROM kunden WHERE id=" + this.getId();
		} else {				// Objekt ist transient
			if(this.getPerson().getId() > 0)
				queryString = "SELECT * FROM kunden WHERE personID=" + this.getPerson().getId();
			else
				message = "Kunde.getRetrieveSQL: PersonID muss gesetzt sein (ist aber 0)";
		}
		return queryString;
	}
	public String getUpdateSQL() {
		if(this.getPerson() != null) {
			String updateString = "UPDATE kunden SET firma='" + this.getFirma()
				+ "', personID=" + this.getPerson().getId()
				+ " WHERE id=" + this.getId();
			return updateString;
		} else
			return null;
	}
	public String getDeleteSQL() {
		String updateString = "DELETE FROM kunden WHERE id=" + this.getId();
		return updateString;
	}
	public boolean loadObjProps(ResultSet rs) {
		try {
			if(rs != null && rs.next()) {
				this.setId(rs.getInt(1));
				this.setFirma(rs.getString(2));
				if(person == null)	// für temporäre Objekte
					person = new Person();
				int newID = rs.getInt(3);
				if(newID != this.getPerson().getId()) // sonst würde das eingebettete Person-Objekt als "verändert" markiert!
					this.getPerson().setId(newID);
				message = "Kundendaten aus ResultSet gelesen";
				return true;
			}
			message = (rs == null)? "ResultSet null" : "ResultSet leer";
			message += ", keine Kundendaten aus DB gelesen";
		} catch (SQLException sqle) {
			System.out.println("Kunde.loadObjProps - " + sqle.toString());
			message = "Kunde.loadObjProps - " + sqle.toString();
		}
		return false;
	}

	/* Die clone-Implementierung wird von "PersistenzInterface vorgeschrieben,
	 * aber mit hier passender Rückgabe. Dennoch überschreibt sie die Basis-
	 * implementierung in "Object". Das clonen von PiImpl-Objekten dient der
	 * Erzeugung von cache-Objekten für die ursprünglich aus der DB
	 * geladenen Objekte */
	@Override
	public PersistenzInterface clone() throws CloneNotSupportedException {
		Kunde kunde = (Kunde) super.clone();
		if(this.id == 0 || this.firma == null || this.person == null)
			return null;
		/* Feldkopien werden von "Object.clone" erledigt!
		 * Referenzen werden dabei n i c h t geclont und müssen über den Aufruf
		 * der clone-Methode für das referenzierte Objekt separat erledigt werden: */
		kunde.setPerson((Person)this.person.clone());
		kunde.setModified(false);
		return kunde;
	}

	/* ******* sonstige Hilfsmethoden ****** */
	/* "equals" wird überschrieben, um zwei Kundenobjekte auf Gleichheit
	 * zu testen (v.a. Kunde im Puffer und Kunde in der Anwendung) */
	@Override
	public boolean equals(Object object){
		if(object != null && object instanceof Kunde) {
			Kunde param = (Kunde) object;
			if(param.getId() != this.id)
				return false;
			if(!param.getFirma().equals(this.firma))
				return false;
			if(param.getPerson().getId() != this.person.getId())
				return false;
			return true;
		}
		return false;
	}
	@Override
	public String toString() {
		return "Kunde id=" + this.getId() + " " + this.getPerson() + ", " + this.getFirma();
	}
}
