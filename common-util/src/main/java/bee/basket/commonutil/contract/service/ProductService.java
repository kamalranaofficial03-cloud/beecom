package bee.basket.commonutil.contract.service;

import bee.basket.commonutil.model.Product;

import java.util.List;

public interface ProductService {
    List<Product> getProductByProductId(String productId);
}
