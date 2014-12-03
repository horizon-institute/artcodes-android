package uk.ac.horizon.aestheticodes.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Entity
public class UserExperiences
{
	@Id
	private String userID;
	private final List<String> experienceIDs = new ArrayList<String>();
}
