package dashboard;

import dto.ClassDTO;
import dto.PageDTO;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Setting;
import model.User;
import service.ClassService;
import utils.SessionUtil;

public class ExpertDashboard extends HttpServlet {

	private final ClassService service = new ClassService();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		User expert = SessionUtil.validateSession(request, response);

		PageDTO<ClassDTO> classes;
		List<Setting> subjects;
		try {
			classes = service.getAllClasses(null, null, null, null, expert.getId());
			subjects = service.getSubjectsForManage(expert.getId());

			request.setAttribute("classCount", classes.getItems().size());
			request.setAttribute("subjectCount", subjects.size());
			
			if(classes.getItems().size() > 6) {
				request.setAttribute("classes", classes.getItems().subList(0, 6));
			} else {
				request.setAttribute("classes", classes.getItems());
			}
			
			request.setAttribute("subjects", subjects);
		} catch (SQLException e) {
			request.setAttribute("error", "Cannot load the dashboard data from the database.");
		}

		request.getRequestDispatcher("/dashboard/expertdashboard.jsp").forward(request, response);
	}
}
