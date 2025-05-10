package com.hobbySphere.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "UserInterests")
public class UserInterests {

    @Embeddable
    public static class UserInterestId implements Serializable {

        private static final long serialVersionUID = 1L;


        @ManyToOne
        @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)  // Correct reference
        private Users user;

        @ManyToOne
        @JoinColumn(name = "interest_id", referencedColumnName = "interest_id", nullable = false)
        private Interests interest;

        // Default constructor
        public UserInterestId() {}

        // Parameterized constructor
        public UserInterestId(Users user, Interests interest) {
            this.user = user;
            this.interest = interest;
        }

        // Getters and Setters
        public Users getUser() {
            return user;
        }

        public void setUser(Users user) {
            this.user = user;
        }

        public Interests getInterest() {
            return interest;
        }

        public void setInterest(Interests interest) {
            this.interest = interest;
        }

        // equals() and hashCode() are necessary for composite keys to work correctly
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserInterestId that = (UserInterestId) o;
            return Objects.equals(user, that.user) && Objects.equals(interest, that.interest);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, interest);
        }
    }

    @EmbeddedId
    private UserInterestId id;

    // Getter and Setter
    public UserInterestId getId() {
        return id;
    }

    public void setId(UserInterestId id) {
        this.id = id;
    }

    // The interest field should be accessible from the embedded UserInterestId class
    @ManyToOne
    @MapsId("interest")  // This maps the interest field from the composite key
    @JoinColumn(name = "interest_id", referencedColumnName = "interest_id", insertable = false, updatable = false)
    private Interests interest;
}
