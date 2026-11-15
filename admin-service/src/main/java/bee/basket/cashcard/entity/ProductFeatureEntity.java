package bee.basket.cashcard.entity;

import bee.basket.commonutil.commons.ImageBuilder;
import bee.basket.commonutil.contract.ToModel;
import bee.basket.commonutil.contract.entity.ProductFeatureInf;
import bee.basket.commonutil.model.ProductFeature;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="product_features")
public class ProductFeatureEntity implements ProductFeatureInf, ToModel<ProductFeature> {
    private String description;
    private String images;

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    @Override
    public ProductFeature model(){
        return new ProductFeature(description, ImageBuilder.builder().sep(",").build().split(images));
    }

    public static ProductFeatureEntity create(ProductFeature feature){
        ProductFeatureEntity newInstance = new ProductFeatureEntity();
        newInstance.setDescription(feature.description());
        newInstance.setImages(ImageBuilder.builder().sep(",").build().join(feature.images()));
        return newInstance;
    }
}
