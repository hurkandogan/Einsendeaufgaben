package jav12Einsendeaufgaben.angestellterAnmeldung.daten;

import jav12Einsendeaufgaben.angestellterAnmeldung.db.DbManager;
import jav12Einsendeaufgaben.angestellterAnmeldung.db.PersistenzInterface;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Person implements PersistenzInterface {

	private String message = "";
	public String getMessage() { return message; }

	/* Properties "PersistenzInterface" */
	private boolean persistent;
	public boolean isPersistent() { return persistent; 	}
	public void setPersistent(boolean newValue) { persistent = newValue; }

	private boolean modified;
	public void setModified(boolean newValue) { modified = newValue; 	}

	public boolean isModified() { return modified; 	}

	/* Properties Personendaten */
	private int id;
	public int getId() { return id; }
	public void setId(int id) { this.id = id;  this.setModified(true); }

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

	/* Konstruktoren */

	public Person() {}
	public Person(int id) {
		this.id = id;
	}

	public Person(String vorname, String nachname) {
		this.vorname = vorname;
		this.nachname = nachname;
		this.setModified(false);
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
	}

	/* DB-Operationen - PersistenzInterface-Methoden */
	/**
	 * Eine Person kann entweder über Name und Vorname oder über die ID in der
	 * Datenbank gefunden und sodann gelesen werden.
	 *
	 * @see lernhefte.jav11.lektion5.db.PersistenzInterface#retrieveObject(java.sql.Connection)
	 */
	public boolean retrieveObject(DbManager dbManager) {
		if(this.isPersistent()) {
			message = "ausDbLesen: Die Person wurde schon aus der DB gelesen";
			return true;
		} else {
			if(!dbManager.executeRetrieve(this)) { // alternative Suche nach id oder Name in
				message = "ausDbLesen: Person nicht gefunden";
				return false;
			} else {
				return true;
			}
		}
	}

	public boolean insertObject(DbManager dbManager) {
		if(this.isPersistent()) {
			message = "insertObject: Nichts zu tun - Person ist gespeichert (persistent)";
			return false;
		} else if(id > 0){
			/* zunächst Existenzprüfung - ebenfalls mit dem DbManager: */
			if(dbManager.executeRetrieve(this)){
				/* es gibt diese Person bereits in der DB */
				message = "insertObject: Person existiert bereits in DB";
				return false;
			}
		}
		if(dbManager.executeInsert(this))
			return true;
		else {
			message = "insertObject: Fehler beim Einfügen der Person (id=" + id + ")";
			return false;
		}
	}
	public boolean updateObject(DbManager dbManager) {
		/* nur persistente und veränderte Objekte müssen aktualisiert werden */
		if(this.isPersistent() && this.isModified()) {
			if(this.checkField(vorname) && this.checkField(nachname)) {
				if(dbManager.executeUpdate(this)) {
					message = "Personen-Update erfolgreich";
					return true;
				} else {
					message = "Person.inDbAendern: Fehler beim Aktualisieren der Personendaten in DB";
					System.out.println(message);
					return false;
				}
			} else
				message = "Vor- und Nachname müssen gesetzt sein!";
		}
		message = "inDbAendern: nichts zu tun - Person entweder transient oder unverändert";
		return false;
	}
	public boolean deleteObject(DbManager dbManager) {
		if(this.isPersistent()) {
			if(dbManager.executeDelete(this))
				return true;
			else {
				message = "ausDbEntfernen: Fehler beim Versuch, die Person zu löschen";
				return false;
			}
		} else
			message = "ausDbEntfernen: Person ist transient, kann daher nicht in der DB gelöscht werden";
		return false;
	}

	/* SQL-String-Methoden von "PersistenzInterface" - ab Lektion 4 */
	public String getInsertSQL() {
		/* "land" und "telefon" können null sein. Dann muss auch "null" gesetzt
		 * werden - ohne Einkleidung durch einfache Anführungszeichen!  */
		String sqlLand = (land == null)? null : "'" + land + "'";
		String sqlTelefon = (telefon == null)? null : "'" + telefon + "'";
		String insertString = "INSERT INTO personen VALUES(NULL, '"
			+ this.getVorname() + "', '" + this.getNachname() + "', '"
			+ this.getStrasse() + "', '" + this.getHausnummer() + "', '"
			+ this.getOrt() + "', " + sqlLand + ", '"
			+ this.getPlz() + "', " + sqlTelefon + ")";
		return insertString;
	}
	public String getRetrieveSQL() {
		String queryString = null;
		if(this.getId() > 0) { 	// noch nicht gelesen, aber schon in DB (id ist dann > 0)
			queryString = "SELECT * FROM personen WHERE id=" + this.getId();
		} else if(this.checkField(vorname) && this.checkField(nachname)) {
			queryString = "SELECT * FROM personen WHERE vorname='" + this.getVorname();
			queryString += "' AND nachname='" + this.getNachname() + "'";
		} else
			message = "ausDbLesen: Vor- und Nachname müssen gesetzt sein";
		return queryString;
	}
	public String getUpdateSQL() {
		// Auch Namensänderungen werden zugelassen!
		String sqlLand = (land == null)? null : "'" + land + "'";
		String sqlTelefon = (telefon == null)? null : "'" + telefon + "'";
		String updateString = "UPDATE personen SET vorname='" + this.getVorname()
			+ "', nachname='" + this.getNachname()
			+ "', strasse='" + this.getStrasse()
			+ "', hausnummer='" + this.getHausnummer()
			+ "', ort='" + this.getOrt()
			+ "', land=" + sqlLand
			+ ", plz='" + this.getPlz()
			+ "', telefon=" + sqlTelefon
			+ " WHERE id=" + this.getId();
		return updateString;
	}
	public String getDeleteSQL() {
		String updateString = "DELETE FROM personen WHERE id=" + this.getId();
		return updateString;
	}
	public boolean loadObjProps(ResultSet rs) {
		try {
			if(rs != null && rs.next()) {
				this.setId(rs.getInt(1));
				this.setVorname(rs.getString(2));
				this.setNachname(rs.getString(3));
				this.setStrasse(rs.getString(4));
				this.setHausnummer(rs.getString(5));
				this.setOrt(rs.getString(6));
				this.setLand(rs.getString(7));
				this.setPlz(rs.getString(8));
				this.setTelefon(rs.getString(9));
				return true;
			}
		} catch (SQLException sqle) {
			System.out.println("Person.ladeObjekt - " + sqle.toString());
		}
		return false;
	}

	@Override
	public String getPufferKey() {
		return null;
	}

	@Override
	public void setPufferKey(String pufferKey) {

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
			if(param.getId() != this.id)
				return false;
			if(!param.getVorname().equals(this.vorname))
				return false;
			if(!param.getNachname().equals(this.nachname))
				return false;
			if(!param.getStrasse().equals(this.strasse))
				return false;
			if(!param.getHausnummer().equals(this.hausnummer))
				return false;
			if(!param.getOrt().equals(this.ort))
				return false;
			// optionaler Parameter "land":
			if(param.getLand() == null) {
				if(this.land != null)
					return false;
			}
			else if(!param.getLand().equals(this.land))
				return false;
			if(!param.getPlz().equals(this.plz))
				return false;
			// optionaler Parameter "telefon"
			if(param.getTelefon() == null) {
				if(this.telefon != null)
					return false;
			} else if(!param.getTelefon().equals(this.telefon))
				return false;
			return true;
		}
		return false;
	}
	@Override
	public String toString(){
		return "Person id=" + this.getId() + " " + this.getVorname() + " " + this.getNachname();
	}
}
