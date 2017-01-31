package web;
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.http.*;
import org.springframework.ui.*;
import org.springframework.boot.*;
import org.springframework.stereotype.*;
import org.springframework.web.multipart.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

@Controller
@SpringBootApplication
public class Main {
	String database = "jdbc:mysql://icode.run/ishop" + 
						"?user=ishop&password=iShop2017" +
						"&characterEncoding=UTF-8";
	String shop = "";
	
	Main() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			try (Connection c = DriverManager.getConnection(database)) {
			try (Statement  s = c.createStatement()) {
			try (ResultSet  r = s.executeQuery("select * from shop")) {
				if (r.next()) {
					this.shop = r.getString("name");
				}
			}
			}
			}
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
		LinkedList<Product> list = new LinkedList<>();
		if (member == null) {
			return "redirect:/login";
		} else {
			try (Connection c = DriverManager.getConnection(database)) {
				try (Statement  s = c.createStatement()) {
					try (ResultSet  r = s.executeQuery("select * from shop")) {
						if (r.next()) {
							model.addAttribute("member", member);
							this.shop = r.getString("name");
							model.addAttribute("shop",   r.getString("name"));
							model.addAttribute("phone",  r.getString("phone"));
						}
					}
					try (ResultSet r = s.executeQuery("select * from product")){
						while (r.next()) {
							Product p = new Product();
							p.code   = r.getLong("code");
							p.name   = r.getString("name");
							p.detail = r.getString("detail");
							p.photo  = r.getString("photo");
							p.price  = r.getDouble("price");
							list.add(p);
						}
					}
				}
			} catch (Exception e) { }
			
			if (shop != null || phone != null) {
				model.addAttribute("shop", shop);
				model.addAttribute("phone", phone);
				String sql = "update shop set name = ?, phone = ?";
				try (Connection c = DriverManager.getConnection(database)) {
				try (PreparedStatement  p = c.prepareStatement(sql)) {
					p.setString(1, shop);
					p.setString(2, phone);
					p.execute();
				}
				} catch (Exception e) { }
				return "redirect:/settings";
			}
			model.addAttribute("product", list);
			return "settings";
		}
	}
	
	@RequestMapping("/logout")
	String showLogOut(Model model, HttpSession session) {
		session.removeAttribute("member");
		model.addAttribute("shop", shop);
		return "logout";
	}
	
	@RequestMapping("/edit")
	String showEditProduct(long code, Model m, HttpSession session) {
		Member member = (Member)session.getAttribute("member");
		if (member == null) {
			return "redirect:/login";
		} else {
			Product t = new Product();
			String sql = "select * from product where code = ?";
			try (Connection c = DriverManager.getConnection(database)) {
				try (PreparedStatement p = c.prepareStatement(sql)) {
					p.setLong(1, code);
					try (ResultSet r = p.executeQuery()) {
						if (r.next()) {
							t.code   = r.getLong("code");
							t.name   = r.getString("name");
							t.detail = r.getString("detail");
							t.photo  = r.getString("photo");
							t.price  = r.getDouble("price");
							if (t.name   == null) { t.name   = ""; }
							if (t.detail == null) { t.detail = ""; }
							if (t.photo  == null) { t.photo  = ""; }
						}
					}
				}
			} catch (Exception e) { }

			m.addAttribute("shop", shop);
			m.addAttribute("product", t);
			return "edit";
		}
	}
	
	@RequestMapping(value="/edit", method=RequestMethod.POST)
	String updateProduct(HttpSession session, Model model,
			long code, String name, String detail, double price,
			MultipartFile photo) {
		Member member = (Member)session.getAttribute("member");
		if (member == null) {
			return "redirect:/login";
		} else {
			Product t = new Product();
			t.code   = code;
			t.name   = name;
			t.detail = detail;
			t.price  = price;
			String file = UUID.randomUUID().toString() + ".jpg";
			if (photo != null) {
				t.photo = file;
				try {
					byte [ ] data = photo.getBytes();
					FileOutputStream fos = new FileOutputStream(
					"./src/main/resources/public/" + file);
					for (int i = 0; i < data.length; i++) {
						fos.write(data[i]);
					}
					fos.close();
				} catch (Exception e) { }
			}
			
			model.addAttribute("product", t);
			model.addAttribute("shop", shop);
			
			String sql = "update product set name=?,detail=?,price=?,photo=? " +
						"where code=?";
			try (Connection c = DriverManager.getConnection(database)) {
				try (PreparedStatement p = c.prepareStatement(sql)) {
					p.setString(1, name);
					p.setString(2, detail);
					p.setDouble(3, price);
					p.setString(4, file);
					p.setLong(5, code);
					p.execute();
				}
			} catch (Exception e) { }
			
			return "edit";
		}
	}
	
	@RequestMapping("/result")
	String showSearchResult(String query, Model model) {
		LinkedList<Product> list = new LinkedList<>();
		String sql = "select * from product where " +
						"name like ? or detail like ?";
		try (Connection c = DriverManager.getConnection(database)) {
			try (PreparedStatement p = c.prepareStatement(sql)) {
				p.setString(1, "%" + "query" + "%");
				p.setString(2, "%" + "query" + "%");
				try (ResultSet r = p.executeQuery()) {
					while (r.next()) {
						Product t = new Product();
						t.code = r.getLong("code");
						t.name = r.getString("name");
						t.detail = r.getString("detail");
						t.price = r.getDouble("price");
						t.photo = r.getString("photo");
						list.add(t);
					}
				}
			}
		} catch (Exception e) { }
		
		model.addAttribute("product", list);
		return "result";
	}
	
	@RequestMapping("/status") @ResponseBody
	String status() {
		return "Server is OK";
	}
	
	public static void main(String [] args) {
		SpringApplication.run(Main.class, args);
	}
	
}
