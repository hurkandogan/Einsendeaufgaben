package jav12Einsendeaufgaben.angestellterAnmeldung.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PiPuffer {
	/* Der Puffer für alle HashMaps, je mit einer Objektmenge vom Typ einer
	 * PersistenzInterface-Klasse.
	 * Parametrisierung "piPuffer":
	 * 		key=String (Klassenname), value=HashMap
	 * Parametrisierung der Inhalts-HashMaps "piImplPuffer":
	 * 		key=String (gemappte Suchinformationen), value=piObjekt  */
	private HashMap<String, HashMap<String, PersistenzInterface>> piPuffer;

	/* Die Puffer für die PI-Objekte in den HashMaps der Objektmengen, sortiert
	 * nach dem Typ der PersistenzInterface-Implementierung. */
	private HashMap<String, PersistenzInterface> piImplPuffer;

	public PiPuffer() {
		piPuffer = new HashMap<String, HashMap<String,PersistenzInterface>>();
	}
	/* ******* Basisausstattung für HashMap: get, put und remove. *******  */
	public PersistenzInterface get(PersistenzInterface piObjekt) {
		piImplPuffer = piPuffer.get(piObjekt.getClass().getName());
		if (piImplPuffer != null) {
			return piImplPuffer.get(piObjekt.getPufferKey());
		} else {
			return null;
		}
	}
	/* Gibt das aus der DB geladene und im Puffer gecachte
	 * Datenbank-Originalobjekt zurück */
	public PersistenzInterface getDbCache(PersistenzInterface piObjekt) {
		piImplPuffer = piPuffer.get(piObjekt.getClass().getName());
		if (piImplPuffer != null) {
			return piImplPuffer.get(this.getCacheKey(piObjekt));
		} else {
			return null;
		}
	}
	public PersistenzInterface put(PersistenzInterface piObjekt) {
		piImplPuffer = piPuffer.get(piObjekt.getClass().getName());
		if (piImplPuffer == null){
			piImplPuffer = new HashMap<String,PersistenzInterface>();
			piPuffer.put(piObjekt.getClass().getName(), piImplPuffer);
		}
		return piImplPuffer.put(piObjekt.getPufferKey(), piObjekt);
	}
	/* cacht das aus der Datenbank geladene Originalobjekt im Puffer */
	public PersistenzInterface putDbCache(PersistenzInterface piObjekt) {
		piImplPuffer = piPuffer.get(piObjekt.getClass().getName());
		if (piImplPuffer == null){
			piImplPuffer = new HashMap<String,PersistenzInterface>();
			piPuffer.put(piObjekt.getClass().getName(), piImplPuffer);
		}
		return piImplPuffer.put(this.getCacheKey(piObjekt), piObjekt);
	}

	/* entfernt zunächst das aus der Datenbank gecachte Objekt und dann das gelöschte */
	public PersistenzInterface remove(PersistenzInterface piObjekt) {
		piImplPuffer = piPuffer.get(piObjekt.getClass().getName());
		if (piImplPuffer != null) {
			piImplPuffer.remove(this.getCacheKey(piObjekt));
			PersistenzInterface removedPiObj = piImplPuffer.remove(piObjekt.getPufferKey());
			/* wenn der klassenspezifische Puffer (piImplPuffer)leer ist, wird auch
			 * dieser aus dem piPuffer entfernt - vgl. Methode "isEmpty" */
			if (piImplPuffer.isEmpty()){
				piPuffer.remove(piObjekt.getClass().getName());
			}
			return removedPiObj;
		} else {
			return null;
		}
	}
	/* ******* Weitere Methoden zum Handling des PiPuffers *******  */
	public void clear() {
		Iterator<HashMap<String,PersistenzInterface>> piImplPufferIterator = piPuffer.values().iterator();
		while (piImplPufferIterator.hasNext()) {
			piImplPuffer = piImplPufferIterator.next();
			piImplPuffer.clear();	// HashMap clear-Methode
		}
		piPuffer.clear();			// HashMap clear-Methode
	}

	public boolean containsValue(PersistenzInterface piObjekt) {
		piImplPuffer = piPuffer.get(piObjekt.getClass().getName());
		if (piImplPuffer != null) {
			return piImplPuffer.containsValue(piObjekt.getPufferKey());
		} else {
			return false;
		}
	}
	/* Hier macht die Typparametrisierung schön deutlich, was eigentlich
	 * in der zurückgegebenen Liste enthalten ist:
	 * Alle PI-Objekte (die hier aus allen piImplPuffer-HashMaps
	 * in der piPuffer-HashMap gesammelt werden). */
	public List<PersistenzInterface> values() {
		List<PersistenzInterface> piObjekte = new ArrayList<PersistenzInterface>();
		Iterator<HashMap<String,PersistenzInterface>> piImplPufferIterator = piPuffer.values().iterator();
		while (piImplPufferIterator.hasNext()){
			piImplPuffer = piImplPufferIterator.next();
			Iterator<PersistenzInterface> piObjIterator = piImplPuffer.values().iterator();
			while (piObjIterator.hasNext()){
				piObjekte.add(piObjIterator.next());
			}
		}
		return piObjekte;	// die  ArrayList
	}

	/* Sagt aus, ob der gesamte Puffer "piPuffer" leer ist, also keine
	 * HashMaps mit piObjekten enthält */
	public boolean isEmpty() {
		/* funktioniert nur, weil in "remove" auch jede klassenspezifische
		 * HashMap gelöscht wird, wenn sie leer ist */
		return piPuffer.isEmpty();
	}
	/* Anzahl aller piObjekte in den diversen HashMaps von piPuffer*/
	public int size() {
		Iterator<HashMap<String, PersistenzInterface>> piImplPufferEnum = piPuffer.values().iterator();
		int result = 0;
		while(piImplPufferEnum.hasNext()){
			result += piImplPufferEnum.next().size();
		}
		return result;
	}
	public String getCacheKey(PersistenzInterface piObjekt) {
		return "cache_" + piObjekt.getPufferKey();
	}
}
