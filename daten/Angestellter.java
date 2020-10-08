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
	public void setID(int id){ this.id = id; this.setModified(true); }

	private Abteilung abteilung;
	public Abteilung getAbteilung(){ return abteilung; }
	public void setAbteilung(Abteilung abteilung){
		this.abteilung = abteilung;
		this.setModified(true);
	}
	public void setAbteilung(DbManager dbManager){
		System.out.println("SETABTEILUNG " + abteilung.getID());
		System.out.println("ABTEILUNGTOSTRING " + abteilung.toString());
		abteilung = (Abteilung) dbManager.executeRetrieve(abteilung);
	}
	private String pufferKey;
	@Override
	public String getPufferKey() { return pufferKey; }
	@Override
	public void setPufferKey(String pufferKey) {
		if(this.pufferKey == null){ this.pufferKey = pufferKey; }
	}
	private boolean isAbteilungsleiter;
	public boolean isAbteilungsleiter(){ return isAbteilungsleiter; }
	public void setAbteilungsleiter(boolean isAbteilungsleiter){ this.isAbteilungsleiter = isAbteilungsleiter; }

	private Person person;
	public Person getPerson() { return person; }
	public void setPerson(Person person) { this.person = person;  this.setModified(true); }

	private int abtID;
	public int getAbtID(){ return abtID; }
	public void setAbtID(int abtID){ this.abtID = abtID; this.setModified(true); }

	private String versicherungsNr;
	public String getVersicherungsNr(){ return versicherungsNr; }
	public void setVersicherungsNr(String versicherungsNr){ this.versicherungsNr = versicherungsNr; this.setModified(true); }

	private Float gehalt;
	public Float getGehalt(){ return gehalt; }
	public void setGehalt(Float gehalt){ this.gehalt = gehalt; this.setModified(true); }

	private Date einstellungsDatum;
	public Date getEinstellungsDatum(){ return einstellungsDatum; }
	public void setEinstellungsDatum(Date einstellungsDatum){ this.einstellungsDatum = einstellungsDatum; this.setModified(true); }

	private Date ausscheidedatum;
	public Date getAusscheidedatum(){ return ausscheidedatum; }
	public void setAusscheidedatum(Date ausscheidedatum){ this.ausscheidedatum = ausscheidedatum; this.setModified(true); }

	private Date geburtsdatum;
	public Date getGeburtsdatum(){ return geburtsdatum; }
	public void setGeburtsdatum(Date geburtsdatum){ this.geburtsdatum = geburtsdatum; this.setModified(true); }

	private String geschlecht;
	public String getGeschlecht() { return geschlecht; }
	public void setGeschlecht(String geschlecht) { this.geschlecht = geschlecht;  this.setModified(true); }

	/******* Databaseoperations *******/
	@Override
	public boolean insertObject(DbManager dbManager) {
		if(this.isPersistent()){
			message = "Angestellter#inserObject: No data to save!";
			return false;
		} else if (this.getPerson().getID() > 0) {
			if (dbManager.executeRetrieve(this) != null){
				message = "Angestellter#inserObject: This person " + this.getPerson().getVorname() + " " + this.getPerson().getNachname() + " is already in Database";
				return false;
			}
			if(dbManager.executeInsert(this)){
				message = "Angestellter#inserObject: This person " + this.getPerson().getVorname() + " " + this.getPerson().getNachname() + " is added to Database!";
				return true;
			} else {
				message = "Angestellter#inserObject: Error! Person could not be added!";
				return false;
			}
		} else {
			message = "Angestellter#inserObject: This person has no Person ID!";
			return false;
		}
	}
	@Override
	public PersistenzInterface retrieveObject(DbManager dbManager) {
		PersistenzInterface piObj = null;
		if(this.isPersistent()) {
			message = "Angestellter#retrieveObject: Der Angestellte wurde schon aus der DB gelesen";
			return this;
		} else {
			if((piObj = dbManager.executeRetrieve(this)) == null) { // alternative Suche nach id oder Name in
				message = "Angestellter#retrieveObject: Kein Angestellter in der Datenbank";
				return null;
			} else {
				return piObj;
			}
		}
	}
	@Override
	public boolean updateObject(DbManager dbManager) {
		if(this.isPersistent() && this.isModified()){
			if(this.getPerson() != null){
				if(dbManager.executeUpdate(this)){
					message = "Angestellter#updateObject: Update is successful!";
					return true;
				} else {
					message = "Angestellter#updateObject: Error! Update is not successful!";
					return false;
				}
			} else {
				message = "Angestellter#updateObject: Error! This is not a Person!";
			}
		}
		return false;
	}
	@Override
	public boolean deleteObject(DbManager dbManager) {
		if (this.isPersistent()){
			if (dbManager.executeDelete(this)){
				return true;
			} else {
				message = "Angestellter#deleteObject: Error! Angestellter is not deleted!";
				return false;
			}
		}
		return false;
	}

	/* ****** SQL-Anweisungen erstellen ****** */
	@Override
	public String getInsertSQL() {
		String queryString = "INSERT INTO angestellte VALUES(Null, " + this.getAbtID() + ", '" + this.getVersicherungsNr() + "', " + this.getGehalt() + ", '"
				+ this.getEinstellungsDatum() + "', '" + this.getAusscheidedatum() + "', '" + this.getGeburtsdatum() + "', '" + this.getGeschlecht() + "')";
		return queryString;
	}
	@Override
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
	@Override
	public String getUpdateSQL() {
		if (this.getPerson() != null) {
			if (this.getAusscheidedatum() != null) {
				String queryString = "UPDATE angestellte SET abtID=" + this.getAbtID() + ", personID="
						+ this.getPerson().getID() + ", versicherungsnr=" + this.getVersicherungsNr() + ", gehalt="
						+ this.getGehalt() + ", einstellungsdatum='" + this.getEinstellungsDatum() + "' "
						+ ", ausscheidedatum='" + this.getAusscheidedatum() + "' " + ", geburtsdatum='"
						+ this.getGeburtsdatum() + "' " + ", geschlecht='" + this.getGeschlecht() + "'" + " WHERE id=" + this.getID();
				return queryString;
			} else {
				String queryString = "UPDATE angestellte SET abtID=" + this.getAbtID() + ", personID="
						+ this.getPerson().getID() + ", versicherungsnr=" + this.getVersicherungsNr() + ", gehalt="
						+ this.getGehalt() + ", einstellungsdatum='" + this.getEinstellungsDatum() + "' "
						+ ", ausscheidedatum='0000-00-00', " + "geburtsdatum='" + this.getGeburtsdatum() + "' " + ", geschlecht='" + this.getGeschlecht() + "'" + " WHERE id=" + this.getID();
				return queryString;
			}
		}
		return null;
	}
	@Override
	public String getDeleteSQL() {
		String queryString = "DELETE FROM angestellte WHERE personID = " + this.getPerson().getID();
		return queryString;
	}
	@Override
	public boolean loadObjProps(ResultSet rs) {
		try {
			if (rs != null && rs.next()) {
				this.setID(rs.getInt(1));
				this.setAbtID(rs.getInt(2));
				if (abteilung == null)    //für temporäre Objekte
					abteilung = new Abteilung(rs.getInt(2));
				if (this.getAbtID() != this.getAbteilung().getID()) { // sonst würde das eingebettete Person-Objekt als "verändert" markiert!
					this.getAbteilung().setID(this.getAbtID());
				}
				this.setVersicherungsNr(rs.getString(3));
				this.setGehalt(rs.getFloat(4));
				this.setEinstellungsDatum(rs.getDate(5));
				this.setAusscheidedatum(rs.getDate(6));
				this.setGeburtsdatum(rs.getDate(7));
				this.setGeschlecht(rs.getString(8));
				int newPersonID = rs.getInt(9);
				if (person == null){ person = new Person(newPersonID); }
				if(newPersonID != this.getPerson().getID()) { this.getPerson().setID(newPersonID); }
				return true;
			}
		} catch (SQLException sqle) {
			System.out.println("Angestellter#loadObjProps - " + sqle.toString());
		}
		return false;
	}
	/******** sonstige Hilfsmethoden *******/
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
		return "Angestellter id=" + this.getID() + " " + this.getPerson() + ", " + "AbteilungsID= " + this.getAbtID()
				+ " " + "Vers Nr= " + getVersicherungsNr() + " " + "Gehalt= " + getGehalt() + " " + "Einstellungsdatum= "
				+ getEinstellungsDatum() + " " + "Ausscheidedatum= " + getAusscheidedatum() + " " + "Geburtsdatum= "
				+ getGeburtsdatum() + " " + "Geschlecht= " + getGeschlecht();
	}
	@Override
	public PersistenzInterface clone() throws CloneNotSupportedException {
		Angestellter angestellter  = (Angestellter) super.clone();
		if(this.id == 0 || this.person == null){ return null; }
		angestellter.setPerson((Person) this.person.clone());
		angestellter.setModified(false);
		return angestellter;
	}
}