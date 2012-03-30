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
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class Condition
{
   //TODO: maybe handle the UAS conditions in another class?
   protected List<String> userAgentContainsList;

   protected List<String> userAgentDoesNotContainList;

   protected List<Pattern> userAgentPatternList;

   protected List<DeviceProperty> deviceProperties;

   public void addUserAgentContains(String contains)
   {
      if (userAgentContainsList == null)
      {
         userAgentContainsList = new ArrayList<String>();
      }
      userAgentContainsList.add(contains);
   }

   public List<String> getUserAgentContains()
   {
      return userAgentContainsList;
   }

   public void addUserAgentDoesNotContain(String doesNotContain)
   {
      if (userAgentDoesNotContainList == null)
      {
         userAgentDoesNotContainList = new ArrayList<String>();
      }
      userAgentDoesNotContainList.add(doesNotContain);
   }

   public List<String> getUserAgentDoesNotContain()
   {
      return userAgentDoesNotContainList;
   }

   public void addUserAgentMatches(Pattern matches)
   {
      if (userAgentPatternList == null)
      {
         userAgentPatternList = new ArrayList<Pattern>();
      }
      userAgentPatternList.add(matches);
   }

   public List<Pattern> getUserAgentPattern()
   {
      return userAgentPatternList;
   }

   public void addDeviceProperty(DeviceProperty devicePropertyComparator)
   {
      if (deviceProperties == null)
      {
         deviceProperties = new ArrayList<DeviceProperty>();
      }
      deviceProperties.add(devicePropertyComparator);
   }

   public List<DeviceProperty> getDeviceProperties()
   {
      return this.deviceProperties;
   }

}
