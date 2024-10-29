package vn.loh.springboot_thymeleaf_exercise.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "categories")
@Builder
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", columnDefinition = "nvarchar(200) not null")
    @NotEmpty(message = "Tên danh mục không được để trống")
    private String name;

    @Column(name = "images", columnDefinition = "nvarchar(500)")
    private String images;

    private int status;
}
