package jav12Einsendeaufgaben.angestellterAnmeldung.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/* Seit Lektion 4 haben die Methoden für Datenbankoperationen statt
 * dem Connection-Objekt ein DbManager-Objekt als Parameter. */
public interface PersistenzInterface extends Cloneable {

	/* Persistenzeigenschaften */
	boolean isPersistent();
	void    setPersistent( boolean newValue );

	boolean isModified();
	void    setModified( boolean newValue );

	/* id-Eigenschaft für ale PI-Objekte ab Lektion 4 */
	void setId(int id);
	int getId();

	/* Fügt das aktuelle Objekt in die Datenbank ein (SQL-INSERT). */
	boolean insertObject(jav12Einsendeaufgaben.angestellterAnmeldung.db.DbManager dbManager);

	/* Liest ein Objekt aus der Datenbank (SQL-SELECT).
	 * In Lektion 5 erhält die Methode eine andere Rückgabe, um alternativ das
	 * Original oder das Pufferobjekt durchreichen zu können. */
	PersistenzInterface retrieveObject(jav12Einsendeaufgaben.angestellterAnmeldung.db.DbManager dbManager) throws SQLException;

	/* Ändert den Objektzustand in der Datenbank (SQL-UPDATE).  */
	boolean updateObject(jav12Einsendeaufgaben.angestellterAnmeldung.db.DbManager dbManager);

	/* Löscht das aktuelle Objekt aus der Datenbank (SQL-DELETE). */
	boolean deleteObject(jav12Einsendeaufgaben.angestellterAnmeldung.db.DbManager dbManager);

	/* SQL-String- und Verwaltungsmethoden - ab Lektion 4 */
	String getInsertSQL();
	String getRetrieveSQL();
	String getUpdateSQL();
	String getDeleteSQL();

	/* Läd die Properties (Eigenschaften) eines perssistenzfähigen Objekts aus
	 * dem übergebenen ResaultSet-Objekt - Ergebnis einer Datenbankabfrage.
	 * Der Methodenname kürzt "loadObjectProperties" ab. */
	boolean loadObjProps(ResultSet rs);

	/* Ergänzung in Lektion 5:
	 * Das mapping dient der S u c h e ("find" - bzw. "retrieve" aus CRUD)
	 * im Puffer - also muss ein Wert erzeugt werden, mit dem typischerweise
	 * gesucht wird. Die Schlüsseldefinition sollte auf die angebotenen
	 * Konstruktoren (bzw. deren Parameter) abgestimmt werden.  */
	String getPufferKey();
	void setPufferKey(String pufferKey);

	PersistenzInterface clone() throws CloneNotSupportedException;

	String getMessage();
}
