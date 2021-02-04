package com.smart.controller;

import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ForgotController {
	Random random = new Random(1000);//otp implemeting class
	
	
	//email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}
	
	//sending otp handler
		@PostMapping("/send-otp")
		public String sendOtp(@RequestParam("email") String email) {
		System.out.println("Email: "+email);
		//generating otp 4 digit value
		
		int otp = random.nextInt(999999);
		System.out.println("OTP: "+otp);
			
			return "verify_otp";
		}


}
