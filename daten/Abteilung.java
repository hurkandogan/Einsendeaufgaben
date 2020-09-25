package jav12Einsendeaufgaben.angestellterAnmeldung.daten;

import jav12Einsendeaufgaben.angestellterAnmeldung.db.DbManager;
import jav12Einsendeaufgaben.angestellterAnmeldung.db.PersistenzInterface;

import java.sql.ResultSet;

public class Abteilung implements PersistenzInterface {

	String message = "";
	public String getMessage() { return message; }

	@Override
	public void setMessage(String message) {

	}

	@Override
	public PersistenzInterface clone() throws CloneNotSupportedException {
		return null;
	}

	/* ****** PI-Properties ****** */
	private boolean persistent;
	public boolean isPersistent() { return persistent; }
	public void setPersistent(boolean newValue) { this.persistent = newValue; }

	private boolean modified;
	public boolean isModified() { return modified; }
	public void setModified(boolean newValue) { this.modified = newValue; }

	@Override
	public void setID(int id) {

	}

	@Override
	public int getID() {
		return 0;
	}

	/* Konstruktoren */
	public Abteilung(int id) {
		this.id = id;
	}

	/* Properties zu Abteilungsdaten */
	private int id;
	public int getId() { return id; }
	public void setId(int id) { this.id = id; this.setModified(true); }

	private String name;
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	private Angestellter leiter;
	public Angestellter getAngestellter() { return leiter; }
	public void setAngestellter(Angestellter leiter) { this.leiter = leiter; this.setModified(true);}

	/* ****** Datenbankoperationen ****** */
	public boolean retrieveObject(DbManager dbManager) {
		// TODO Auto-generated method stub
		return false;
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
		if(this.getId() > 0){
			querySQL = "SELECT * FROM abteilungen WHERE id = " + this.getId();
		} else{
			if (this.getAngestellter().getId() > 0) {
				querySQL = "SELECT * FROM abteilungen WHERE id = " + this.getAngestellter().getAbteilung();
			} else {
				message = "Abteilung#getInsertSQL: There is no person with this ID";
			}
		}
		return null;
	}
	public String getRetrieveSQL() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getUpdateSQL() {
		// TODO Auto-generated method stub
		return null;
	}
	public boolean loadObjProps(ResultSet rs) {
		// TODO Auto-generated method stub
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
	/* "equals" wird überschrieben, um zwei Abteilungsobjeke auf Gleichheit
	 * zu testen. */
	@Override
	public boolean equals(Object object){
		// TODO implement
		return false;
	}
	@Override
	public String toString() {
		return  new Integer(id).toString();
	}
}