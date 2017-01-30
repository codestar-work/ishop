package web;

import org.springframework.ui.*;
import org.springframework.boot.*;
import org.springframework.stereotype.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

@Controller
@SpringBootApplication
class Main {
	
	@RequestMapping("/")
	String showHome(Model model) {
		model.addAttribute("shop", "iStud");
		return "index";
	}
	
	@RequestMapping("/status") @ResponseBody
	String status() {
		return "Server is OK";
	}
	
	
	public static void main(String [] args) {
		SpringApplication.run(Main.class, args);
	}	
	
}
