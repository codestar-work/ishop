package web;

import org.springframework.ui.*;
import org.springframework.boot.*;
import org.springframework.stereotype.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

@Controller
@SpringBootApplication
class Main {
	String shop = "iStud";
	
	@RequestMapping("/")
	String showHome(Model model) {
		model.addAttribute("shop", shop);
		return "index";
	}
	
	@RequestMapping(value="/login")
	String showLogIn(Model model) {
		model.addAttribute("shop", shop);
		return "login";
	}
	
	@RequestMapping(value="/login", method=RequestMethod.POST)
	String checkLogin(String username, String password,
			Model model) {
		if (username.equals("owner") && 
			password.equals("owner2017")) {
			return "redirect:/settings";
		} else {
			return "redirect:/login?message=Incorrect Password";
		}
	}
	
	@RequestMapping("/settings")
	String showSettings(Model model) {
		model.addAttribute("shop", shop);
		// ...
		return "settings";
	}
	
	@RequestMapping("/logout")
	String showLogOut(Model model) {
		model.addAttribute("shop", shop);
		// ...
		return "logout";
	}
	
	@RequestMapping("/status") @ResponseBody
	String status() {
		return "Server is OK";
	}
	
	
	public static void main(String [] args) {
		SpringApplication.run(Main.class, args);
	}	
	
}
