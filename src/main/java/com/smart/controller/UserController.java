package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String username = principal.getName();
		System.out.println("USERNAME: "+username);
		
		//get the user using username(email)
		
		User user = userRepository.getUserByUserName(username);
		System.out.println("USER: "+user);
		
		model.addAttribute("user", user);
	}
	
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		
		model.addAttribute("title", "User Dashboard");
	
		return "normal/user_dashboard";
	}
	
	
	//open add contact handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	//process-contact Handler which is action by add_contact_form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			//processing and uploading file
			if(file.isEmpty()) {
				//if the file is empty then try our message
				System.out.println("file is empty");
				contact.setImage("contact.png");
				
			}else {
				//upload the file to folder and update the name to contact
			contact.setImage(file.getOriginalFilename());
			
			File saveFile = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image is uploaded");
			}
			
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			System.out.println("added to the db");
			//alert message success in add-contact form
			session.setAttribute("message", new Message("Your contact is added !! Add more..","success"));
			
			
			System.out.println("Data" +contact);
			
		} catch (Exception e) {
			e.printStackTrace();
			//alert message gave if failed
			session.setAttribute("message", new Message("something went wrong !! Try again..","danger"));
		}
		
		return "normal/add_contact_form";
	}
	
	//show or view contacts handler
	//per page = 5[n]
	//current page = 0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model m,Principal principal) {
		m.addAttribute("title", "View all user Contacts");
		//all contact lists dekhaune
		
		/* yesari garda ni hunxa
		String userName = principal.getName();
		
		User user = this.userRepository.getUserByUserName(userName);
		List<Contact> contacts = user.getContacts();
		 */
		
		/* or contactRepository baata garna ni milne */
		String username = principal.getName();
		
		
		User user = this.userRepository.getUserByUserName(username);
		//pageable interface has 2 info: current page
		//& contact per page - 5
		Pageable pageable = PageRequest.of(page,3);
		
		Page<Contact> contacts = this.contactRepository.findContactsbyUser(user.getId(),pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		
		return "normal/show_contacts";
		
	}
	
	//showing particular showContactDetails
	@RequestMapping("/{cid}/contact")
	public String showContactDetail(@PathVariable("cid") Integer cid,Model model,Principal principal) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		
		//solving security Bug
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		
		
		System.out.println("CID "+cid);
		return "normal/contact_detail";
	}
	
	//delete contact handler(show contactko delete button ko kaam)
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid,
			Principal principal,Model model,HttpSession session) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		
		User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		
		session.setAttribute("message", new Message("contact deleted successfully....","success"));
		
		return "redirect:/user/show-contacts/0";
	}
	
	//open update handler in viewcontacts
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model m) {
		
		m.addAttribute("title", "update contact");
		
		Contact contact = this.contactRepository.findById(cid).get();
		
		m.addAttribute("contact", contact);
		return "normal/update_form";
	}
	
	//update contact handler
	@RequestMapping(value="/process-update", method=RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model m, HttpSession session,Principal principal) {
		
		try {
			//old contact detaails fetch gareko
			Contact oldContactDetail = this.contactRepository.findById(contact.getCid()).get();
			//image
			if(!file.isEmpty()) {
				//file work..
				//rewrite
				
				//delete old photo
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1= new File(deleteFile,oldContactDetail.getImage());
				file1.delete();
				
				//update new photo
				
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				
			}else {
				contact.setImage(oldContactDetail.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("your contact is updated.....","success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Contact Name: "+contact.getName());
		System.out.println("Contact id: "+contact.getCid());
		
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	
	//YOur profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
	
	//open setting handler
	@GetMapping("/settings")
	public String openSetting() {
		return "normal/settings";
	}
	
	
	//change pw handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("confirmNewPassword") String confirmNewPassword,
			Principal principal,HttpSession session)
	{
		System.out.println("Old Password: "+oldPassword);
		System.out.println("confirm New  Password: "+confirmNewPassword);
		
		String userName = principal.getName();
		System.out.println("userName: "+userName);
		User currentUser = this.userRepository.getUserByUserName(userName);
		System.out.println("Currentuser: "+currentUser);
		System.out.println(currentUser.getPassword());
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
		//change pw if old pw matches new pw
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(confirmNewPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your pw is successfully changed....","success"));
			
		}else {
			//error msg if not matches
			session.setAttribute("message", new Message("Please Enter Correct old Password....","danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
		
	}
}
