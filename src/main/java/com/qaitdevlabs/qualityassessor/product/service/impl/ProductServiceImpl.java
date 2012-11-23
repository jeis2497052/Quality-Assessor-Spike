package com.qaitdevlabs.qualityassessor.product.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.qaitdevlabs.qualityassessor.model.Product;
import com.qaitdevlabs.qualityassessor.model.User;
import com.qaitdevlabs.qualityassessor.product.dao.ProductDao;
import com.qaitdevlabs.qualityassessor.product.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService{

	private ProductDao productDao;
	
	@Autowired
	public void setProductDao(ProductDao productDao) {
		this.productDao = productDao;
	}
	
	@Override
	public List<Product> getListOfProductsByUser(User user) {
		return productDao.getListOfProductsByUser(user);
	}

}
