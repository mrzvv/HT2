package app;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
//import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ManagePersonServlet extends HttpServlet {

	// Идентификатор для сериализации/десериализации.
	private static final long serialVersionUID = 1L;

	// Основной объект, хранящий данные телефонной книги.
	private Phonebook phonebook;

	public ManagePersonServlet() {
		// Вызов родительского конструктора.
		super();

		// Создание экземпляра телефонной книги.
		try {
			this.phonebook = Phonebook.getInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Валидация ФИО и генерация сообщения об ошибке в случае невалидных данных.
	private static String validatePersonFMLName(Person person) {
		String error_message = "";

		if (!Person.validateFMLNamePart(person.getName(), false)) {
			error_message += "Имя должно быть строкой от 1 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br/>";
		}

		if (!Person.validateFMLNamePart(person.getSurname(), false)) {
			error_message += "Фамилия должна быть строкой от 1 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br/>";
		}

		if (!Person.validateFMLNamePart(person.getMiddlename(), true)) {
			error_message += "Отчество должно быть строкой от 0 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br/>";
		}
		return error_message;
	}

	// Реакция на GET-запросы.
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		request.setAttribute("phonebook", this.phonebook);

		HashMap<String, String> jsp_parameters = new HashMap<String, String>();

		RequestDispatcher dispatcher_for_list = request.getRequestDispatcher("/List.jsp");
		RequestDispatcher dispatcher_for_addingPerson = request.getRequestDispatcher("/AddPerson.jsp");
		RequestDispatcher dispatcher_for_editingPerson = request.getRequestDispatcher("/EditPerson.jsp");
		RequestDispatcher dispatcher_for_addingPhone = request.getRequestDispatcher("/AddPhone.jsp");
		RequestDispatcher dispatcher_for_editingPhone = request.getRequestDispatcher("/EditPhone.jsp");

		String action = request.getParameter("action");
		String personId = request.getParameter("id");
		String phoneId = request.getParameter("phoneId");

		// Если идентификатор и действие не указаны, мы находимся в состоянии
		// "просто показать список и больше ничего не делать".
		if ((action == null) && (personId == null)) {
			request.setAttribute("jsp_parameters", jsp_parameters);
			dispatcher_for_list.forward(request, response);
		}
		// Если же действие указано, то...
		else {
			switch (action) {

			case "addPerson":
				Person empty_person = new Person();

				jsp_parameters.put("current_action", "addPerson");
				jsp_parameters.put("next_action", "addPersonPOST");
				jsp_parameters.put("next_action_label", "Добавить");

				request.setAttribute("person", empty_person);
				request.setAttribute("jsp_parameters", jsp_parameters);
				dispatcher_for_addingPerson.forward(request, response);
				break;

			case "editPerson":
				Person editable_person = this.phonebook.getPerson(personId);

				jsp_parameters.put("current_action", "editPerson");
				jsp_parameters.put("next_action", "editPersonPOST");
				jsp_parameters.put("next_action_label", "Сохранить");

				request.setAttribute("person", editable_person);
				request.setAttribute("jsp_parameters", jsp_parameters);
				dispatcher_for_editingPerson.forward(request, response);
				break;

			case "deletePerson":
				// Если запись удалось удалить...
				if (phonebook.deletePerson(personId)) {
					jsp_parameters.put("current_action_result", "DELETION_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Удаление выполнено успешно");
				} else {
					jsp_parameters.put("current_action_result", "DELETION_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка удаления (возможно, запись не найдена)");
				}

				request.setAttribute("jsp_parameters", jsp_parameters);
				dispatcher_for_list.forward(request, response);
				break;

			case "addPhone":
				Person personWhosePhoneToAdd = this.phonebook.getPerson(personId);

				jsp_parameters.put("current_action", "addPhone");
				jsp_parameters.put("next_action", "addPhonePOST");
				jsp_parameters.put("next_action_label", "Добавить номер");

				request.setAttribute("person", personWhosePhoneToAdd);
				request.setAttribute("jsp_parameters", jsp_parameters);
				dispatcher_for_addingPhone.forward(request, response);
				break;

			case "editPhone":
				Person personWhoseNumberToEdit = this.phonebook.getPerson(personId);

				jsp_parameters.put("current_action", "editPhone");
				jsp_parameters.put("next_action", "editPhonePOST");
				jsp_parameters.put("next_action_label", "Сохранить номер");

				request.setAttribute("person", personWhoseNumberToEdit);
				request.setAttribute("jsp_parameters", jsp_parameters);
				request.setAttribute("phoneId", phoneId);
				dispatcher_for_editingPhone.forward(request, response);
				break;

			case "deletePhone":
				Person personWhosePhoneToDelete = this.phonebook.getPerson(personId);
				// Если number удалось удалить...
				if (phonebook.deletePhone(phoneId, personWhosePhoneToDelete)) {
					jsp_parameters.put("current_action_result", "DELETION_SUCCESS");
					jsp_parameters.put("next_action_label", "Сохранить");
					jsp_parameters.put("next_action", "editPersonPOST");
					jsp_parameters.put("current_action_result_label", "Удаление выполнено успешно");
				} else {
					jsp_parameters.put("current_action_result", "DELETION_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка удаления (возможно, запись не найдена)");
				}

				request.setAttribute("jsp_parameters", jsp_parameters);
				request.setAttribute("person", personWhosePhoneToDelete);
				dispatcher_for_editingPerson.forward(request, response);
				break;
			}
		}
	}

	// Реакция на POST-запросы.
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		request.setAttribute("phonebook", this.phonebook);

		HashMap<String, String> jsp_parameters = new HashMap<String, String>();

		RequestDispatcher dispatcher_for_list = request.getRequestDispatcher("/List.jsp");
		RequestDispatcher dispatcher_for_addingPerson = request.getRequestDispatcher("/AddPerson.jsp");
		RequestDispatcher dispatcher_for_editingPerson = request.getRequestDispatcher("/EditPerson.jsp");
		RequestDispatcher dispatcher_for_addingPhone = request.getRequestDispatcher("/AddPhone.jsp");
		RequestDispatcher dispatcher_for_editingPhone = request.getRequestDispatcher("/EditPhone.jsp");

		String addPersonPOST = request.getParameter("addPersonPOST");
		String editPersonPOST = request.getParameter("editPersonPOST");
		String editPhonePOST = request.getParameter("editPhonePOST");
		String addPhonePOST = request.getParameter("addPhonePOST");
		String personId = request.getParameter("id");
		String phoneId = request.getParameter("phoneId");

		if (addPersonPOST != null) {
			Person new_person = new Person(request.getParameter("name"), request.getParameter("surname"),
					request.getParameter("middlename"));
			String error_message = validatePersonFMLName(new_person);
			if (error_message.equals("")) {
				if (this.phonebook.addPerson(new_person)) {
					jsp_parameters.put("current_action_result", "ADDITION_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Добавление выполнено успешно");
				} else {
					jsp_parameters.put("current_action_result", "ADDITION_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка добавления");
				}

				request.setAttribute("jsp_parameters", jsp_parameters);
				dispatcher_for_list.forward(request, response);
			} else {
				jsp_parameters.put("current_action", "addPerson");
				jsp_parameters.put("next_action", "addPersonPOST");
				jsp_parameters.put("next_action_label", "Добавить");
				jsp_parameters.put("error_message", error_message);

				request.setAttribute("person", new_person);
				request.setAttribute("jsp_parameters", jsp_parameters);
				dispatcher_for_addingPerson.forward(request, response);
			}
		}

		if (editPersonPOST != null) {
			Person updatable_person = this.phonebook.getPerson(request.getParameter("id"));
			String error_message = "";

			if (!Person.validateFMLNamePart(request.getParameter("name"), false)) {
				error_message += "Имя должно быть строкой от 1 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
				System.out.println(request.getParameter("name") + ": "
						+ Person.validateFMLNamePart(request.getParameter("name"), false));
			} else if (!Person.validateFMLNamePart(request.getParameter("surname"), false)) {
				error_message += "Фамилия должна быть строкой от 1 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
				System.out.println(request.getParameter("surname") + ": "
						+ Person.validateFMLNamePart(request.getParameter("surname"), false));
			} else if (!Person.validateFMLNamePart(request.getParameter("middlename"), true)) {
				error_message += "Отчество должно быть строкой от 0 до 150 символов из букв, цифр, знаков подчёркивания и знаков минус.<br />";
				System.out.println(request.getParameter("middlename") + ": "
						+ Person.validateFMLNamePart(request.getParameter("middlename"), true));
			} else {
				error_message = "";
				updatable_person.setName(request.getParameter("name"));
				updatable_person.setSurname(request.getParameter("surname"));
				updatable_person.setMiddlename(request.getParameter("middlename"));
			}

			if (error_message.equals("")) {
				if (this.phonebook.updatePerson(personId, updatable_person)) {
					jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
					jsp_parameters.put("current_action_result_label", "Обновление выполнено успешно");
				} else {
					jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка обновления");
				}

				request.setAttribute("jsp_parameters", jsp_parameters);
				dispatcher_for_list.forward(request, response);
			} else {
				jsp_parameters.put("current_action", "editPerson");
				jsp_parameters.put("next_action", "editPersonPOST");
				jsp_parameters.put("next_action_label", "Сохранить");
				jsp_parameters.put("error_message", error_message);

				request.setAttribute("person", updatable_person);
				request.setAttribute("jsp_parameters", jsp_parameters);
				dispatcher_for_editingPerson.forward(request, response);
			}
		}

		if (addPhonePOST != null) {
			Person personWhosePhoneToAdd = this.phonebook.getPerson(request.getParameter("id"));
			String phone = request.getParameter("phone");
			String error_message = "";

			if (!Person.validatePhone(phone)) {
				error_message = "Телефон должен содержать от 2 до 50 символов, включая цифры и знаки: [+] [-] [#]";
			}

			if (error_message.equals("")) {
				if (this.phonebook.addPhone(personWhosePhoneToAdd, phone)) {
					jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
					jsp_parameters.put("next_action_label", "Сохранить");
					jsp_parameters.put("next_action", "editPersonPOST");
					jsp_parameters.put("current_action_result_label", "Обновление выполнено успешно");
				} else {
					jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка обновления");
				}
				request.setAttribute("jsp_parameters", jsp_parameters);
				request.setAttribute("person", personWhosePhoneToAdd);
				dispatcher_for_editingPerson.forward(request, response);
			} else {
				jsp_parameters.put("current_action", "addPhone");
				jsp_parameters.put("next_action", "addPhonePOST");
				jsp_parameters.put("next_action_label", "Добавить номер");
				jsp_parameters.put("error_message", error_message);

				request.setAttribute("person", personWhosePhoneToAdd);
				request.setAttribute("jsp_parameters", jsp_parameters);
				dispatcher_for_addingPhone.forward(request, response);
			}
		}

		if (editPhonePOST != null) {
			// Получение записи и её обновление на основе данных из формы.
			Person personWhosePhoneToEdit = this.phonebook.getPerson(request.getParameter("id"));
			String phone = request.getParameter("phone");
			String error_message = "";

			if (!Person.validatePhone(phone)) {
				error_message = "Телефон должен содержать от 2 до 50 символов, включая цифры и знаки: [+] [-] [#]";
			}

			if (error_message.equals("")) {
				// Если запись удалось обновить...
				if (this.phonebook.editPhone(phoneId, personWhosePhoneToEdit, phone)) {
					jsp_parameters.put("current_action_result", "UPDATE_SUCCESS");
					jsp_parameters.put("next_action_label", "Сохранить");
					jsp_parameters.put("next_action", "editPersonPOST");
					jsp_parameters.put("current_action_result_label", "Обновление выполнено успешно");
				} else {
					jsp_parameters.put("current_action_result", "UPDATE_FAILURE");
					jsp_parameters.put("current_action_result_label", "Ошибка обновления");
				}
				request.setAttribute("jsp_parameters", jsp_parameters);
				request.setAttribute("person", personWhosePhoneToEdit);
				dispatcher_for_editingPerson.forward(request, response);
			} else {
				jsp_parameters.put("current_action", "editPhone");
				jsp_parameters.put("next_action", "editPhonePOST");
				jsp_parameters.put("next_action_label", "Сохранить номер");
				jsp_parameters.put("error_message", error_message);

				request.setAttribute("person", personWhosePhoneToEdit);
				request.setAttribute("jsp_parameters", jsp_parameters);
				request.setAttribute("phoneId", phoneId);

				dispatcher_for_editingPhone.forward(request, response);
			}
		}
	}
}
