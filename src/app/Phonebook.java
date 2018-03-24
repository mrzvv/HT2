package app;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import util.DBWorker;

public class Phonebook {

	// Хранилище записей о людях.
	private HashMap<String, Person> persons = new HashMap<String, Person>();

	// Объект для работы с БД.
	private DBWorker db = DBWorker.getInstance();

	// Указатель на экземпляр класса.
	private static Phonebook instance = null;

	// Метод для получения экземпляра класса (реализован Singleton).
	public static Phonebook getInstance() throws ClassNotFoundException, SQLException {
		if (instance == null) {
			instance = new Phonebook();
		}

		return instance;
	}

	// При создании экземпляра класса из БД извлекаются все записи.
	protected Phonebook() throws ClassNotFoundException, SQLException {
		ResultSet db_data = this.db.getDBData("SELECT * FROM `person` ORDER BY `surname` ASC");
		while (db_data.next()) {
			this.persons.put(db_data.getString("id"), new Person(db_data.getString("id"), db_data.getString("name"),
					db_data.getString("surname"), db_data.getString("middlename")));
		}
	}

	// Добавление записи о человеке.
	public boolean addPerson(Person person) {
		String query;

		// У человека может не быть отчества.
		if (!person.getSurname().equals("")) {
			query = "INSERT INTO `person` (`name`, `surname`, `middlename`) VALUES ('" + person.getName() + "', '"
					+ person.getSurname() + "', '" + person.getMiddlename() + "')";
			try {
				java.sql.Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/phonebook",
						"root", "root");
				Statement st = connection.createStatement();
				st.executeUpdate(query);

				String sql = "SELECT `id` FROM `person` WHERE `name`='" + person.getName() + "' AND `surname`='"
						+ person.getSurname() + "' AND `middlename`='" + person.getMiddlename() + "'";
				ResultSet db_data2 = DBWorker.getInstance().getDBData(sql);
				while (db_data2.next()) {
					person.setId(db_data2.getString("id"));
					this.persons.put(db_data2.getString("id"), person);
					return true;
				}
			} catch (SQLException e) {
				System.out.println("[An error has occured under trying to add new person with middlename]");
			}
		} else {
			query = "INSERT INTO `person` (`name`, `surname`) VALUES ('" + person.getName() + "', '"
					+ person.getSurname() + "')";
			try {
				java.sql.Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/phonebook",
						"root", "root");
				Statement st = connection.createStatement();
				st.executeUpdate(query);

				String sql = "SELECT `id` FROM `person` WHERE `name`='" + person.getName() + "' AND `surname`='"
						+ person.getSurname() + "' AND `middlename`='" + person.getMiddlename() + "'";
				ResultSet db_data2 = DBWorker.getInstance().getDBData(sql);
				while (db_data2.next()) {
					person.setId(db_data2.getString("id"));
					this.persons.put(db_data2.getString("id"), person);
					return true;
				}
			} catch (SQLException e) {
				System.out.println("[An error has occured under trying to add new person with middlename]");
			}
		}
		return false;
	}
	
	// Обновление записи о человеке.
	public boolean updatePerson(String id, Person person) {
		Integer id_filtered = Integer.parseInt(person.getId());
		String query = "";

		// У человека может не быть отчества.
		if (!person.getSurname().equals("")) {
			query = "UPDATE `person` SET `name` = '" + person.getName() + "', `surname` = '" + person.getSurname()
					+ "', `middlename` = '" + person.getMiddlename() + "' WHERE `id` = " + id_filtered;
			try {
				java.sql.Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/phonebook",
						"root", "root");
				Statement st = connection.createStatement();
				st.executeUpdate(query);
				this.persons.put(person.getId(), person);
				return true;
			} catch (SQLException e) {
				System.out.println("[An error has occured under trying to add new person with middlename]");
			}
		} else {
			query = "UPDATE `person` SET `name` = '" + person.getName() + "', `surname` = '" + person.getSurname()
					+ "' WHERE `id` = " + id_filtered;
			try {
				java.sql.Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/phonebook",
						"root", "root");
				Statement st = connection.createStatement();
				st.executeUpdate(query);
				this.persons.put(person.getId(), person);
				return true;
			} catch (SQLException e) {
				System.out.println("[An error has occured under trying to add new person without middlename]");
			}
		}
		return false;
	}

	// Удаление записи о человеке.
	public boolean deletePerson(String id) {
		if ((id != null) && (!id.equals("null"))) {
			int filtered_id = Integer.parseInt(id);

			// Integer affected_rows = this.db.changeDBData("DELETE FROM
			// `person` WHERE `id`=" + filtered_id);
			try {
				java.sql.Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/phonebook",
						"root", "root");
				Statement st = connection.createStatement();
				st.executeUpdate("DELETE FROM `person` WHERE `id`=" + filtered_id);
				this.persons.remove(id);
				return true;
			} catch (SQLException e) {
				System.out.println("[An error has occured under trying to delete a person with id " + id + "]");
			}
		}
		return false;
	}
	
	public boolean addPhone(Person person, String newPhone) {
		ResultSet db_data = DBWorker.getInstance().getDBData("SELECT * FROM `phone` WHERE `owner`=" + person.getId());
		HashMap<String, String> phones = new HashMap<>();
		try {
			if (db_data != null) {
				while (db_data.next()) {
					phones.put(db_data.getString("id"), db_data.getString("number"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		int id_filtered = Integer.parseInt(person.getId());
		try {
			java.sql.Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/phonebook",
					"root", "root");
			Statement st = connection.createStatement();
			st.execute("INSERT INTO `phone` (`number`, `owner`) VALUES ('" + newPhone + "', '" + id_filtered + "')");
			String sql = "SELECT `id` FROM `phone` WHERE `owner`='" + id_filtered + "' AND `number`='" + newPhone + "'";
			ResultSet db_dataEdited = DBWorker.getInstance().getDBData(sql);
			while (db_dataEdited.next()) {
				phones.put(db_dataEdited.getString("id"), newPhone);
				person.setPhones(phones);
				this.persons.put(person.getId(), person);
				return true;
			}
		} catch (SQLException e) {
			System.out.println("[An error has occured under trying to add new phone]");
		}
		return false;
	}
	
	public boolean editPhone(String phoneId, Person personWhosePhoneToEdit, String phone) {
		ResultSet db_data = DBWorker.getInstance()
				.getDBData("SELECT * FROM `phone` WHERE `owner`=" + personWhosePhoneToEdit.getId());
		HashMap<String, String> phones = new HashMap<>();
		try {
			if (db_data != null) {
				while (db_data.next()) {
					phones.put(db_data.getString("id"), db_data.getString("number"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		int filteredPhoneId = Integer.parseInt(phoneId);

		try {
			java.sql.Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/phonebook",
					"root", "root");
			Statement st = connection.createStatement();
			st.executeUpdate("UPDATE `phone` SET `number` = '" + phone + "' WHERE `id` = " + filteredPhoneId);
			phones.put(phoneId, phone);
			personWhosePhoneToEdit.setPhones(phones);
			return true;
		} catch (SQLException e) {
			System.out.println("[An error has occured under trying to edit a number with id " + filteredPhoneId + "]");
		}
		return false;
	}

	public boolean deletePhone(String phoneId, Person personWhosePhoneToDelete) {
		if ((phoneId != null) && (!phoneId.equals("null"))) {
			int filteredPhoneId = Integer.parseInt(phoneId);
			HashMap<String, String> phones = personWhosePhoneToDelete.getPhones();

			try {
				java.sql.Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/phonebook",
						"root", "root");
				Statement st = connection.createStatement();
				st.executeUpdate("DELETE FROM `phone` WHERE `id`=" + filteredPhoneId);
				phones.remove(phoneId);
				return true;
			} catch (SQLException e) {
				System.out.println(
						"[An error has occured under trying to delete a number with id " + filteredPhoneId + "]");
			}
		}
		return false;
	}

	// +++++++++++++++++++++++++++++++++++++++++
	// Геттеры и сеттеры
	public HashMap<String, Person> getContents() {
		return persons;
	}

	public Person getPerson(String id) {
		return this.persons.get(id);
	}

	public HashMap<String, String> getPhones(String id) {
		Person p = this.persons.get(id);
		return p.getPhones();
	}
	// Геттеры и сеттеры
	// -----------------------------------------

}
