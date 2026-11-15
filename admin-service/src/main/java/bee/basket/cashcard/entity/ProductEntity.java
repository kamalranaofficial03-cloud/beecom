package bee.basket.cashcard.entity;

import bee.basket.commonutil.commons.ImageBuilder;
import bee.basket.commonutil.contract.ToModel;
import bee.basket.commonutil.contract.entity.ProductInf;
import bee.basket.commonutil.model.Product;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_variant",
                        columnNames = {"product_id", "variant_id"}
                )
        }
)
public class ProductEntity extends Audit implements ProductInf, ToModel<Product> {
    @Column(name="product_id", nullable = false)
    private String productId;
    @Column(name="variant_id", nullable=false)
    private String variantId;
    private String name;
    private String description;
    @OneToMany
    @JoinColumn(name="product_id")
    private List<ProductFeatureEntity> features = new ArrayList<>();
    private String images;

    @Override
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Override
    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public List<ProductFeatureEntity> getFeatures() {
        return features;
    }

    public void setFeatures(List<ProductFeatureEntity> features) {
        this.features = features;
    }

    @Override
    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Product model(){
        return new Product(productId,variantId,name,description, ImageBuilder.builder().sep(",").build().split(images), features.stream().map(f-> f.model()).collect(Collectors.toUnmodifiableList()));
    }

    public static ProductEntity create(Product product){
        ProductEntity newInstance = new ProductEntity();
        newInstance.setDescription(product.description());
        newInstance.setImages(ImageBuilder.builder().sep(",").build().join(product.images()));
        newInstance.setName(product.name());
        newInstance.setVariantId(product.variantId());
        newInstance.setProductId(product.productId());
        newInstance.setFeatures(product.features().stream().map(f-> ProductFeatureEntity.create(f)).collect(Collectors.toUnmodifiableList()));
        return newInstance;
    }

}
