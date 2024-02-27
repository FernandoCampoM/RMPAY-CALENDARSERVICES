package com.retailmanager.rmpaydashboard.services.DTO;
import com.retailmanager.rmpaydashboard.models.Product;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;

    @Size(max = 255, message = "{product.barcode.max}")
    @NotBlank(message = "{product.barcode.empty}")
    private String barcode;

    @NotBlank(message = "{product.name.empty}")
    @Size(max = 255, message = "{product.name.max}")
    private String name;

    @Size(max = 1000, message = "{product.description.max}")
    private String description;

    @NotNull(message = "{product.cost.null}")
    @Min(value = 0, message = "{product.cost.min}")
    private double cost;

    @NotNull(message = "{product.price.null}")
    @Min(value = 0, message = "{product.price.min}")
    private double price;
    
    private Long idCategory;
    private String nameCategory;
    private Long idBusiness;
    @Size(max = 255, message = "{product.code.max}")
    @NotBlank(message = "{product.code.empty}")
    private String code;

    // Inventory attributes

    @NotNull(message = "{product.estatal.null}")
    private boolean estatal;

    @NotNull(message = "{product.municipal.null}")
    private boolean municipal;

    @NotNull(message = "{product.inventoryLevel.null}")
    @Min(value = 0, message = "{product.inventoryLevel.min}")
    private int inventoryLevel;

    @NotNull(message = "{product.minimumLevel.null}")
    @Min(value = 0, message = "{product.minimumLevel.min}")
    private int minimumLevel;

    @NotNull(message = "{product.maximumLevel.null}")
    @Min(value = 0, message = "{product.maximumLevel.min}")
    private int maximumLevel;
    @NotNull(message = "{product.enable.null}")
    private Boolean enable;

    public static ProductDTO tOProduct(Product product) {
        ProductDTO objProduct = new ProductDTO();
        objProduct.setProductId(product.getProductId());
        objProduct.setIdCategory(product.getCategory().getCategoryId());
        objProduct.setBarcode(product.getBarcode());
        objProduct.setName(product.getName());
        objProduct.setDescription(  product.getDescription());
        objProduct.setCost(product.getCost());
        objProduct.setPrice(product.getPrice());
        objProduct.setCode( product.getCode());
        objProduct.setEstatal(product.isEstatal());
        objProduct.setMunicipal(product.isMunicipal());
        objProduct.setInventoryLevel(product.getInventoryLevel());
        objProduct.setMinimumLevel(product.getMinimumLevel());
        objProduct.setMaximumLevel(product.getMaximumLevel());
        objProduct.setEnable(product.isEnable());
        return objProduct;
    }
}