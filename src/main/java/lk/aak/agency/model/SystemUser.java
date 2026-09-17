package lk.aak.agency.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "system_users")
public class SystemUser {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            name = "username",
            nullable = false,
            unique = true,
            length = 100
    )
    private String username;

    @Column(
            name = "password",
            nullable = false,
            length = 255
    )
    private String password;

    @Column(
            name = "full_name",
            nullable = false,
            length = 150
    )
    private String fullName;

    @Column(
            name = "role",
            nullable = false,
            length = 50
    )
    private String role;

    @Column(
            name = "enabled",
            nullable = false
    )
    private boolean enabled = true;

    public SystemUser() {
    }

    public SystemUser(
            String username,
            String password,
            String fullName,
            String role,
            boolean enabled) {

        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(
            String username) {

        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(
            String password) {

        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(
            String fullName) {

        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(
            String role) {

        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(
            boolean enabled) {

        this.enabled = enabled;
    }
}