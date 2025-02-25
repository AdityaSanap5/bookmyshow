package com.bookmyshow.controller;

import java.io.IOException;
import java.security.Principal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Base64;

import com.bookmyshow.entity.Image;
import com.bookmyshow.service.ImageService;
import com.bookmyshow.service.ImageServiceImpl;
import com.bookmyshow.service.UserServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.bookmyshow.entity.User;
import com.bookmyshow.repository.UserRepo;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private UserServiceImpl userServiceImpl;

	@Autowired
	private ImageService imageService;

	@Autowired
	private ImageServiceImpl imageServiceImpl;

	@ModelAttribute
	public void commonUser(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			User user = userRepo.findByEmail(email);
			m.addAttribute("user", user);
		}
	}

	@GetMapping("/profile")
	public String profile() {
		return "admin/admin_profile";
	}

	@GetMapping("/")
	public String index(Principal principal, HttpSession session, Model model) throws  SQLException {

		if (principal != null && session.getAttribute("userImage") == null) {

			String email = principal.getName();

			Image userImage = imageServiceImpl.findByUserEmail(email);
			if (userImage != null && userImage.getImage() != null) {
				byte[] imageBytes = userImage.getImage().getBytes(1, (int) userImage.getImage().length());
				String base64Image = Base64.getEncoder().encodeToString(imageBytes);
				session.setAttribute("userImage", base64Image);
			}

		}
		return "admin/admin_index";
	}

	@GetMapping("/change-password")
	public String changePassword() {
		return "admin/change-password";
	}


	@GetMapping("/editProfile")
	public String showEditProfilePage(Model model, Principal principal) {

		String email = principal.getName();
		User user = userRepo.getUserByEmail(email);

		model.addAttribute("user", user);

		return "admin/edit_Profile";

	}

	@PostMapping("/updateProfile")
	public String updateProfile(@ModelAttribute User user, Principal principal, HttpSession session) {

		String email = principal.getName();

		boolean isUpdated = userServiceImpl.updateUserProfile(user, email);

		if (isUpdated) {
			session.setAttribute("msg", "Profile updated successfully.");
		} else {
			session.setAttribute("msg", "Error updating profile.");
		}

		return "redirect:/admin/editProfile";

	}

	@PostMapping("/change-password")
	public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
								 @RequestParam String currentPassword,
								 @RequestParam String newPassword,
								 @RequestParam String confirmPassword,
								 Model model,
								 RedirectAttributes redirectAttributes) {

		if (!newPassword.equals(confirmPassword)) {
			model.addAttribute("error", "New passwords do not match.");
			return "/admin/change-password";
		}

		boolean isChanged = userServiceImpl.changePasswordByEmail(userDetails.getUsername(), currentPassword, newPassword);

		if (isChanged) {
			redirectAttributes.addFlashAttribute("message", "Password changed successfully!");
			return "redirect:/admin/change-password";
		} else {
			model.addAttribute("error", "Current password is incorrect.");
			return "/admin/change-password";
		}
	}



	@GetMapping("/setImage")
	public String setImageInSession(Principal principal, HttpSession session) throws  SQLException {
		if (principal != null) {

			String email = principal.getName();

			Image userImage = imageServiceImpl.findByUserEmail(email);
			if (userImage != null && userImage.getImage() != null) {
				byte[] imageBytes = userImage.getImage().getBytes(1, (int) userImage.getImage().length());
				String base64Image = Base64.getEncoder().encodeToString(imageBytes);
				session.setAttribute("userImage", base64Image);
			}
		}
		return "redirect:/admin/";
	}


	@GetMapping("/display")
	public ResponseEntity<byte[]> displayImageFromSession(HttpSession session) {

		String base64Image = (String) session.getAttribute("userImage");

		if (base64Image != null) {
			byte[] imageBytes = Base64.getDecoder().decode(base64Image);
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes);
		}
		return ResponseEntity.notFound().build();
	}

	@GetMapping("/add")
	public ModelAndView addImage(){
		return new ModelAndView("addimage");
	}

	@PostMapping("/add")
	public String addImagePost(@RequestParam("image") MultipartFile file, Principal principal, HttpSession session) throws IOException,  SQLException {
		String email = principal.getName();

		User user = userRepo.getUserByEmail(email);
		byte[] bytes = file.getBytes();

		Blob blob = new javax.sql.rowset.serial.SerialBlob(bytes);
		Image existingImage = imageServiceImpl.findByUserEmail(email);

		if (existingImage != null) {
			existingImage.setImage(blob);
			imageServiceImpl.update(existingImage);
		} else {
			Image newImage = new Image();
			newImage.setImage(blob);
			newImage.setUser(user);
			imageService.create(newImage);
		}

		byte[] imageBytes = blob.getBytes(1, (int) blob.length());
		String base64Image = Base64.getEncoder().encodeToString(imageBytes);
		session.setAttribute("userImage", base64Image);

		return "redirect:/admin/editProfile";
	}



}
