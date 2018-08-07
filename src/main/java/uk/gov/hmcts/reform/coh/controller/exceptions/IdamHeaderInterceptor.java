package uk.gov.hmcts.reform.coh.controller.exceptions;

import gherkin.deps.com.google.gson.JsonObject;
import org.springframework.stereotype.Component; 
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter; 
 
import javax.servlet.http.HttpServletRequest; 
import javax.servlet.http.HttpServletResponse; 
import java.io.IOException; 
 
@Component 
public class IdamHeaderInterceptor extends HandlerInterceptorAdapter { 
 
    public static final String IDAM_AUTHOR_KEY = "IDAM_Author_ref";
 
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException { 
        response.setContentType("application/json"); 
        response.setCharacterEncoding("utf-8"); 
 
        if(request.getHeader(IDAM_AUTHOR_KEY) == null) {
            JsonObject jsonObject = new JsonObject(); 
            jsonObject.addProperty("error_message","Missing required IDAM header."); 
            response.getWriter().write(jsonObject.toString()); 
            response.setStatus(400); 
            return false; 
        } 
        return true; 
    } 
}