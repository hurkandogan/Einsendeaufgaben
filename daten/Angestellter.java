package jav12Einsendeaufgaben.angestellterAnmeldung.daten;

import jav12Einsendeaufgaben.angestellterAnmeldung.db.DbManager;
import jav12Einsendeaufgaben.angestellterAnmeldung.db.PersistenzInterface;

import java.sql.Date;
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

	/* Message Fields */
	private String message;
	public String getMessage() { return message; }

	/* Konstruktoren */
	public Angestellter(int id) { this.id = id; }
	public Angestellter(Person person) {
		this.person = person;
		this.setModified(false);
		this.setPersistent(false);
		this.setPufferKey(new Integer(this.getPerson().getID()).toString());
	}

	// Properties
	private int id;
	public int getID(){ return id; }
	public void setID(int id){
		this.id = id;
		this.setModified(true);
	}

	private Abteilung abteilung;
	public Abteilung getAbteilung(){ return abteilung; }
	public void setAbteilung(Abteilung abteilung){
		this.abteilung = abteilung;
		this.setModified(true);
	}
	private String geschlecht;
	public String getGeschlecht() { return geschlecht; }
	public void setGeschlecht(String geschlecht) { this.geschlecht = geschlecht;  this.setModified(true); }

	private Person person;
	public Person getPerson() { return person; }
	public void setPerson(Person person) { this.person = person;  this.setModified(true); }

	private String versicherungsNummer;
	public String getVersicherungsNummer(){ return versicherungsNummer; }
	public void setVersicherungsNummer(String versicherungsNummer){
		this.versicherungsNummer = versicherungsNummer;
		this.setModified(true);
	}

	private Float gehalt;
	public Float getGehalt(){ return gehalt; }
	public void setGehalt(Float gehalt){
		this.gehalt = gehalt;
		this.setModified(true);
	}

	private Date einstellungsDatum;
	public Date getEinstellungsDatum(){ return einstellungsDatum; }
	public void setEinstellungsDatum(Date einstellungsDatum){
		this.einstellungsDatum = einstellungsDatum;
		this.setModified(true);
	}

	private Date ausscheidedatum;
	public Date getAusscheidedatum(){ return ausscheidedatum; }
	public void setAusscheidedatum(Date ausscheidedatum){
		this.ausscheidedatum = ausscheidedatum;
		this.setModified(true);
	}

	private Date geburtsdatum;
	public Date getGeburtsdatum(){ return geburtsdatum; }
	public void setGeburtsdatum(Date geburtsdatum){
		this.geburtsdatum = geburtsdatum;
		this.setModified(true);
	}

	/* ****** Datenbankoperationen ****** */
	public PersistenzInterface retrieveObject(DbManager dbManager) {
		PersistenzInterface piObj = null;
		if(this.isPersistent()) {
			message = "Angestellter#retrieveObject: Der Angestellte wurde schon aus der DB gelesen";
			return this;
		} else {
			if((piObj = dbManager.executeRetrieve(this)) != null) { // alternative Suche nach id oder Name in
				message = "Angestellter#retrieveObject: Kein Angestellter in der Datenbank";
				return null;
			} else {
				return piObj;
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
		if(this.getID() > 0) { 	// noch nicht gelesen, aber schon in DB (id ist dann > 0)
			queryString = "SELECT * FROM angestellte WHERE id=" + this.getID();
		} else {				// Objekt ist transient
			if(this.getPerson().getID() > 0)
				queryString = "SELECT * FROM angestellte WHERE personID=" + this.getPerson().getID();
			else
				message = "Angestellter#retrieveObject: PersonID muss gesetzt sein (ist aber 0)";
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
				this.setID(rs.getInt(1));
				int newID = rs.getInt(2);
				if(abteilung == null)	//für temporäre Objekte
					abteilung = new Abteilung(rs.getInt(2));
				if(newID != this.getAbteilung().getID()) { // sonst würde das eingebettete Person-Objekt als "verändert" markiert!
					this.getAbteilung().setID(newID);
				}
					this.setVersicherungsNummer(rs.getString(3));
					this.setGehalt(rs.getFloat(4));
					this.setEinstellungsDatum(rs.getDate(5));
					this.setAusscheidedatum(rs.getDate(6));
					this.setGeburtsdatum(rs.getDate(7));
					this.setGeschlecht(rs.getString(8));
					newID = rs.getInt(9);
				if(person == null)	//für temporäre Objekte
					person = new Person(newID);
				if(newID != this.getPerson().getID()) // sonst würde das eingebettete Person-Objekt als "verändert" markiert!
					this.getPerson().setID(newID);
				return true;
			}
		} catch (SQLException sqle) {
			System.out.println("Angestellter#loadObjProps - " + sqle.toString());
		}
		return false;
	}

	/* ******* sonstige Hilfsmethoden ****** */
	@Override
	public boolean equals(Object object){
		if(object != null && object instanceof Angestellter) {
			Angestellter param = (Angestellter) object;
			if(param.getID() != this.id) {
				return false;
			}
			if(param.getPerson().getID() != this.person.getID()) {
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "Angestellter id=" + id + " " + this.getPerson() + ", Abteilung " + abteilung;
	}
	private String pufferKey;
	@Override
	public String getPufferKey() {
		return pufferKey;
	}
	@Override
	public void setPufferKey(String pufferKey) {
		if(this.pufferKey == null){
			this.pufferKey = pufferKey;
		}
	}

	@Override
	public PersistenzInterface clone() throws CloneNotSupportedException {
		return abteilung;
	}
}