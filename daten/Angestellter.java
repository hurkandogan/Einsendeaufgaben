package jav12Einsendeaufgaben.angestellterAnmeldung.daten;

import jav12Einsendeaufgaben.angestellterAnmeldung.db.DbManager;
import jav12Einsendeaufgaben.angestellterAnmeldung.db.PersistenzInterface;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Angestellter implements PersistenzInterface {

	/* ****** PI-Properties ****** */
	private boolean persistent;
	public boolean isPersistent() { return persistent; }
	public void setPersistent(boolean newValue) { this.persistent = newValue; }

	private boolean modified;
	public boolean isModified() { return modified; }
	public void setModified(boolean newValue) { this.modified = newValue; }

	/* Konstruktoren */
	public Angestellter(int id) {
		this.id = id;
	}

	public Angestellter(Person person) {
		this.person = person;
	}
	/* ausgewählte Properties Angestelltendaten, ausreichend für Kunden-Client */
	private int id;		// auch von PI gefordert
	public int getId() { return id; }
	public void setId(int id) { this.id = id;  this.setModified(true); }

	private jav12Einsendeaufgaben.angestellterAnmeldung.daten.Abteilung abteilung;
	public jav12Einsendeaufgaben.angestellterAnmeldung.daten.Abteilung getAbteilung() { return abteilung; }
	public void setAbteilung(jav12Einsendeaufgaben.angestellterAnmeldung.daten.Abteilung abteilung) { this.abteilung = abteilung;  this.setModified(true); }

	private String geschlecht;
	public String getGeschlecht() { return geschlecht; }
	public void setGeschlecht(String geschlecht) { this.geschlecht = geschlecht;  this.setModified(true); }

	private Person person;
	public Person getPerson() { return person; }
	public void setPerson(Person person) { this.person = person;  this.setModified(true); }

	/* weitere Felder */
	private String message;
	public String getMessage() { return message; }

	/* ****** Datenbankoperationen ****** */
	public boolean retrieveObject(DbManager dbManager) {
		if(this.isPersistent()) {
			message = "ausDbLesen: Der Angestellte wurde schon aus der DB gelesen";
			return true;
		} else {
			if(!dbManager.executeRetrieve(this)) { // alternative Suche nach id oder Name in
				message = "ausDbLesen: Kein Angestellter in der Datenbank";
				return false;
			} else {
				return true;
			}
		}
	}
	public boolean deleteObject(DbManager dbManager) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean updateObject(DbManager dbManager) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean insertObject(DbManager dbManager) {
		// TODO Auto-generated method stub
		return false;
	}
	/* ****** SQL-Anweisungen erstellen ****** */
	public String getDeleteSQL() {

		// TODO Auto-generated method stub
		return null;
	}
	public String getInsertSQL() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getRetrieveSQL() {
		String queryString = null;
		if(this.getId() > 0) { 	// noch nicht gelesen, aber schon in DB (id ist dann > 0)
			queryString = "SELECT id, abtID, geschlecht, personID FROM angestellte WHERE id=" + this.getId();
		} else {				// Objekt ist transient
			if(this.getPerson().getId() > 0)
				queryString = "SELECT id, abtID, geschlecht, personID FROM angestellte WHERE personID=" + this.getPerson().getId();
			else
				message = "Angestellter.ausDbLesen: PersonID muss gesetzt sein (ist aber 0)";
		}
		return queryString;
	}
	public String getUpdateSQL() {
		// TODO Auto-generated method stub
		return null;
	}
	public boolean loadObjProps(ResultSet rs) {
		try {
			if(rs != null && rs.next()) {
				this.setId(rs.getInt(1));
				int newID = rs.getInt(2);
				if(abteilung == null)	//für temporäre Objekte
					abteilung = new jav12Einsendeaufgaben.angestellterAnmeldung.daten.Abteilung(rs.getInt(2));
				if(newID != this.getAbteilung().getId()) // sonst würde das eingebettete Person-Objekt als "verändert" markiert!
					this.getAbteilung().setId(newID);
				this.setGeschlecht(rs.getString(3));
				newID = rs.getInt(4);
				if(person == null)	//für temporäre Objekte
					person = new Person(newID);
				if(newID != this.getPerson().getId()) // sonst würde das eingebettete Person-Objekt als "verändert" markiert!
					this.getPerson().setId(newID);
				return true;
			}
		} catch (SQLException sqle) {
			System.out.println("Angestellter.ladeObjekt - " + sqle.toString());
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

	/* ******* sonstige Hilfsmethoden ****** */
	@Override
	public boolean equals(Object object){
		// TODO implement
		return false;
	}
	@Override
	public String toString() {
		return "Angestellter id=" + id + " " + this.getPerson() + ", Abteilung " + abteilung;//.getId();//this.getAbteilung().getName();
	}
}