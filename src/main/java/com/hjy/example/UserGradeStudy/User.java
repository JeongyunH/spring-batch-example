package com.hjy.example.UserGradeStudy;

import com.hjy.example.temp.Orders;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Enumerated(EnumType.STRING)
    private Level level = Level.NORMAL;

//    private int totalAmount;
    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id")
    private List<Orders> orders;

    private LocalDate updatedDate;

    @Builder
    public User(String username, List<Orders> orders) {
        this.username = username;
        this.orders = orders;
    }

    public boolean availableLevelUp() {
        return Level.availableLevelUp(this.getLevel(), this.getTotalAmount());
    }

    private int getTotalAmount() {
        return this.orders.stream()
                .mapToInt(Orders::getAmount)
                .sum();
    }

    public Level levelUp(){
        Level nextLevel = Level.getNextLevel(this.getTotalAmount());

        this.level = nextLevel;
        this.updatedDate = LocalDate.now();

        return nextLevel;
    }

    public enum Level{
        VIP(500_000, null),
        GOLD(500_000, VIP),
        SILVER(300_000, GOLD),
        NORMAL(200_000, SILVER);

        private final int nextAmount;
        private final Level nextLevel;

        Level(int nextAmount, Level nextLevel){
            this.nextLevel = nextLevel;
            this.nextAmount = nextAmount;
        }

        private static boolean availableLevelUp(Level level, int totalAmount) {
            if(Objects.isNull(level)){
                return false;
            }
            if(Objects.isNull(level.nextLevel)){
                return false;
            }

            return totalAmount >= level.nextAmount;
        }

        private static Level getNextLevel(int totalAmount) {
            if(totalAmount >= Level.VIP.nextAmount){
                return VIP;
            }
            if(totalAmount >= Level.GOLD.nextAmount){
                return GOLD.nextLevel;
            }
            if(totalAmount >= Level.SILVER.nextAmount){
                return SILVER.nextLevel;
            }
            if(totalAmount >= Level.NORMAL.nextAmount){
                return NORMAL.nextLevel;
            }

            return NORMAL;
        }
    }
}
