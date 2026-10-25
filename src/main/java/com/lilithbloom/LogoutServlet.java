package com.lilithbloom;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    HttpSession session = request.getSession(false);
    if (session != null) session.invalidate();
    response.sendRedirect(request.getContextPath() + "/login.xhtml?logout=1");
  }
}
