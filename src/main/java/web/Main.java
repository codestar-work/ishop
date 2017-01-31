package web;
import java.sql.*;
import java.util.*;
import javax.servlet.http.*;
import org.springframework.ui.*;
import org.springframework.boot.*;
import org.springframework.stereotype.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

@Controller
@SpringBootApplication
public class Main {
	String database = "jdbc:mysql://icode.run/ishop" + 
						"?user=ishop&password=iShop2017";
	String shop = "iCoffee";
	
	Main() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) { }
	}
	
	@RequestMapping("/")
	String showHome(Model model) {
		LinkedList list = new LinkedList();
		try {
			Connection c = DriverManager.getConnection(database);
			Statement s = c.createStatement();
			ResultSet r = s.executeQuery("select * from product");
			while (r.next()) {
				String name = r.getString("name");
				list.add(name);
			}
			r.close();
			s.close();
			c.close();
		} catch (Exception e) { }
		model.addAttribute("product", list);
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
			Model model, HttpSession session) {
		boolean passed = false;
		try {
			Connection c = DriverManager.getConnection(database);
			PreparedStatement p = c.prepareStatement(
			"select * from member where name=? and password=sha2(?, 512)");
			p.setString(1, username);
			p.setString(2, password);
			ResultSet r = p.executeQuery();
			if (r.next()) {
				passed = true;
				Member m = new Member();
				m.name = r.getString("name");
				m.fullName = r.getString("full_name");
				m.email = r.getString("email");
				m.code  = r.getLong("code");
				session.setAttribute("member", m);
			}
			r.close();
			p.close();
			c.close();
		} catch (Exception e) { }
		if (passed) {
			return "redirect:/settings";
		} else {
			return "redirect:/login?message=Incorrect Password";			
		}
	}
	
	@RequestMapping("/settings")
	String showSettings(Model model, HttpSession session,
			String shop, String phone) {
		Object member = session.getAttribute("member");
		if (member == null) {
			return "redirect:/login";
		} else {
			try (Connection c = DriverManager.getConnection(database)) {
			try (Statement  s = c.createStatement()) {
			try (ResultSet  r = s.executeQuery("select * from shop")) {
				if (r.next()) {
					model.addAttribute("member", member);
					model.addAttribute("shop",   r.getString("name"));
					model.addAttribute("phone",  r.getString("phone"));
				}
			}	
			}
			} catch (Exception e) { }
			
			if (shop != null || phone != null) {
				model.addAttribute("shop", shop);
				model.addAttribute("phone", phone);
				String sql = "update shop set shop = ?, phone = ?";
				try (Connection c = DriverManager.getConnection(database)) {
				try (PreparedStatement  p = c.prepareStatement(sql)) {
					p.setString(1, shop);
					p.setString(2, phone);
					p.execute();
				}
				} catch (Exception e) { }
			}
			return "settings";
		}
	}
	
	@RequestMapping("/logout")
	String showLogOut(Model model, HttpSession session) {
		session.removeAttribute("member");
		model.addAttribute("shop", shop);
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
