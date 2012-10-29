package com.github.podd.prototype;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet to handle login to PODD prototype web service
 * 
 * @author kutila
 * @created 2012/10/26
 */
public class LoginServlet extends PoddBaseServlet {

	// Check whether this behaves as expected
    private static Properties passwords;
    private static long passwordsLoadedAt = -1;
       
    /**
     */
    public LoginServlet() {
        super();
    }

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException
    {
        PrintWriter out = response.getWriter();
        
        String httpMethod = request.getMethod();
        String username = null;
        
        if(HTTP_POST.equals(httpMethod))
        {
            username = request.getParameter("username");
            String password = request.getParameter("password");
            if(!checkCredentials(username, password))
            {
                log.info("Failed login attempt for " + username + "/" + password);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Login failed");
                return;
            }
            // create session and send SUCCESS response
            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(180); // invalidate session after 3 minutes
            session.setAttribute("user", username);
            out.write(username + " login successful\r\n");
            out.flush();
            out.close();
            return;
        }
        
    }

    /**
     * The prototype will use a file based authentication mechanism. All userid/password pairs are
     * periodically read from a Properties file.
     * 
     * @param userid
     * @param password
     * @return
     */
    private boolean checkCredentials(String userid, String password)
    {
        if(userid == null || password == null || userid.trim().length() < 1 || password.trim().length() < 1)
        {
            return false;
        }
        loadPasswordFile();
        if(passwords.containsKey(userid))
        {
            String expectedPassword = passwords.getProperty(userid);
            if(expectedPassword.equals(password))
            {
                return true;
            }
        }
        
        return false;
    }
    
    private void loadPasswordFile()
    {
        if((System.currentTimeMillis() - passwordsLoadedAt) < 60000)
        {
            return;
        }
        String passwordFile = getServletContext().getInitParameter("passwdfile");
        log.debug("The password file is located at : " + passwordFile);
        passwords = new Properties();
        try
        {
            passwords.load(new FileInputStream(passwordFile));
        }
        catch(Exception e)
        {
            log.error("Failed to load password file", e);
        }
        passwordsLoadedAt = System.currentTimeMillis();
    }
    
    
}
