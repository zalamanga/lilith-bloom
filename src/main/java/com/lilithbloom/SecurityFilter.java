package com.lilithbloom;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

@WebFilter("/*")
public class SecurityFilter implements Filter {
  public static String token(HttpSession session) {
    synchronized (session) {
      if (session.getAttribute("csrf") == null) {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        session.setAttribute("csrf", Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
      }
      return (String) session.getAttribute("csrf");
    }
  }

  public static boolean validToken(String expected, String supplied) {
    return expected != null
        && supplied != null
        && supplied.length() < 100
        && MessageDigest.isEqual(
            expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public void doFilter(ServletRequest input, ServletResponse output, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) input;
    HttpServletResponse response = (HttpServletResponse) output;
    request.setCharacterEncoding("UTF-8");
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("X-Frame-Options", "DENY");
    response.setHeader("Referrer-Policy", "same-origin");
    response.setHeader(
        "Content-Security-Policy",
        "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; object-src 'none'; base-uri 'self'; form-action 'self'; frame-ancestors 'none'");
    String path = request.getServletPath();
    boolean resource = path.startsWith("/javax.faces.resource/") || path.startsWith("/resources/");
    if (!resource) {
      response.setHeader("Cache-Control", "no-store");
      HttpSession session = request.getSession(true);
      String csrf = token(session);
      if ("POST".equals(request.getMethod())
          && (!validToken(csrf, request.getParameter("csrf"))
              || (path.endsWith(".xhtml")
                  && request.getParameter("javax.faces.ViewState") == null))) {
        response.sendError(403, "Form kedaluwarsa. Muat ulang halaman lalu coba lagi.");
        return;
      }
      boolean protectedPage = path.equals("/reservations.xhtml") || path.equals("/logout");
      if (protectedPage && session.getAttribute("accountId") == null) {
        response.sendRedirect(request.getContextPath() + "/login.xhtml");
        return;
      }
    }
    chain.doFilter(request, response);
  }
}
