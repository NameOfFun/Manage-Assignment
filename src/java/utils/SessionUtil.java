/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

/**
 *
 * @author PC
 */
public class SessionUtil {
    
    // Check if there is a session
    public static User validateSession(HttpServletRequest request, HttpServletResponse response){
        HttpSession session = request.getSession(false);
        if(session != null){
            User user = (User) session.getAttribute("user");
            return user != null ? user : null;
        } else {
            return null;
        }
    }
    
    // Check if a session exists AND then if that user has the right permissionsS
    public static User validateAdmin(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if(session != null){
            User user = (User) session.getAttribute("user");
            return user != null && user.getRoleId() >= 2 ? user : null;
        } else {
            return null;
        }
    }
}
