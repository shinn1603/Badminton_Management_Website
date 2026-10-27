package vn.yain.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "courts")
@Data
public class Court {
    public Court() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "court_code", nullable = false, unique = true, length = 20)
    private String courtCode;

    @Column(name = "court_name", nullable = false, length = 100)
    private String courtName;

    @Column(name = "court_type", nullable = false, length = 50)
    private String courtType;

    @Column(name = "branch_code", nullable = false, length = 20)
    private String branchCode;

    @Column(name = "hourly_rate", nullable = false, precision = 15, scale = 2)
    private BigDecimal hourlyRate;

    @Column(length = 30)
    private String status = "Trống";

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "condition_images", length = 4000)
    private String conditionImages;

    @Column(name = "likes_count")
    private Integer likesCount = 0;

    @Column(name = "comments", length = 4000)
    private String comments;

    @Column(name = "lighting_condition", length = 150)
    private String lightingCondition = "Hệ thống đèn LED chống chói 500 Lux chuẩn BWF";

    @Column(name = "floor_condition", length = 150)
    private String floorCondition = "Thảm PVC Enlio 4.5mm chống trơn trượt tiêu chuẩn thi đấu";

    @Column(name = "net_condition", length = 150)
    private String netCondition = "Lưới thi đấu BWF căng mới, cột nhôm chắc chắn";
}
