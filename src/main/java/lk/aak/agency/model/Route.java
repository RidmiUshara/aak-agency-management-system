package lk.aak.agency.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Route name is required.")
    @Size(max = 100, message = "Route name must be 100 characters or fewer.")
    @Column(name = "route_name", nullable = false, unique = true)
    private String routeName;

    @Column(name = "area_description", columnDefinition = "TEXT")
    private String areaDescription;

    @Column(name = "status", nullable = false)
    private String status;

    public Route() {
    }

    @PrePersist
    public void setDefaultValues() {
        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getAreaDescription() {
        return areaDescription;
    }

    public void setAreaDescription(String areaDescription) {
        this.areaDescription = areaDescription;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
