package com.retailmanager.rmpaydashboard.repositories;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Product;
import com.retailmanager.rmpaydashboard.models.UserBusiness_Product;
import com.retailmanager.rmpaydashboard.models.UsersBusiness;

public interface UserBusiness_ProductRepository extends CrudRepository<UserBusiness_Product, Long> {
    
    public List<UserBusiness_Product> findByObjUser(UsersBusiness objUser);
    public List<UserBusiness_Product> findByObjUserAndDownload(UsersBusiness objUser, boolean download);
    public List<UserBusiness_Product>  findByObjProduct(Product objProduct);

    @Modifying
    @Query("UPDATE UserBusiness_Product u SET u.download = :download WHERE u.objProduct.productId = :product_id and u.objUser.userBusinessId = :user_id")
    public void updateDownload(Long product_id, Long user_id,boolean download);
}
