package com.qaitdevlabs.qualityassessor.web;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qaitdevlabs.qualityassessor.assessment.service.AssessmentService;
import com.qaitdevlabs.qualityassessor.dao.RoleDao;
import com.qaitdevlabs.qualityassessor.domain.service.DomainService;
import com.qaitdevlabs.qualityassessor.dto.DomainDTO;
import com.qaitdevlabs.qualityassessor.dto.TreeNodeDTO;
import com.qaitdevlabs.qualityassessor.model.Assessment;
import com.qaitdevlabs.qualityassessor.model.Domain;
import com.qaitdevlabs.qualityassessor.model.Role;
import com.qaitdevlabs.qualityassessor.model.User;
import com.qaitdevlabs.qualityassessor.service.UserService;

@Controller
public class AssessmentController {

	private AssessmentService assessmentService;

	@Autowired
	public void setAssessmentService(AssessmentService assessmentService) {
		this.assessmentService = assessmentService;
	}

	private UserService userService;

	@Autowired
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	private DomainService domainService;

	@Autowired
	public void setDomainService(DomainService domainService) {
		this.domainService = domainService;
	}

	private RoleDao roleDao;

	public RoleDao getRoleDao() {
		return roleDao;
	}

	@Autowired
	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}

	@RequestMapping(value = "/assessment", method = RequestMethod.GET)
	public String getListOfRootDomains(ModelMap map) {
		List<DomainDTO> listOfRootDomains = domainService
				.getListOfRootDomains();
		map.addAttribute("listOfRootDomains", listOfRootDomains);
		return "assessment";
	}

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String home(ModelMap model, HttpServletRequest request) {
		
		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		System.out.println(auth.getAuthorities());
		User user = (User) auth.getPrincipal();
		Long userId = user.getUserId();
		HttpSession session = request.getSession();
		session.setAttribute("USER_ID", userId);
		request.setAttribute("message",session.getAttribute("message"));
		session.setAttribute("message",null);
		Role adminRole = roleDao.getRoleByName("ROLE_ADMIN");
		// Role userRole = roleDao.getRoleByName("ROLE_USER");
		System.out.println("landing page");
		if (auth.getAuthorities().contains(adminRole)) {
			System.out.println("landing admin page");
			session.setAttribute("group", "AdminGroup");
			return "home";
		} else {
			System.out.println("landing user page");
			session.setAttribute("group", "UserGroup");
			return getListOfRootDomains(model);
		}

	}

	@RequestMapping(value = "/domain", method = RequestMethod.GET)
	public String showDomainPage(ModelMap map) {
		return "domain";
	}

	@RequestMapping(value = "/rate", method = RequestMethod.GET)
	public String showRatePage(@RequestParam String key, ModelMap map,
			HttpServletRequest request) {

		return "rate";
	}

	@RequestMapping(value = "/rate", method = RequestMethod.POST)
	public @ResponseBody
	long saveRating(
			@RequestParam String key,
			@RequestParam String id,
			@RequestParam(value = "requestedUserId", required = false) String requestedUserId,
			@RequestParam String score, ModelMap map, HttpServletRequest request) {
		System.out.println("id" + id);

		Long userId = (Long) request.getSession().getAttribute("USER_ID");
		User assessor = userService.getUser(userId);

		User user = null;
		if (!requestedUserId.equals("null")) {
			user = userService.getUser(Long.valueOf(requestedUserId));
		} else {
			user = assessor;
		}

		Domain domain = domainService.getDomain(key);
		Long assessmentId = Long.valueOf(id);
		if (assessmentId == 0) {
			assessmentId = null;
		}
		Date assessmentDate = new Date();
		Assessment assessment = new Assessment();
		assessment.setAssessmentId(assessmentId);
		assessment.setUser(user);
		assessment.setAssessor(assessor);
		assessment.setDomain(domain);
		assessment.setScore(Integer.valueOf(score));
		assessment.setAssessmentDate(assessmentDate);
		assessment = assessmentService.saveAssessment(assessment);
		assessmentId = assessment.getAssessmentId();
		return assessmentId;
	}

	@RequestMapping(value = "/domainHierarchy", method = RequestMethod.GET)
	public @ResponseBody
	TreeNodeDTO getDomainHierarchy(
			@RequestParam String key,
			@RequestParam(value = "requestedUserId", required = false) String requestedUserId,
			ModelMap map, HttpServletRequest request) {
		Long userId = (Long) request.getSession().getAttribute("USER_ID");
		User assessor = userService.getUser(userId);
		System.out.println("requestedUserId" + requestedUserId);
		User user = null;
		if (!requestedUserId.equals("null")) {
			user = userService.getUser(Long.valueOf(requestedUserId));
		} else {
			user = assessor;
		}
		TreeNodeDTO dto = domainService.getDomainHierarchy(Long.valueOf(key),
				assessor, user);
		return dto;
	}

}