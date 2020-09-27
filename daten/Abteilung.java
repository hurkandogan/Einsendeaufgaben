package jav12Einsendeaufgaben.angestellterAnmeldung.daten;

import jav12Einsendeaufgaben.angestellterAnmeldung.db.DbManager;
import jav12Einsendeaufgaben.angestellterAnmeldung.db.PersistenzInterface;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Abteilung implements PersistenzInterface {

	// Logging
	private boolean log;

	// Messages
	String message = "";
	public String getMessage() { return message; }

	/* ****** PI-Properties ****** */
	private boolean persistent;
	public boolean isPersistent() { return persistent; }
	public void setPersistent(boolean newValue) { this.persistent = newValue; }

	private boolean modified;
	public boolean isModified() { return modified; }
	public void setModified(boolean newValue) { this.modified = newValue; }

	/* Constructor */
	public Abteilung(int id){
		this.id = id;
		this.setPufferKey(id + ""); //This addition is not working
	}

	public Abteilung(String test){}

	public Abteilung(Angestellter angestellter){
		this.leiter = angestellter;
		this.setModified(false);
		this.setPersistent(false);
		this.setPufferKey(new Integer(this.getAngestellter().getID()).toString());
	}

	// Abteilung Properties
	private int id;
	@Override
	public int getID() { return id; }
	@Override
	public void setID(int id) { this.id = id; this.setModified(true); }

	private String name;
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	private Angestellter leiter;
	public Angestellter getAngestellter() { return leiter; }
	public void setAngestellter(Angestellter leiter) { this.leiter = leiter; this.setModified(true);}

	/* ****** Datenbankoperationen ****** */
	public PersistenzInterface retrieveObject(DbManager dbManager) {
		PersistenzInterface piObjekt = null;
		if(this.isPersistent()){
			message = "Abteilung#retrieveObject: Der Abteilungsleiter wurde schon ausder DB gelesen.";
			if(log) { System.out.println(message); }
			return this;
		} else {
			if((piObjekt = dbManager.executeRetrieve(this)) == null) {
				message = "Abteilung#retrieveObject: Kein Abteilungsleiter in der Datenbank.";
				if(log){ System.out.println(message); }
				return null;
			} else {
				return piObjekt;
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
		String querySQL = null;
		if(this.getID() > 0){
			querySQL = "SELECT * FROM abteilungen WHERE id = " + this.getID();
		} else{
			if (this.getAngestellter().getID() > 0) {
				querySQL = "SELECT * FROM abteilungen WHERE id = " + this.getAngestellter().getAbteilung();
			} else {
				message = "Abteilung#getInsertSQL: There is no person with this ID";
			}
		}
		return null;
	}
	public String getRetrieveSQL() {
		String queryString = null;
		if(this.getID() > 0){
			queryString = "SELECT id, name, leiterID FROM abteilungen WHERE id = " + this.getID();
		}else{
			if(this.getAngestellter().getID() > 0) {
				queryString = "SELECT id, name, leiterID FROM abteilungen WHERE leiterID = " + this.getAngestellter().getID();
			}else{
				message = "Abteilungen#getRetrieveSQL: personID should be given!";
				if(log) {
					System.out.println(message);
				}
			}
		}
		return queryString;
	}

	public String getUpdateSQL() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean loadObjProps(ResultSet rs) {
		try {
			if(rs != null && rs.next()){
				this.setID(rs.getInt(1));
				this.setName(rs.getString(2));
				message = "Abteilung datas read from ResultSet";
				if(log){
					System.out.println(message);
				}
				return true;
			}
			message = (rs == null) ? "ResultSet null" : "ResultSet Leer";
			message += ", keine Abteilungsdaten aus DB gelesen";
		}catch(SQLException sqle){
			System.out.println("Abteilung#loadObjProps: " + sqle.toString());
			message = "Abteilung#loadObjProps: \" + sqle.toString()";
		}
		return false;
	}
	/* ******* sonstige Hilfsmethoden ****** */
	/* "equals" wird überschrieben, um zwei Abteilungsobjeke auf Gleichheit
	 * zu testen. */
	@Override
	public boolean equals(Object object){
		if(object != null && object instanceof Abteilung){
			Abteilung param = (Abteilung) object;
			if(param.getID() != this.id){
				return false;
			} else if (param.getAngestellter().getID() != this.leiter.getID()){
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return  "Abteilung-ID= " + id + " " + this.getAngestellter();
	}

	private String pufferKey;
	@Override
	public String getPufferKey() { return pufferKey; }

	@Override
	public void setPufferKey(String pufferKey) {
		if(this.pufferKey == null){
			this.pufferKey = pufferKey;
		}
	}

	@Override
	public PersistenzInterface clone() throws CloneNotSupportedException {
		return leiter;
	}
}