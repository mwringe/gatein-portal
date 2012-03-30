/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.web.device.redirection;

import java.util.Map;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public interface DeviceRedirectionService
{
   public static final String NEED_BROWSER_DETECTION = "gtn.device.browserDetectionNeeded";

   public static final String NO_REDIRECT_DETECTED = "gtn.device.NoRedirectDetected";

   /**
    * Returns the site name to redirect to based on the current
    * site being accessed, the userAgentString of the browser and
    * any properties determined about the device.
    * 
    * Returns null if no redirect is required.
    * 
    * @param originSite The original site requested
    * @param userAgentString The user agent passed in the http header
    * @param deviceProperties Any device properties detected
    * @return The name of the site to redirect to, or null if no redirect is required
    */
   public String getRedirectSite(String originSite, String userAgentString, Map<String, String> deviceProperties);

   /**
    * Returns the URL to use for a redirect based on the site
    * we wish to redirect to and the requestPath of the original
    * request.
    *
    * This method may return null if the redirection service is configured
    * to only redirect if the redirect site contains the same nodes as specified in
    * the original request path.
    * 
    * @param redirectSite The site to redirect to
    * @param requestPath The request path to the original site
    * @return The url to use for a redirect, or null if no redirect is required.
    */
   public String getRedirectPath(String originSite, String redirectSite, String requestPath);

}
