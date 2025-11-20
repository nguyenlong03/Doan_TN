/**
 * @(#)ForgetPasswordController.java 2021/09/09.
 * 
 * Copyright(C) 2021 by PHOENIX TEAM.
 * 
 * Last_Update 2021/09/09.
 * Version 1.00.
 */
package poly.store.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import poly.store.common.Constants;
import poly.store.entity.User;
import poly.store.model.UserRegister;
import poly.store.service.UserService;
import poly.store.service.impl.MailerServiceImpl;

/**
 * Class de lay lai mat khau
 * 
 * @author khoa-ph
 * @version 1.00
 */
@Controller
public class ForgetPasswordController {
	
	@Autowired
	UserService userService;
	
	@Autowired
	MailerServiceImpl mailerService;
	
	@Autowired
	PasswordEncoder pe;

	/**
	 * Hien thi man hinh forget-password
	 * 
	 * @param model
	 * @return man hinh forget-password
	 */
	@GetMapping("/forget-password")
	public String displayFormForgetPassword(Model model) {
		UserRegister userForm = new UserRegister();
		model.addAttribute("userForm", userForm);
		return Constants.USER_DISPLAY_FORGET_PASSWORD;
	}

	@PostMapping("/forget-password")
	public String handlerFormForgetPassword(Model model, @ModelAttribute("userForm") @Validated UserRegister userForm,
			BindingResult result) {
		if (userForm.getEmail().isEmpty()) {
			result.rejectValue("email", "NotBlank.userRegister.email");
		} else {
			User user = userService.findUserByEmail(userForm.getEmail());
			if (user == null) {
				result.rejectValue("email", "NotExist.userLogin.username");
			}
			else {
				String password = pe.encode(user.getPassword());
				String subject = "Yêu cầu đặt lại mật khẩu";
				String body = "<!DOCTYPE html>"
						+ "<html lang=\"vi\">"
						+ "<head>"
						+ "  <meta charset=\"UTF-8\" />"
						+ "  <title>Yêu cầu đặt lại mật khẩu</title>"
						+ "  <style>"
						+ "    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; font-size: 16px; }"
						+ "    .container { padding: 20px; }"
						+ "    a { background: #00bc69; color: #fff !important; padding: 10px 15px; border-radius: 4px; text-decoration: none; display: inline-block; margin: 10px 0; }"
						+ "  </style>"
						+ "</head>"
						+ "<body>"
						+ "  <div class=\"container\">"
						+ "    <p>Chào <strong>" + user.getFullname() + "</strong>,</p>"
						+ "    <p>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>"
						+ "    <p>Vui lòng nhấp vào nút bên dưới để đặt lại mật khẩu:</p>"
						+ "    <a href=\"http://localhost:8080/reset-password?code="+password+"&email="+user.getEmail()+"\">Đặt lại mật khẩu</a>"
						+ "    <p>Lưu ý: Liên kết này chỉ có hiệu lực trong 3 phút.</p>"
						+ "    <p>Trân trọng,<br/><strong>Đội ngũ phát triển</strong></p>"
						+ "  </div>"
						+ "</body>"
						+ "</html>";
				mailerService.queue(userForm.getEmail(), subject, body);
			}
		}

		if (result.hasErrors()) {
			return Constants.USER_DISPLAY_FORGET_PASSWORD;
		}
		
		model.addAttribute("alert", "Thông báo!");
		model.addAttribute("message", "Vui lòng kiểm tra email để thay đổi mật khẩu!");
		return Constants.USER_DISPLAY_ALERT_STATUS;
	}
}
