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
package org.gatein.web.device.configuration.metadata;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class DeviceRedirect
{
   String originSiteName;

   List<Redirect> redirects;

   public DeviceRedirect(String originSiteName)
   {
      this.originSiteName = originSiteName;
   }

   public String getSiteName()
   {
      return this.originSiteName;
   }

   public void addRedirect(Redirect redirect)
   {
      if (redirects == null)
      {
         redirects = new ArrayList<Redirect>();
      }

      redirects.add(redirect);
   }

   public Redirect getRedirect(String redirectSiteName)
   {
      //TODO: this is crazy inefficient, should should use something like a LinkedHashMap for Redirects and access directly...
      if (redirects != null && redirectSiteName != null)
      {
         for (Redirect redirect : redirects)
         {
            if (redirect.getRedirectSiteName().equals(redirectSiteName))
            {
               return redirect;
            }
         }
      }
      return null;
   }

   public List<Redirect> getRedirects()
   {
      return redirects;
   }
}
