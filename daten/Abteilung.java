package jav12Einsendeaufgaben.angestellterAnmeldung.daten;

import jav12Einsendeaufgaben.angestellterAnmeldung.db.DbManager;
import jav12Einsendeaufgaben.angestellterAnmeldung.db.PersistenzInterface;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.util.Vector;

public class Abteilung implements PersistenzInterface {

	// Logging
	private boolean log = true;

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

	private DbManager dbManager;
	private Connection connection;

	/* Constructor */
	public Abteilung(){}

	public Abteilung(int id, String name){
		this.id = id;
		this.name = name;
		this.setPufferKey(id + " " + name);
	}

	public Abteilung(int id){
		this.id = id;
		this.setPufferKey(id + ""); //This addition is not working
	}

	public Abteilung(String name){
		this.name = name;
		this.setPufferKey(name); //This addition is not working
	}

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

	private int leiterID;
	public int getLeiterID(){ return leiterID; }
	public void setLeiterID(int leiterID){ this.leiterID = leiterID; }

	private String pufferKey;
	@Override
	public String getPufferKey() { return pufferKey; }

	@Override
	public void setPufferKey(String pufferKey) {
		if(this.pufferKey == null){
			this.pufferKey = pufferKey;
		}
	}


	/* ****** Datenbankoperationen ****** */
	@Override
	public boolean insertObject(DbManager dbManager) {
		if(this.isPersistent()){
			message = "Abteilung#insertObject: Abteilung ist gespeichert.";
			return false;
		} else if (this.id > 0){
			if(dbManager.executeRetrieve(this) == null){
				message = "Abteilung#insertObject: Abteilung is already in Database.";
				return false;
			}
		}
		if(dbManager.executeInsert(this)){
			return true;
		}else{
			message = "Abteilung#insertObject: Fehler beim Einfügen der Abteilung (id= " + id + ")";
			return false;
		}
	}
	@Override
	public PersistenzInterface retrieveObject(DbManager dbManager) {
		PersistenzInterface piObjekt = null;
		if(this.isPersistent()){
			message = "Abteilung#retrieveObject: Der Abteilungsleiter wurde schon aus der DB gelesen.";
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
	@Override
	public boolean deleteObject(DbManager dbManager) {
		if(this.isPersistent()) {
			if(dbManager.executeDelete(this)){
				return true;
			} else {
				message = "Abteilung#deleteObject: Error with deleting Abteilung!";
				return false;
			}
		} else {
			message = "Abteilung#insertObject: Abteilung ist transient, kann daher nicht in der DB gelöscht werden";
			return false;
		}
	}
	@Override
	public boolean updateObject(DbManager dbManager) {
		if(this.isPersistent() && this.isModified()){
			if(dbManager.executeUpdate(this)){
				message = "Abteilung#updateObject: Update is done!";
				return true;
			} else {
				message = "Abteilung#updateObject: Error with the update";
				System.out.println(message);
				return false;
			}
		}
		return false;
	}
	/* ****** SQL - Anweisungen erstellen ****** */
	@Override
	public String getDeleteSQL() {
		String queryString = "DELETE FROM abteilungen WHERE id = " + this.getID();
		return queryString;
	}
	@Override
	public String getInsertSQL() {
		String queryString = "INSERT INTO abteilungen WHERE id = " + this.getID();
		return queryString;
	}
	@Override
	public String getRetrieveSQL() {
		String queryString = null;
		if(this.getID() > 0){
			queryString = "SELECT id, name, leiterID FROM abteilungen WHERE id = " + this.getID();
		} else if (((this.getName()) != null) && (!this.getName().equals(""))){
			queryString = "SELECT id, name, leiterID FROM abteilungen WHERE name='" + this.getName() + "'";
		}
		return queryString;
	}
	@Override
	public String getUpdateSQL() {
		String queryString = "UPDATE abteilungen SET leiterID = " + this.getLeiterID() + ", name = '" + this.getName() + "' WHERE id = " + this.getID();
		return queryString;
	}
	@Override
	public boolean loadObjProps(ResultSet rs) {
		try {
			if(rs != null && rs.next()){
				this.setID(rs.getInt(1));
				this.setName(rs.getString(2));
				this.setLeiterID(rs.getInt(3));
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
			if(param.getID() != this.id){ return false; }
			if (!param.getName().equals(this.getName())){ return false; }
			if(param.getLeiterID() != this.getLeiterID()) { return false; }
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return  "Abteilung-ID= " + id + ", Abteilungsname:  " + this.getName();
	}

	@Override
	public PersistenzInterface clone() throws CloneNotSupportedException {
		Abteilung abteilung = (Abteilung) super.clone();
		if(this.id == 0 || this.getLeiterID() == 0 || this.getName() == null)
			return null;
		abteilung.setModified(false);
		return abteilung;
	}
}