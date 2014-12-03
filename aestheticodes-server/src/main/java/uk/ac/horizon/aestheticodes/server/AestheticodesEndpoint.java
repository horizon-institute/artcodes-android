/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2014  Aestheticodes
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.horizon.aestheticodes.server;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.NotFoundException;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import uk.ac.horizon.aestheticodes.model.Experience;
import uk.ac.horizon.aestheticodes.model.UserExperiences;

import javax.inject.Named;

@Api(name = "aestheticodes",
	 version = "v1",
	 scopes = {Constants.EMAIL_SCOPE},
	 clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID},
	 audiences = {Constants.ANDROID_AUDIENCE},
	 namespace = @ApiNamespace(ownerDomain = "server.aestheticodes.horizon.ac.uk", ownerName = "server.aestheticodes.horizon.ac.uk", packagePath = ""))
public class AestheticodesEndpoint
{
	@ApiMethod(name = "experiences")
	public UserExperiences getExperiences(User user) throws OAuthRequestException
	{
		if(user == null)
		{

		}
		else
		{

		}
		return null;
	}

	@ApiMethod(name = "experience")
	public Experience getExperience(@Named("id") String id) throws NotFoundException
	{
		return null;
	}

	@ApiMethod(name = "experience.store")
	public Experience storeExperience(Experience experience, User user)
	{
		return null;
	}

	@ApiMethod(name= "experiences.store")
	public void storeExperiences(UserExperiences experiences, User user)
	{

	}
}
