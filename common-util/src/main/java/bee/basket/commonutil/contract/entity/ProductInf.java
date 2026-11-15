package bee.basket.commonutil.contract.entity;

import java.util.List;

public interface ProductInf {
    String getProductId();

    String getVariantId();

    String getName();

    String getDescription();

    List<? extends ProductFeatureInf> getFeatures();

    String getImages();
}
