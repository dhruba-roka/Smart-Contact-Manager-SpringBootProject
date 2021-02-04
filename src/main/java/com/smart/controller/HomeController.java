package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	//to push the value in database from the registeration form
	//autowired-->object dincha userRepositoryko
	@Autowired
	private UserRepository userRepository;
	
	//handler for home
	@RequestMapping("/")	
	public String home(Model model) {
		
			model.addAttribute("title","Home-smart contact manager");
			return "home";
		}
	
	//handler for about
	@RequestMapping("/about")	
	public String about(Model model) {
		
			model.addAttribute("title","Register-smart contact manager");
			return "about";
		}
	
	//handler for signup 
	@RequestMapping("/signup")	
	public String signup(Model model) {
		
			model.addAttribute("title","About-smart contact manager");
			model.addAttribute("user", new User());
			return "signup";
		}
	
	//handler for registering user
	@RequestMapping(value="/do_register", method=RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1,
			@RequestParam(value="agreement",defaultValue="false") boolean agreement, Model model,
			HttpSession session) {
		
		try {
			//if you didnt check the terms and conditions
			if(!agreement) {
				System.out.println("you have not agreed the terms and conditions");
				throw new Exception("you have not agreed the terms and conditions"); 
			}
			
			if(result1.hasErrors()) {
				System.out.println("ERROR: "+result1.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			
			//setting the rest entities
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			
			System.out.println("agreement "+agreement);
			System.out.println("USER "+user);
			
			//saving the valuses of user in database
			User result = this.userRepository.save(user);
			
			model.addAttribute("user", 	new User());
			session.setAttribute("message", new Message("successfully registered !!" , "alert-success"));
			return "signup";
			
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("something went wrong !!" +e.getMessage(), "alert-danger"));
			return "signup";
		}
		
		
	}

	//handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login Page");
		return "login";
		
	}
}
