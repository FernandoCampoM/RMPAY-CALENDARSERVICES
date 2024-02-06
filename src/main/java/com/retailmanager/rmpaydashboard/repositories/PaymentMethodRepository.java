package com.retailmanager.rmpaydashboard.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.PaymentMethods;

public interface PaymentMethodRepository extends CrudRepository<PaymentMethods, String> {
    Optional<PaymentMethods> findOneByName(String name);
    List<PaymentMethods> findByEnableTrue();

    /**
     * Update the enable status of a payment method.
     *
     * @param  code   the code of the payment method
     * @param  enable the new enable status
     * @return        the number of rows affected
     */
    @Modifying
    @Query("UPDATE PaymentMethods u SET u.enable = :enable WHERE u.code = :code")
    public int updateEnable(String code, boolean enable);
}
