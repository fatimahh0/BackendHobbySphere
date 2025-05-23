package com.hobbySphere.services;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hobbySphere.dto.AdminActivityDTO;
import com.hobbySphere.repositories.ActivitiesRepository;

@Service
	public class AdminActivityService {

	    @Autowired
	    private ActivitiesRepository activityRepository;

	    public List<AdminActivityDTO> getAllActivities() {
	        return activityRepository.findAllActivitiesWithBusinessInfo();
	    }
	}

