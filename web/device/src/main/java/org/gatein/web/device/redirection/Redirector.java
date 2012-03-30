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

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.web.device.configuration.metadata.Condition;
import org.gatein.web.device.configuration.metadata.DeviceConfiguration;
import org.gatein.web.device.configuration.metadata.DeviceProperty;
import org.gatein.web.device.configuration.metadata.DeviceRedirect;
import org.gatein.web.device.configuration.metadata.Redirect;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class Redirector
{
   protected static Logger log = LoggerFactory.getLogger(Redirector.class);

   DeviceConfiguration dcd;

   public Redirector(DeviceConfiguration dcd)
   {
      this.dcd = dcd;
   }

   protected enum RedirectCondition {
      REDIRECT_FOUND, DEVICE_DETECTION_NEEDED, NO_REDIRECT_FOUND
   }

   public String getRedirectSite(String originSite, String userAgentString, Map<String, String> deviceProperties)
   {
      DeviceRedirect deviceRedirect = dcd.getDeviceRedirects(originSite);

      boolean needsBrowserDetection = false;

      if (deviceRedirect != null && deviceRedirect.getRedirects() != null)
      {
         for (Redirect redirect : deviceRedirect.getRedirects())
         {
            RedirectCondition redirectCondition = checkConditions(userAgentString, deviceProperties,
                  redirect.getConditions());
            if (redirectCondition.equals(RedirectCondition.REDIRECT_FOUND))
            {
               log.debug("Found a match with UAS " + userAgentString + " and DeviceProperties " + deviceProperties
                     + ". Seting redirect to : " + redirect.getRedirectSiteName());
               return redirect.getRedirectSiteName();
            }
            else if (redirectCondition.equals(RedirectCondition.DEVICE_DETECTION_NEEDED))
            {
               needsBrowserDetection = true;
            }
         }

         if (needsBrowserDetection == false)
         {
            log.debug("Could not find a match with the specifed uas and device properties. Returning NO_REDIRECT_DETECTED");
            return DeviceRedirectionService.NO_REDIRECT_DETECTED;
         }
         else
         {
            log.debug("Found a match with the specifed uas but it requires device properties. Returning NO_REDIRECT_DETECTED");
            return DeviceRedirectionService.NEED_BROWSER_DETECTION;
         }
      }
      else
      {
         log.debug("No UserAgentString specified and no device properties. Returning NO_REDIRECT_DETECTED");
         return DeviceRedirectionService.NO_REDIRECT_DETECTED;
      }
   }

   protected RedirectCondition checkConditions(String userAgentString, Map<String, String> deviceProperties,
         List<Condition> conditions)
   {
      log.debug("Checking conditions for redirect with " + userAgentString + " and device properties "
            + deviceProperties);
      if (conditions != null)
      {
         for (Condition condition : conditions)
         {
            boolean userAgentStringMatch = checkUserAgentStrings(userAgentString, condition);
            log.debug("UserAgentStringMatch : " + userAgentStringMatch);
            if (userAgentStringMatch == true)
            {
               if (condition.getDeviceProperties() == null || condition.getDeviceProperties().isEmpty())
               {
                  log.debug("UserAgentStringMatch and no device detection has been specified. Using Redirect");
                  return RedirectCondition.REDIRECT_FOUND;
               }
               else
               {
                  if (deviceProperties == null || deviceProperties.isEmpty())
                  {
                     log.debug("Conditional device properties exists, but no deviceProperties available. Using Browser Detection");
                     return RedirectCondition.DEVICE_DETECTION_NEEDED;
                  }
                  else
                  {
                     boolean devicePropertiesMatch = checkDeviceProperties(deviceProperties, condition);
                     if (devicePropertiesMatch == true)
                     {
                        log.debug("UserAgentStringMatch and device properties match. Using Redirect");
                        return RedirectCondition.REDIRECT_FOUND;
                     }
                     else
                     {
                        return RedirectCondition.NO_REDIRECT_FOUND;
                     }
                  }
               }
            }
         }
      }
      return RedirectCondition.NO_REDIRECT_FOUND;
   }

   protected boolean checkDeviceProperties(Map<String, String> deviceProperties, Condition condition)
   {
      if (condition.getDeviceProperties() != null)
      {
         if (deviceProperties == null)
         {
            return false;
         }

         for (DeviceProperty deviceProperty : condition.getDeviceProperties())
         {
            if (deviceProperties.containsKey(deviceProperty.getPropertyName()))
            {
               if (!checkProperty(deviceProperties.get(deviceProperty.getPropertyName()), deviceProperty))
               {
                  return false;
               }
            }
            else
            {
               return false;
            }
         }
      }
      return true; // if the deviceProperties are null or empty, then the condition is matched
   }

   protected boolean checkProperty(String propertyValue, DeviceProperty deviceProperty)
   {
      if (deviceProperty.getGreaterThan() != null)
      {
         Float propertyValueFloat = Float.parseFloat(propertyValue);
         if (propertyValueFloat <= deviceProperty.getGreaterThan())
         {
            return false;
         }
      }

      if (deviceProperty.getLessThan() != null)
      {
         Float propertyValueFloat = Float.parseFloat(propertyValue);
         if (propertyValueFloat >= deviceProperty.getLessThan())
         {
            return false;
         }
      }

      if (deviceProperty.getEquals() != null)
      {
         if (!propertyValue.equals(deviceProperty.getEquals()))
         {
            return false;
         }
      }

      if (deviceProperty.getMatches() != null)
      {
         if (!deviceProperty.getMatches().matcher(propertyValue).matches())
         {
            return false;
         }
      }

      return true;
   }

   protected boolean checkUserAgentStrings(String userAgentString, Condition condition)
   {
      // check first that we don't want to explicitly not consider this useragent string
      if (condition.getUserAgentDoesNotContain() != null)
      {
         if (userAgentContains(userAgentString, condition.getUserAgentDoesNotContain()))
         {
            return false;
         }
      }

      if ((condition.getUserAgentContains() == null || condition.getUserAgentContains().isEmpty())
            && (condition.getUserAgentPattern() == null || condition.getUserAgentPattern().isEmpty()))
      {
         return true;
      }
      else
      {
         boolean matches = false;

         if (condition.getUserAgentContains() != null)
         {
            matches = userAgentContains(userAgentString, condition.getUserAgentContains());
         }

         if (condition.getUserAgentPattern() != null)
         {
            matches = userAgentMatches(userAgentString, condition.getUserAgentPattern());
         }

         return matches;
      }
   }

   protected boolean userAgentContains(String userAgentString, List<String> contains)
   {
      for (String contain : contains)
      {
         if (userAgentString.contains(contain))
         {
            return true;
         }
      }
      return false;
   }

   protected boolean userAgentMatches(String userAgentString, List<Pattern> patterns)
   {
      for (Pattern pattern : patterns)
      {
         if (pattern.matcher(userAgentString).matches())
         {
            return true;
         }
      }
      return false;
   }
}
