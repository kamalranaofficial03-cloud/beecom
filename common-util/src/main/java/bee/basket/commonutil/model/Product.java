package bee.basket.commonutil.model;

import java.util.List;

public record Product (String productId, String variantId, String name, String description, List<String> images, List<ProductFeature> features){

}
