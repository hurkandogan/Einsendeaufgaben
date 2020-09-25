package jav12Einsendeaufgaben.angestellterAnmeldung.daten;

import jav12Einsendeaufgaben.angestellterAnmeldung.db.DbManager;
import jav12Einsendeaufgaben.angestellterAnmeldung.db.PersistenzInterface;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Person implements PersistenzInterface {

	// Message fields
	private String message = "";
	public String getMessage() { return message; }

	/* Properties "PersistenzInterface" */
	private boolean persistent;
	public boolean isPersistent() { return persistent; 	}
	public void setPersistent(boolean newValue) { persistent = newValue; }

	private boolean modified;
	public boolean isModified() { return modified; }
	public void setModified(boolean newValue) { modified = newValue; }

	private String pufferKey;
	public String getPufferKey(){ return pufferKey; }
	public void setPufferKey(String pufferKey){
		if(this.pufferKey == null){
			this.pufferKey = pufferKey;
		}
	}

	/* Properties Person Data */
	private int id;
	public int getID() { return id; }
	public void setID(int id) { this.id = id;  this.setModified(true); }

	private String vorname;
	public String getVorname() { return vorname; }
	public void setVorname(String vorname) {
		this.vorname = vorname;
		this.setModified(true);
	}
	private String nachname;
	public String getNachname() { return nachname; }
	public void setNachname(String nachname) {
		this.nachname = nachname;
		this.setModified(true);
	}

	private String strasse;
	public String getStrasse() { return strasse; }
	public void setStrasse(String strasse) { this.strasse = strasse;  this.setModified(true); }

	private String hausnummer;
	public String getHausnummer() { return hausnummer; }
	public void setHausnummer(String hausnummer) { this.hausnummer = hausnummer;  this.setModified(true); }

	private String ort;
	public String getOrt() { return ort; }
	public void setOrt(String ort) { this.ort = ort; this.setModified(true); }

	private String land;
	public String getLand() { return land; }
	public void setLand(String land) { this.land = land;  this.setModified(true); }

	private String plz;
	public String getPlz() { return plz; }
	public void setPlz(String plz) { this.plz = plz;  this.setModified(true); }

	private String telefon;
	public String getTelefon() { return telefon; }
	public void setTelefon(String telefon) { this.telefon = telefon;  this.setModified(true); }

	private String landSQL;
	private String telefonSQL;

	/* Konstruktoren */

	public Person() {}
	public Person(int id) {
		this.id = id;
		this.setPufferKey(new Integer(id).toString());
	}

	public Person(String vorname, String nachname) {
		this.vorname = vorname;
		this.nachname = nachname;
		this.setModified(false);
		this.setPersistent(false);
		this.setPufferKey(vorname + " " + nachname);
	}
	public Person(String vorname, String nachname,
			String strasse, String hausnummer, String ort,
			String land, String plz, String telefon) {
		this.setVorname(vorname);
		this.setNachname(nachname);
		this.setStrasse(strasse);
		this.setHausnummer(hausnummer);
		this.setOrt(ort);
		this.setLand(land);
		this.setPlz(plz);
		this.setTelefon(telefon);
		this.setModified(false);
		this.setPersistent(false);
		this.setPufferKey(vorname + " " + nachname);
	}

	/* DB-Operationen - PersistenzInterface-Methoden */
	/**
	 * Eine Person kann entweder über Name und Vorname oder über die ID in der
	 * Datenbank gefunden und sodann gelesen werden.
	 *
	 * @see jav12Einsendeaufgaben.angestellterAnmeldung.db.PersistenzInterface::retrieveObject(java.sql.Connection)
	 */
	public PersistenzInterface retrieveObject(DbManager dbManager) {
		PersistenzInterface piObj = null;
		if(this.isPersistent()) {
			message = "Person#retrieveObject: Die Person wurde schon aus der DB gelesen";
			return this;
		} else {
			if((piObj = dbManager.executeRetrieve(this)) == null) { // alternative Suche nach id oder Name in
				message = "Person#retrieveObject: Person nicht gefunden";
				return null;
			} else {
				return piObj;
			}
		}
	}

	public boolean insertObject(DbManager dbManager) {
		if(this.isPersistent()) {
			message = "Person#insertObject: Nichts zu tun - Person ist gespeichert (persistent)";
			return false;
		} else if(id > 0){
			/* zunächst Existenzprüfung - ebenfalls mit dem DbManager: */
			if(dbManager.executeRetrieve(this) == null){
				/* es gibt diese Person bereits in der DB */
				message = "Person#insertObject: Person existiert bereits in DB";
				return false;
			}
		}
		if(dbManager.executeInsert(this))
			return true;
		else {
			message = "Person#insertObject: Fehler beim Einfügen der Person (id=" + id + ")";
			return false;
		}
	}

	public boolean updateObject(DbManager dbManager) {
		/* nur persistente und veränderte Objekte müssen aktualisiert werden */
		if(this.isPersistent() && this.isModified()) {
				if(dbManager.executeUpdate(this)) {
					message = "Person#updateObject: Personen-Update erfolgreich";
					return true;
				} else {
					message = "Person#updateObject: Fehler beim Aktualisieren der Personendaten in DB";
					System.out.println(message);
					return false;
				}
			}
		message = "Person#updateObject: nichts zu tun - Person entweder transient oder unverändert";
		return false;
	}

	public boolean deleteObject(DbManager dbManager) {
		if(this.isPersistent()) {
			if(dbManager.executeDelete(this)) {
				return true;
			} else {
				message = "Person#deleteObject: Fehler beim Versuch, die Person zu löschen";
				return false;
			}
		} else {
			message = "Person#deleteObject: Person ist transient, kann daher nicht in der DB gelöscht werden";
		}
		return false;
	}

	/* SQL-String-Methoden von "PersistenzInterface" - ab Lektion 4 */
	public String getInsertSQL() {
		this.prepareOptionalAttributes();
		String insertString = "INSERT INTO personen VALUES(NULL, '"
			+ this.getVorname() + "', '" + this.getNachname() + "', '"
			+ this.getStrasse() + "', '" + this.getHausnummer() + "', '"
			+ this.getOrt() + "', " + landSQL + ", '"
			+ this.getPlz() + "', " + telefonSQL + ")";
		return insertString;
	}

	public String getRetrieveSQL() {
		String queryString = null;
		if(this.getID() > 0) { 	// noch nicht gelesen, aber schon in DB (id ist dann > 0)
			queryString = "SELECT * FROM personen WHERE id=" + this.getID();
		} else if(this.checkField(vorname) && this.checkField(nachname)) {
			queryString = "SELECT * FROM personen WHERE vorname='" + this.getVorname();
			queryString += "' AND nachname='" + this.getNachname() + "'";
		} else {
			message = "Person#getRetrieveSQL: Vor- und Nachname müssen gesetzt sein";
		}
		return queryString;
	}

	public String getUpdateSQL() {
		// Auch Namensänderungen werden zugelassen!
		this.prepareOptionalAttributes();
		String updateString = "UPDATE personen SET vorname='" + this.getVorname()
			+ "', nachname='" + this.getNachname()
			+ "', strasse='" + this.getStrasse()
			+ "', hausnummer='" + this.getHausnummer()
			+ "', ort='" + this.getOrt()
			+ "', land=" + landSQL
			+ ", plz='" + this.getPlz()
			+ "', telefon=" + telefonSQL
			+ " WHERE id=" + this.getID();
		return updateString;
	}
	public String getDeleteSQL() {
		String updateString = "DELETE FROM personen WHERE id=" + this.getID();
		return updateString;
	}

	private void prepareOptionalAttributes() {
		landSQL = (this.getLand() == null || this.getLand().equals("")) ? "NULL" : "'" + this.getLand().substring(0, 1).trim() + "'";
		telefonSQL = (this.getTelefon() == null || this.getTelefon().equals("")) ? "NULL" : "'" + this.getTelefon() + "'";
	}

	public boolean loadObjProps(ResultSet rs) {
		try {
			if(rs != null && rs.next()) {
				this.setID(rs.getInt(1));
				this.setVorname(rs.getString(2));
				this.setNachname(rs.getString(3));
				this.setStrasse(rs.getString(4));
				this.setHausnummer(rs.getString(5));
				this.setOrt(rs.getString(6));
				this.setLand(rs.getString(7));
				this.setPlz(rs.getString(8));
				this.setTelefon(rs.getString(9));
				message = "Person#loadObjProps: Personendaten aus ResultSet gelesen";
				return true;
			}
		} catch (SQLException sqle) {
			System.out.println("Person.ladeObjekt - " + sqle.toString());
		}
		return false;
	}

	@Override
	public PersistenzInterface clone() throws CloneNotSupportedException {
		Person person = (Person) super.clone();
		if(this.id == 0 || this.vorname == null || this.nachname == null
		|| strasse == null || hausnummer == null || ort == null || plz == null){
			return null;
		}
		person.setModified(false);
		return person;
	}

	/* Hilfsmethoden */
	public boolean checkField(String name) {
		return ((name != null) && (!name.equals("")));
	}
	/* "equals" wird überschrieben, um zwei Personenbjeke auf Gleichheit
	 * zu testen (v.a. Person im Puffer und Person in der Anwendung) */
	@Override
	public boolean equals(Object object){
		if(object != null && object instanceof Person) {
			Person param = (Person) object;
			if(param.getID() != this.id) { return false; }
			if(!param.getVorname().equals(this.vorname)) { return false; }
			if(!param.getNachname().equals(this.nachname)) { return false; }
			if(!param.getStrasse().equals(this.strasse)) { return false; }
			if(!param.getHausnummer().equals(this.hausnummer)) { return false; }
 			if(!param.getOrt().equals(this.ort)) { return false; }
			// optionaler Parameter "land":
			if(param.getLand() == null) { if(this.land != null) { return false; } }
			else if(!param.getLand().equals(this.land)) { return false; }
			if(!param.getPlz().equals(this.plz)) { return false; }
			// optionaler Parameter "telefon"
			if(param.getTelefon() == null) { if(this.telefon != null) { return false; }}
			else if(!param.getTelefon().equals(this.telefon)) { return false; }
			return true;
		}
		return false;
	}
	@Override
	public String toString(){
		return "Person id=" + this.getID() + " " + this.getVorname() + " " + this.getNachname();
	}
}
