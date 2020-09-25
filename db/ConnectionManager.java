package jav12Einsendeaufgaben.angestellterAnmeldung.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Verwaltet einen Pool von einem Connection-Objekt zur Datenbank
 * mit dem fest eingestellten Namen "dbdemo2".
 */
public final class ConnectionManager extends Object {

	private static Connection poolConnection = null;
	private static String mysqlJdbcDriverClass = "org.gjt.mm.mysql.Driver";
	private static String mysqlJdbcConnectionProtocol = "jdbc:mysql://";
	private static String database = "/dbdemo2";

	public static boolean log = false;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/* Empty Constructor */
	private ConnectionManager() {}

	/**
	 * Macht die einzige Connection im Pool des ConnectionManagers
	 * verfügbar oder erstellt eine neue Connection, wenn keine verfügbar ist.
	 * @param host Name des zugreifenden Hosts
	 * @param user	Name des bei der Datenbank registrierten Benutzers
	 * @param passwort Passwort dieses Benutzers
	 * @return Connection-Objekt für die Datenbank "dbdemo2"
	 * @throws ClassNotFoundException wenn die JDBC-Treiberklasse nicht gefunden werdenkonnte
	 * @throws SQLException bei Fehlern beim Aufbau der Connection
	 */
	public static synchronized Connection getConnection(String host, String user, String passwort) throws ClassNotFoundException, SQLException {
		Connection connection = null;
		if (poolConnection == null) {
			// keine Connection verfügbar
			Class.forName(ConnectionManager.mysqlJdbcDriverClass );
			String mysqlJdbcConnectionUrl = mysqlJdbcConnectionProtocol + host + database + "?zeroDateTimeBehavior=convertNull";
			connection = DriverManager.getConnection(mysqlJdbcConnectionUrl, user, passwort);
			if(log)
				System.out.println("Neue Connection für \"" + user + "\" erzeugt " + sdf.format(new Date()));
		} else {
			// es gibt eine freie Connection, die genommen wird
			connection = poolConnection;
			poolConnection = null;
			if(log)
				System.out.println("Vorhandene Connection verwendet " + sdf.format(new Date()));
		}
		return connection;
	}
	/**
	 * Gibt eine genutzte Connection an den Pool (mit einer Connection) zurück.
	 * @param connection Zuvor genutztes Connection-Objekt, das zurückgegeben
	 * werden soll.
	 */
	public static synchronized void releaseConnection(Connection connection) {
		if (poolConnection == null){
			// keine Connection verfügbar, die aktuelle wird verfügbar gemacht
			poolConnection = connection;
			if(log) {
				System.out.println("Connection zurückgegeben " + sdf.format(new Date()));
			}
		} else {
			// eine Connection ist verfügbar, die aktuelle wird geschlossen
			if (connection != null){
				try {
					connection.close();
					if(log) {
						System.out.println("Connection geschlossen " + sdf.format(new Date()));
					}
				} catch (SQLException sqle) {
					System.out.println("ConnectionManager.releaseConnection: " + sqle.getMessage());
				}
			}
		}
	}
	/**
	 * Schließt die verfügbare Connection im ConnectionPool.
	 */
	public static synchronized void closeConnection() {
		if(poolConnection != null)
			try {
				poolConnection.close();
				if(log) {
					System.out.println("PoolConnection geschlossen " + sdf.format(new Date()));
				}
			} catch (SQLException sqle) {
				System.out.println("ConnectionManager.closeConnection: " + sqle.getMessage());
			}
	}
}