package com.qaitdevlabs.qualityassessor.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.omg.CORBA.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.qaitdevlabs.qualityassessor.domain.service.DomainService;
import com.qaitdevlabs.qualityassessor.dto.ProductDTO;
import com.qaitdevlabs.qualityassessor.dto.TemplateSelectionForm;
import com.qaitdevlabs.qualityassessor.dto.TreeNodeDTO;
import com.qaitdevlabs.qualityassessor.model.Domain;
import com.qaitdevlabs.qualityassessor.model.Product;
import com.qaitdevlabs.qualityassessor.model.ProductTemplateMap;
import com.qaitdevlabs.qualityassessor.model.User;
import com.qaitdevlabs.qualityassessor.product.service.ProductService;
import com.qaitdevlabs.qualityassessor.productTemplateMap.service.ProductTemplateMapService;
import com.qaitdevlabs.qualityassessor.service.UserService;

@Controller
public class TemplateSelectionViewController {

	private ProductService productService;

	@Autowired
	public void setProductService(ProductService productService) {
		this.productService = productService;
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

	private ProductTemplateMapService productTemplateMapService;

	@Autowired
	public void setProductTemplateMapService(
			ProductTemplateMapService productTemplateMapService) {
		this.productTemplateMapService = productTemplateMapService;
	}

	@RequestMapping(value = "/templateSelectionView", method = RequestMethod.GET)
	public ModelAndView getTemplateSelectionView(@RequestParam Long productId,
			HttpServletRequest request) {
		Product product = productService.getProductById(productId);
		TemplateSelectionForm form = new TemplateSelectionForm();
		if (product != null) {
			form.setProductName(product.getProductName());
			form.setProductId(product.getProductId());
		} else {
			System.out.println("Product not found in system");
		}
		return new ModelAndView("templateSelectionView", "command", form);
	}

	@RequestMapping(value = "/templateSelectionView", method = RequestMethod.POST, params = "save&ReviewLater")
	public String submitTemplateSelectionView(
			@ModelAttribute TemplateSelectionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		Long userId = (Long) request.getSession().getAttribute("USER_ID");
		User user = userService.getUser(userId);
		Long domainId = form.getDomainId();
		Domain domain = domainService.getDomain(domainId.toString());
		Long productId = form.getProductId();
		Product product = productService.getProductById(productId);
		if (isProductCorrespondToUser(product, user)) {
			saveProductTemplateMap(domain, product);
		} else {
			response.setStatus(500);
			System.out.println("Product doesn't correspond to user");
		}
		return "redirect:/productReviews";
	}

	@RequestMapping(value = "/templateSelectionView", method = RequestMethod.POST, params = "selfReview")
	public String submitAndSelfReview(
			@ModelAttribute TemplateSelectionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		Long userId = (Long) request.getSession().getAttribute("USER_ID");
		Long domainId = form.getDomainId();
		User user = userService.getUser(userId);
		Long productId = form.getProductId();
		Product product = productService.getProductById(productId);
		Domain domain = domainService.getDomain(domainId.toString());
		if (isProductCorrespondToUser(product, user)) {
			saveProductTemplateMap(domain, product);
			TreeNodeDTO dto = domainService.getDomainHierarchy(
					form.getDomainId(), user, product, true);
			System.out.println(dto.getTitle());
			request.setAttribute("templateDTO",dto);
		} else {
			response.setStatus(500);
			System.out.println("Product doesn't correspond to user");
		}
		return "reviewPage";
	}

	public void saveProductTemplateMap(Domain domain, Product product) {
		ProductTemplateMap map = new ProductTemplateMap();
		map.setDomain(domain);
		map.setProduct(product);
		productTemplateMapService.saveOrUpdateProductTemplateMap(map);
	}

	private boolean isProductCorrespondToUser(Product product, User user) {
		User productCreatedUse = product.getUser();
		if (user.equals(productCreatedUse)) {
			return true;
		}
		return false;

	}

	@RequestMapping(value = "/getMatchingProducts", method = RequestMethod.GET)
	public @ResponseBody
	List<ProductDTO> getMatchingProducts(@RequestParam String term) {
		System.out.println("controller");
		List<ProductDTO> matchingProductList = productService
				.getMatchingProducts(term);
		System.out.println(matchingProductList);
		return matchingProductList;
	}

	@RequestMapping(value = "/productReviews", method = RequestMethod.GET)
	public String getProductsToBeReviewed(HttpServletRequest request) {
		System.out.println("controller");
		List<ProductTemplateMap> matchingProductList = productTemplateMapService
				.getProductsToBeReviewed();
		System.out.println(matchingProductList);
		request.setAttribute("list", matchingProductList);
		return "productReviews";
	}
}
