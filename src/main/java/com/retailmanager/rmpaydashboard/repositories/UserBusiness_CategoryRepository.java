package com.retailmanager.rmpaydashboard.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Category;
import com.retailmanager.rmpaydashboard.models.UserBusiness_Category;
import com.retailmanager.rmpaydashboard.models.UsersBusiness;

public interface UserBusiness_CategoryRepository extends CrudRepository<UserBusiness_Category, Long> {
    public List<UserBusiness_Category> findByObjUser(UsersBusiness objUser);
    public List<UserBusiness_Category> findByObjUserAndDownload(UsersBusiness objUser, boolean download);
    public List<UserBusiness_Category>  findByObjCategory(Category objCategory);

    @Modifying
    @Query("UPDATE UserBusiness_Category u SET u.download = :download WHERE u.objCategory.categoryId = :categoryId and u.objUser.userBusinessId = :user_id")
    public void updateDownload(Long categoryId, Long user_id,boolean download);
    @Modifying
    @Query("DELETE FROM UserBusiness_Category u WHERE u.objCategory.categoryId = :categoryId")
    public void deleteByCategoryId(Long categoryId);
}
