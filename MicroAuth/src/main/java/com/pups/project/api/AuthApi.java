package com.pups.project.api;

import java.util.HashMap;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.pups.project.domain.CustomerCredentials;
import com.pups.project.domain.CustomerInformation;
import com.pups.project.jwt.JWTUtility;

@RestController
public class AuthApi {
	
	private String apiHost = System.getenv("API_HOST");
	private String customersAPIbase="http://"+apiHost+"/api/customers"; 	//API base
	private JWTUtility jwtUtility = new JWTUtility(); 						//JWTUtility used to create a token
	private RestTemplate restTemplate = new RestTemplate(); 				//RestTemplate used to make HTTP GET and POST requests
	private String customerAllowedScopes = "/api/customers /api/events /api/registrations";
		
	@PostMapping(path = "/token")
	public ResponseEntity<?> authenticateCredentials(@RequestBody(required = false) CustomerCredentials credentials) {
		
		System.out.print("asking at "+apiHost);
		
		//Check validity of provided data
		if(credentials.getName()==null || credentials.getPassword()==null || credentials.getName().length()==0 || credentials.getPassword().length()==0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		
		//Make a request to the database for all customers and cast the list as an array of CustomerCredentials
	    HttpHeaders headers = new HttpHeaders();
	    headers.setBearerAuth(jwtUtility.createToken("/api/customers"));
	    
	    // build the request
	    HttpEntity<?> entity = new HttpEntity<>(headers);
		
		ResponseEntity<?> customerResponse=restTemplate.exchange(customersAPIbase, HttpMethod.GET, entity,CustomerCredentials[].class);
		
		
		CustomerCredentials[] customers = (CustomerCredentials[]) customerResponse.getBody();
		
		//Do a foreach loop on the customers array to check if the provided credentials match any of the customer credentials
		for(CustomerCredentials checking:customers) {
			if (checking.getName().equals(credentials.getName()) && checking.getPassword().equals(credentials.getPassword())){
				
				//CREDENTIAL CHECK SUCCEEDS, generate and return a token
				String token = jwtUtility.createToken(customerAllowedScopes);
				HashMap<String, String> tokenJson = new HashMap<String, String>();
				tokenJson.put("token",token);
				ResponseEntity<?> response = ResponseEntity.ok(tokenJson);
				return response;
			}
		}
		
		//If the foreach check fails to verify credentials, return an UNAUTHORIZED http status code
		return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}
	
	@PostMapping(path = "/register")
	public ResponseEntity<?> registerUser(@RequestBody(required=false) CustomerInformation information){
		
		//Check validity of provided information
		if(information.getName()==null || information.getPassword()==null || information.getEmail()==null || information.getName().length()==0 || information.getPassword().length()==0 || information.getEmail().length()==0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		
	    // create header
	    HttpHeaders headers = new HttpHeaders();
	    headers.setBearerAuth(jwtUtility.createToken("/api/customers"));
	    
	    // build the request
	    HttpEntity<?> entity = new HttpEntity<>(information, headers);
	    
	    // send POST request
	    ResponseEntity<?> response = this.restTemplate.postForEntity(customersAPIbase, entity, CustomerInformation.class);
	    
	    // check response status code
	    if (response.getStatusCode() == HttpStatus.CREATED) {
	        return (ResponseEntity<?>) response.getBody();
	    } else {
	        return null;
	    }
	}
}
