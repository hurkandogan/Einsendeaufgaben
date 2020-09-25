package jav12Einsendeaufgaben.angestellterAnmeldung.db;

import java.sql.ResultSet;

/* Das Interface muss auch in dieses Projekt aufgenommen werden,
 * da es den DbManager dieser Projektversion verwendet. Daher ist kein
 * Import aus "lektion4" möglich.  */
public interface PersistenzInterface extends Cloneable {

	/* Instanzen von Klassen, die diese Interface implementieren, koennen
	 * persistent sein, muessen es aber nicht. Die Instanzen sind dann
	 * persistent, wenn sie aus der Datenbank gelesen, oder in sie
	 * hinein geschrieben wurden. Wenn sie aus der Datenbank geloescht wurden,
	 * sind sie wieder transient.
	 * Die Eigenschaft "persistent" verwaltet für ein Objekt seinen
	 * Persistenzzustand. */
	boolean isPersistent();
	void    setPersistent( boolean newValue );

	/* Solange ein persistentes Objekt in der Anwendung den gleichen Zustand
	 * hat wie in der Datenbank, liefert "isModified" den Wert "false",
	 * ansonsten "true". Es liefert also insbesondere dann "true", wenn der
	 * Objektzustand durch eine set-Methode verändert wurde.  */
	boolean isModified();
	void    setModified( boolean newValue );

	void setID(int id);
	int getID();

	/* Fügt das aktuelle Objekt in die Datenbank ein (SQL-INSERT). */
	boolean insertObject(DbManager dbManager);
	/* Löscht das aktuelle Objekt aus der Datenbank (SQL-DELETE). */
	boolean deleteObject(DbManager dbManager);
	/* Ändert den Objektzustand in der Datenbank (SQL-UPDATE).  */
	boolean updateObject(DbManager dbManager);
	/* Sucht und liest ein Objekt aus der Datenbank (SQL-SELECT).  */
	boolean retrieveObject(DbManager dbManager);

	/* SQL-String Methods */
	String getDeleteSQL();
	String getInsertSQL();
	String getRetrieveSQL();
	String getUpdateSQL();

	/*
	 * Läd die Properties (Eigenschaften) eines persistenzfähigen Objekts aus
	 * dem übergebenen ResaultSet-Objekt - Ergebnis einer Datenbankabfrage.
	 * Der Methodenname kürzt "loadObjectProperties" ab.
	 * */
	boolean loadObjProps(ResultSet rs);

	/* Ergänzung in Lektion 5:
	 * Das mapping dient der S u c h e ("find" - bzw. "retrieve" aus CRUD)
	 * im Puffer - also muss ein Wert erzeugt werden, mit dem typischerweise
	 * gesucht wird. Die Schlüsseldefinition sollte auf die angebotenen
	 * Konstruktoren (bzw. deren Parameter) abgestimmt werden.
	 * */
	String getPufferKey();
	void setPufferKey(String pufferKey);

	String getMessage();
	void setMessage(String message);

	PersistenzInterface clone() throws CloneNotSupportedException;
}