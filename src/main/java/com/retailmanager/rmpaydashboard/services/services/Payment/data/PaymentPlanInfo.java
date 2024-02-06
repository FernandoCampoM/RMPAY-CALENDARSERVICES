
package com.retailmanager.rmpaydashboard.services.services.Payment.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class PaymentPlanInfo {
    private int PlanId;
    private float SwipeDiscount;
    private float SwipeTransactionFee;
    private float NonSwipeDiscount;
    private float NonSwipeTransactionFee;
    private float MonthlyFee;
    private String Description;
    private String PlanName;
    private boolean DisplayName;
}
