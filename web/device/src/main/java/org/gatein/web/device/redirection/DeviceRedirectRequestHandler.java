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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.portal.application.PortalRequestHandler;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.url.PortalURLContext;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebRequestHandler;
import org.exoplatform.web.url.URLFactoryService;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class DeviceRedirectRequestHandler extends WebRequestHandler
{
   protected static Logger log = LoggerFactory.getLogger(DeviceRedirectRequestHandler.class);

   //The handler name to use
   public static final String HANDLER_NAME = "deviceRedirect";

   public static final String BROWSER_DETECTOR_PAGE_PATH = "/device.jsp"; //TODO: make this configurable somewhere

   //Different states the redirect can be in
   public static final String NO_REDIRECT = "gtn.device.noRedirect";

   public static final String DEVICE_REDIRECT = "gtn.device.redirect";

   //Flag if we have already tried to detect capabilities of the browser
   public static final String DEVICE_DETECTION_ATTEMPTED = "gtn.device.detectionAttempted";

   //The initial URI that was requested
   public static final String INITIAL_URI = "gtn.device.initialURI";

   DeviceRedirectionService deviceRedirectionService;

   URLFactoryService urlFactory;

   public DeviceRedirectRequestHandler(DeviceRedirectionService service, URLFactoryService urlFactory)
   {
      log.debug("DeviceRedirectionService : " + service + " : " + urlFactory);
      this.deviceRedirectionService = service;
      this.urlFactory = urlFactory;
   }

   @Override
   public String getHandlerName()
   {
      return HANDLER_NAME;
   }

   @Override
   public boolean execute(ControllerContext context) throws Exception
   {
      HttpServletRequest request = context.getRequest();
      HttpServletResponse response = context.getResponse();

      String originSiteName = context.getParameter(PortalRequestHandler.REQUEST_SITE_NAME);
      String orginSiteType = context.getParameter(PortalRequestHandler.REQUEST_SITE_TYPE);
      String originalRequestPath = context.getParameter(PortalRequestHandler.REQUEST_PATH);

      if (originalRequestPath != null && originalRequestPath.equals("null"))
      {
         originalRequestPath = null;
      }

      log.debug("Site Redirect being checked on [" + originSiteName + "], with type [" + orginSiteType
            + "], and request path [" + originalRequestPath + "]");

      String redirect = getRedirect(originSiteName, orginSiteType, request);
      if (redirect != null) // a redirect has already been set, use it
      {
         if (redirect.equals(NO_REDIRECT)) //don't do any redirect
         {
            log.debug("[" + originSiteName + ":" + orginSiteType + "] is set to not redirect");
            return false;
         }
         else
         // we need to redirect to another site
         {
            log.debug("[" + originSiteName + ":" + orginSiteType + "] is set to redirect to : " + redirect
                  + ". Attempting Redirect with request path :" + originalRequestPath);
            return performRedirect(originSiteName, redirect, originalRequestPath, context);
         }
      }
      else
      // no redirect set yet, we need to check if a redirect is requested or not
      {
         Map<String, String> deviceProperties = null;

         String userAgentString = request.getHeader("User-Agent"); //TODO: find a proper class to load in this value instead of hardcoding it here
         log.debug("Found user-agent string : " + userAgentString);

         //we only care if this exists or not, no need to set it to anything other than Object
         Object attemptedDeviceDetection = context.getRequest().getSession().getAttribute(DEVICE_DETECTION_ATTEMPTED);
         if (attemptedDeviceDetection != null)
         {
            deviceProperties = getDeviceProperties(request);
            log.debug("Found device properties : " + deviceProperties);
         }

         String redirectSite = deviceRedirectionService.getRedirectSite(originSiteName, userAgentString,
               deviceProperties);

         if (redirectSite == null || redirectSite.equals(DeviceRedirectionService.NO_REDIRECT_DETECTED))
         {
            log.debug("Redirect returned is null or NO_REDIRECT_DETECTED. Setting NO_REDIRECT for this user");
            setRedirect(originSiteName, NO_REDIRECT, request);
            return false;
         }
         else if (redirectSite.equals(DeviceRedirectionService.NEED_BROWSER_DETECTION))
         {
            if (attemptedDeviceDetection == null)
            {
               log.debug("Need browser properties detection. Redirecting to BrowserDetectionPage : "
                     + BROWSER_DETECTOR_PAGE_PATH);
               request.getSession().setAttribute(DEVICE_DETECTION_ATTEMPTED, true);

               String initialURI = request.getRequestURI();
               // add back in any query strings to the initialURI
               if (request.getQueryString() != null)
               {
                  if (initialURI.endsWith("/"))
                  {
                     initialURI = initialURI.substring(0, initialURI.length() - 1);
                  }
                  initialURI += "?" + request.getQueryString();
               }

               request.getSession().setAttribute(INITIAL_URI, initialURI);
               request.setAttribute(INITIAL_URI, initialURI);
               request.getRequestDispatcher(BROWSER_DETECTOR_PAGE_PATH).forward(request, response);
               return true;
            }
            else
            // attemptedDeviceDetection == null //TODO: use two different URLs to prevent any possible situation of a loop?
            {
               log.warn("DeviceDetectionService retruned NEED_BROWSER_DETECTION but the browser has already attempted dection. Setting no redirect.");
               setRedirect(originSiteName, NO_REDIRECT, request);
               return false;
            }
         }
         else
         // the service gave us a redirection site to use, use it.
         {
            log.debug("Redirect for origin site " + originSiteName + " is being set to : " + redirectSite);
            return performRedirect(originSiteName, redirectSite, originalRequestPath, context);
         }
      }
   }

   protected String getRedirect(String siteName, String siteType, HttpServletRequest request)
   {
      Object deviceRedirectObject = request.getSession(true).getAttribute(DEVICE_REDIRECT);

      if (deviceRedirectObject != null && deviceRedirectObject instanceof Map) //TODO: overwrite/delete if not a Map<String, String>?
      {
         Map<String, String> deviceRedirectMap = (Map) deviceRedirectObject;
         return deviceRedirectMap.get(siteName);
      }
      else
      {
         return null;
      }
   }

   protected void setRedirect(String originSite, String redirectSite, HttpServletRequest request)
   {
      Object deviceRedirectObject = request.getSession(true).getAttribute(DEVICE_REDIRECT);

      Map<String, String> redirectMap = (Map) deviceRedirectObject; //TODO: handle the situation if its not a Map<String, String>

      if (redirectMap == null)
      {
         redirectMap = new HashMap<String, String>();
      }

      redirectMap.put(originSite, redirectSite);

      request.getSession().setAttribute(DEVICE_REDIRECT, redirectMap);
   }

   protected Map<String, String> getDeviceProperties(HttpServletRequest request)
   {
      Map<String, String> parameterMap = request.getParameterMap();
      if (parameterMap != null)
      {
         Map<String, String> deviceProperties = new HashMap<String, String>();

         for (String key : parameterMap.keySet())
         {
            if (key.startsWith("gtn.device."))
            {
               deviceProperties.put(key.substring("gtn.device.".length()), request.getParameter(key));
            }
         }
         return deviceProperties;
      }
      else
      {
         return null;
      }
   }

   protected boolean performRedirect(String originSite, String redirectSite, String requestPath,
         ControllerContext context) throws IOException
   {
      log.debug("Attempting redirect to site " + redirectSite + " with request path :" + requestPath);
      String redirectLocation = deviceRedirectionService.getRedirectPath(originSite, redirectSite, requestPath);
      if (redirectLocation != null)
      {
         log.debug("RedirectPath set to : " + redirectLocation);

         setRedirect(originSite, redirectSite, context.getRequest());

         String siteTypeString = context.getParameter(PortalRequestHandler.REQUEST_SITE_TYPE);
         SiteType siteType = SiteType.valueOf(siteTypeString.toUpperCase());
         SiteKey siteKey = new SiteKey(siteType, redirectSite);
         PortalURLContext urlContext = new PortalURLContext(context, siteKey);
         NodeURL url = urlFactory.newURL(NodeURL.TYPE, urlContext);
         String s = url.setResource(new NavigationResource(siteType, redirectSite, redirectLocation)).toString();
         HttpServletResponse resp = context.getResponse();
         resp.sendRedirect(resp.encodeRedirectURL(s));
         return true;
      }
      else
      {
         log.debug("Did not get a node match for redirecting to site [" + redirectSite + "] with requestPath ["
               + requestPath + "]. Cannot perform redirect.");
         return false;
      }
   }

   @Override
   protected boolean getRequiresLifeCycle()
   {
      // Do nothing for now
      return false;
   }

}
