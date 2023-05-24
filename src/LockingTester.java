/**
 * This is a sample class that demonstrates concurrency control
 * in three different ways: optimistic locking, pessimistic locking,
 * and setting the isolation levels in the database itself.
 *
 * @author Ken Ritley based on Stefan Fischli's wonderful idea and code!
 * @since 2023-05-24
 * @version 1.1
 */
import java.util.Scanner; // used to prompt the user for input between tests

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;

public class LockingTester {

	private static final String DB_URL = "jdbc:postgresql://127.0.0.1:5431/postgres";
	private static final String DB_USERNAME = "postgres";
	private static final String DB_PASSWORD = "kenpostgres";

	public static void main(String[] args) throws Exception {
		initDatabase();
		Scanner scanner = new Scanner(System.in);
		System.out.println("\nPress RETURN to start the test");
		scanner.nextLine();
	
		System.out.println("TEST: updateWithoutLocking");
		System.out.println("--------------------");
		for (int i = 0; i < 2; i++) {
			new Thread(LockingTester::updateWithoutLocking).start();
		}

		Thread.sleep(3000);
		System.out.println("\nPress RETURN to start the next test");
		scanner.nextLine();
		System.out.println("--------------------");
		System.out.println("TEST: updateWithOptimistictLocking");
		for (int i = 0; i < 2; i++) {
			new Thread(LockingTester::updateWithOptimisticLocking).start();
		}


		Thread.sleep(3000);
		System.out.println("\nPress RETURN to start the next test");
		scanner.nextLine();
		System.out.println("--------------------");
		System.out.println("TEST: updateWithIsolationLevel");
		for (int i = 0; i < 2; i++) {
			new Thread(LockingTester::updateWithIsolationLevel).start();
		}

		Thread.sleep(3000);
		System.out.println("\nPress RETURN to start the next test");
		scanner.nextLine();
		System.out.println("--------------------");
		System.out.println("TEST: updateWithPessimisticLocking");
		for (int i = 0; i < 2; i++) {
			new Thread(LockingTester::updateWithPessimisticLocking).start();
		}

		scanner.close();
		System.out.println("all done.");
	}

	private static void initDatabase() throws Exception {
		Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
		Statement statement = connection.createStatement();
		statement.executeUpdate("DROP TABLE IF EXISTS DATA");
		statement.executeUpdate("CREATE TABLE DATA (ID INTEGER, VALUE VARCHAR(255), VERSION INTEGER, PRIMARY KEY (ID))");
		statement.executeUpdate("INSERT INTO DATA VALUES (1, '$', 0)");
		connection.close();
	}

	/**
 	 * Have a good look at the SQL statement used to update the table. It is just
	 * a normal SELECT followed by a normal UPDATE, e.g. exactly what you would
	 * do on an SQL console to update the table.  No concurrency control!
	 */
	private static void updateWithoutLocking() {
		String threadName = Thread.currentThread().getName();
		try {
			// get connection
			Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
			connection.setAutoCommit(false);

			// read value
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM DATA WHERE ID=1");
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			String value = resultSet.getString("VALUE");
			System.out.println(threadName + " Value read: " + value);

			// increment value
			Thread.sleep(1000);
			value = value + '$';

			// write Value
			statement = connection.prepareStatement("UPDATE DATA SET VALUE=? WHERE ID=1");
			statement.setString(1, value);
			int rows = statement.executeUpdate();
			if (rows > 0)
				System.out.println(threadName + " Value written: " + value);
			else System.out.println(threadName + " Update failed");

			// commit transaction
			connection.commit();
			connection.close();
		} catch (Exception ex) {
			System.out.println(threadName + " Update failed: " + ex.getMessage());
		}
	}

	/**
 	 * Have a good look at the SQL statement used to update the table. It
	 * not only reads the data via a SELECT statement but it also reads a version
	 * number. Then, the UPDATE statement is carefully constructed so that two
	 * things happen: (1) the update only occurs if the version number has not
	 * been changed since it was read; (2) the new version print to the database
	 * is incremented.
	 * 
	 */
	private static void updateWithOptimisticLocking() {
		String threadName = Thread.currentThread().getName();
		try {
			// get connection
			Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
			connection.setAutoCommit(false);

			// read value and version
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM DATA WHERE ID=1");
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			String value = resultSet.getString("VALUE");
			int version = resultSet.getInt("VERSION");
			System.out.println(threadName + " Value read: " + value);

			// increment value
			Thread.sleep(1000);
			value = value + '$';

			// write Value
			statement = connection.prepareStatement("UPDATE DATA SET VALUE=?, VERSION=? WHERE ID=1 AND VERSION=?");
			statement.setString(1, value);
			statement.setInt(2, version + 1);
			statement.setInt(3, version);

			int rows = statement.executeUpdate();
			if (rows > 0)
				System.out.println(threadName + " Value written: " + value);
			else System.out.println( threadName + " Update failed");

			// commit transaction
			connection.commit();
			connection.close();
		} catch (Exception ex) {
			System.out.println(threadName + " Update failed: " + ex.getMessage());
		}
	}

	/**
 	 * Have a good look at the SQL statement used to update the table. It
	 * not only reads the data via a SELECT statement but it uses the
	 * FOR UPDATE clause. This tells the database we are planning an update
	 * soon, so the database should not allow any changes to that data unless
	 * those changes come from us!
	 */
	private static void updateWithPessimisticLocking() {
		String threadName = Thread.currentThread().getName();
		try {
			// get connection
			Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
			connection.setAutoCommit(false);

			// read value
			PreparedStatement statement = connection.prepareStatement(
					"SELECT * FROM DATA WHERE ID=1 FOR UPDATE", TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE);
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			String value = resultSet.getString("VALUE");
			System.out.println(threadName + " Value read: " + value);

			// increment value
			Thread.sleep(1000);
			value = value + '$';

			// write Value
			resultSet.updateString("VALUE", value);
			resultSet.updateRow();
			System.out.println(threadName + " Value written: " + value);

			// commit transaction
			connection.commit();
			connection.close();
		} catch (Exception ex) {
			System.out.println(threadName + " Update failed: " + ex.getMessage());
		}
	}

	/**
 	 * Have a good look at the SQL statement used to update the table. It
	 * is a normal SELECT statement. But, just after the database connection
	 * was created, the setTransactionIsolation() method was invoked with
	 * the parameter TRANSACTION_SERIALIZABLE, which is the strongest possible
	 * isolation level. This means the database itself will take precautions
	 * if it detects there are two or more transactions in progress at the same
	 * time!
	 */
	private static void updateWithIsolationLevel() {
		String threadName = Thread.currentThread().getName();
		try {
			// get connection
			Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
			connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			connection.setAutoCommit(false);

			// read value
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM DATA WHERE ID=1");
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			String value = resultSet.getString("VALUE");
			System.out.println(threadName + " Value read: " + value);

			// increment value
			Thread.sleep(1000);
			value = value + '$';

			// write Value
			statement = connection.prepareStatement("UPDATE DATA SET VALUE=? WHERE ID=1");
			statement.setString(1, value);
			int rows = statement.executeUpdate();
			if (rows > 0)
				System.out.println(threadName + " Value written: " + value);
			else System.out.println(threadName + " Update failed");

			// commit transaction
			connection.commit();
			connection.close();
		} catch (Exception ex) {
			System.out.println(threadName + " Update failed: " + ex.getMessage());
		}
	}
}
