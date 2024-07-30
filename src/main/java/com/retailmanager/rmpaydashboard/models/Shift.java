package com.retailmanager.rmpaydashboard.models;
import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shiftId;

    @Column( nullable = false)
    private Double initialBalance;
    @Column( nullable = true)
    private Double finalBalance;
    @Column( nullable = false)
    private LocalDate startDate;
    @Column( nullable = false)
    private LocalTime startTime;
    @Column( nullable = true)
    private LocalDate endDate;
    @Column( nullable = true)
    private LocalTime endTime;

    private String summary;



    @ManyToOne
    @JoinColumn(name = "userBusinessId", nullable = false)
    private UsersBusiness userBusiness;
    @ManyToOne
    @JoinColumn(name = "terminalId", nullable = false)
    private Terminal terminal;
}