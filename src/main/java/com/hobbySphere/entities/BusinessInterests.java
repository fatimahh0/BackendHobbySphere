package com.hobbySphere.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "BusinessInterests")
public class BusinessInterests {

    @Embeddable
    public static class BusinessInterestsId implements Serializable {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Column(name = "business_id")
        private Long businessId;

        @Column(name = "interest_id")
        private Long interestId;

        public BusinessInterestsId() {}

        public BusinessInterestsId(Long businessId, Long interestId) {
            this.businessId = businessId;
            this.interestId = interestId;
        }

        public Long getBusinessId() {
            return businessId;
        }

        public void setBusinessId(Long businessId) {
            this.businessId = businessId;
        }

        public Long getInterestId() {
            return interestId;
        }

        public void setInterestId(Long interestId) {
            this.interestId = interestId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BusinessInterestsId)) return false;
            BusinessInterestsId that = (BusinessInterestsId) o;
            return Objects.equals(businessId, that.businessId) &&
                   Objects.equals(interestId, that.interestId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(businessId, interestId);
        }
    }

    @EmbeddedId
    private BusinessInterestsId id;

    @ManyToOne
    @MapsId("businessId")
    @JoinColumn(name = "business_id", nullable = false)
    private Businesses business;

    @ManyToOne
    @MapsId("interestId")
    @JoinColumn(name = "interest_id", nullable = false)
    private Interests interest;

    public BusinessInterests() {}

    public BusinessInterests(Businesses business, Interests interest) {
        this.business = business;
        this.interest = interest;
        this.id = new BusinessInterestsId(business.getId(), interest.getId());
    }

    public BusinessInterestsId getId() {
        return id;
    }

    public void setId(BusinessInterestsId id) {
        this.id = id;
    }

    public Businesses getBusiness() {
        return business;
    }

    public void setBusiness(Businesses business) {
        this.business = business;
    }

    public Interests getInterest() {
        return interest;
    }

    public void setInterest(Interests interest) {
        this.interest = interest;
    }
}
