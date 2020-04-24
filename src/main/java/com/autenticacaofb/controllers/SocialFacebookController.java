package com.autenticacaofb.controllers;


import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.social.connect.Connection;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.User;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1")
@Api(value = "Facebook CONTROLLER")
public class SocialFacebookController {

    private FacebookConnectionFactory factory = new FacebookConnectionFactory("CLIENT_ID",
            "CLIENT_SECRET");


    @RequestMapping("/teste")
    public ModelAndView firstPage() {
        return new ModelAndView("welcome");
    }

    @GetMapping(value = "/useApplication")
    public String producer() {
        OAuth2Operations operations = factory.getOAuthOperations();
        OAuth2Parameters params = new OAuth2Parameters();

        params.setRedirectUri("http://localhost:8080/forwardLogin");
        params.setScope("email,public_profile");

        String url = operations.buildAuthenticateUrl(params);
        System.out.println("The URL is" + url);
        return "redirect:" + url;

    }

    @ApiOperation(value = "Valida Token Facebook")
    @RequestMapping(value = "/forwardLogin")
    public ModelAndView authenticateFacebook(@RequestParam(value = "code") String authorizationCode) {
        OAuth2Operations operations = factory.getOAuthOperations();
        AccessGrant accessToken = operations.exchangeForAccess(authorizationCode, "http://localhost:8080/forwardLogin",
                null);
        Connection<Facebook> connection = factory.createConnection(accessToken);
        Facebook facebook = connection.getApi();
//        String[] fields = { "id", "email", "first_name", "last_name" };
        String[] fields = {"email"};
        User userProfile = facebook.fetchObject("me", User.class, fields);
        ModelAndView model = new ModelAndView("details");
        model.addObject("user", userProfile);
        return model;
    }


    public boolean validarToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        JsonNode resp = null;
        try {
//            logger.info("VALIDANDO TOKEN - FB...");
            resp = restTemplate.getForObject("https://graph.facebook.com/debug_token?input_token=" + code + "&access_token=" + code, JsonNode.class);
            Boolean isValid = resp.path("data").findValue("is_valid").asBoolean();
//            logger.info(resp.toString());
            return isValid;
        } catch (Exception e) {
//            logger.info("ERRO AO VALIDAR TOKEN. VERIFIQUE SE A TOKEN É VALIDA: " + e.getMessage());
        }
        return false;
    }

}