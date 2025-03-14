package pfe.HumanIQ.HumanIQ.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Pointage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime arrivalTime;
    private LocalDateTime departureTime;
    private LocalDateTime pauseStartTime;
    private LocalDateTime pauseEndTime;


    @Enumerated(EnumType.STRING)
    private PointageStatus status;

    private LocalDate date = LocalDate.now();
    private Double workingTime;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getWorkingTime() {
        return workingTime;
    }

    public void setWorkingTime(Double workingTime) {
        this.workingTime = workingTime;
    }

    public LocalDateTime getPauseEndTime() {
        return pauseEndTime;
    }
    public PointageStatus getStatus() {
        return status;
    }

    public void setStatus(PointageStatus status) {
        this.status = status;
    }

    public void setPauseEndTime(LocalDateTime pauseEndTime) {
        this.pauseEndTime = pauseEndTime;
    }
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }


    public LocalDateTime getPauseStartTime() {
        return pauseStartTime;
    }

    public void setPauseStartTime(LocalDateTime pauseStartTime) {
        this.pauseStartTime = pauseStartTime;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }





}
