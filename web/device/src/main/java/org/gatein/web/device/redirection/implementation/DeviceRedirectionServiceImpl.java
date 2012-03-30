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
package org.gatein.web.device.redirection.implementation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.exoplatform.commons.utils.Safe;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.web.device.configuration.metadata.DescriptorBuilder;
import org.gatein.web.device.configuration.metadata.DeviceConfiguration;
import org.gatein.web.device.redirection.DeviceRedirectionService;
import org.gatein.web.device.redirection.Mapper;
import org.gatein.web.device.redirection.Redirector;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class DeviceRedirectionServiceImpl implements DeviceRedirectionService, Startable
{
   protected static Logger log = LoggerFactory.getLogger(DeviceRedirectionServiceImpl.class);

   Redirector redirector;

   Mapper mapper;

   NavigationService navService;

   public DeviceRedirectionServiceImpl(InitParams initParams, NavigationService navService) throws IOException
   {
      this.navService = navService;
      ValueParam configLocation = initParams.getValueParam("device.config");

      if (configLocation != null && configLocation.getValue() != null)
      {
         loadConfiguration(configLocation.getValue());
      }
      else
      {
         throw new IllegalArgumentException("The device configuration xml file is not specified.");
      }
   }

   @Override
   public String getRedirectSite(String originSite, String userAgentString, Map<String, String> deviceProperties)
   {
      if (redirector != null)
      {
         return redirector.getRedirectSite(originSite, userAgentString, deviceProperties);
      }
      else
      {
         log.debug("Redirector not set. Cannot determine redirect.");
         return null;
      }
   }

   @Override
   public String getRedirectPath(String originSite, String redirectSite, String requestPath)
   {
      if (mapper != null)
      {
         if (requestPath == null || requestPath.isEmpty())
         {
            return "";
         }
         else
         {
            return mapper.getRedirectPath(originSite, redirectSite, requestPath);
         }
      }
      else
      {
         log.debug("Mapper not set. Cannot determine node to map to. Returning Null.");
         return null;
      }
   }

   protected void loadConfiguration(String configLocation) throws IOException
   {
      File configFile = new File(configLocation);
      if (!configFile.exists())
      {
         throw new MalformedURLException("Could not resolve path " + configFile);
      }
      else if (!configFile.isFile())
      {
         throw new MalformedURLException("Could not resolve path " + configFile + " to a valid file");
      }

      loadConfiguration(configFile.toURI().toURL());
   }

   protected void loadConfiguration(URL configFile) throws IOException
   {
      log.debug("Loading device configuration file from : " + configFile);

      InputStream is = configFile.openStream();
      try
      {
         DeviceConfiguration dcd = new DescriptorBuilder().build(is);
         redirector = new Redirector(dcd);
         mapper = new Mapper(dcd, navService);
      }
      finally
      {
         Safe.close(is);
      }
   }

   @Override
   public void start()
   {
      // only needed because exo kernel requires this method
   }

   @Override
   public void stop()
   {
      // only needed because exo kernel requires this method
   }
}
