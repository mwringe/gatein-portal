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

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.GenericScope;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.Scope;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.web.device.configuration.metadata.DeviceConfiguration;
import org.gatein.web.device.configuration.metadata.Mappings;
import org.gatein.web.device.configuration.metadata.Redirect;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class Mapper
{
   protected static Logger log = LoggerFactory.getLogger(Mapper.class);

   DeviceConfiguration dcd;

   NavigationService navService;

   public Mapper(DeviceConfiguration dcd, NavigationService navService)
   {
      this.dcd = dcd;
      this.navService = navService;
   }

   public String getRedirectPath(String originSite, String redirectSite, String requestPath)
   {
      return getRequestPath(originSite, redirectSite, requestPath);
   }

   protected String getRequestPath(String originSite, String redirectSite, String requestPath)
   {
      if (dcd.getDeviceRedirects(originSite) != null)
      {
         Redirect redirect = dcd.getDeviceRedirects(originSite).getRedirect(redirectSite);
         if (redirect == null)
         {
            return null;
         }

         Mappings mappings = redirect.getMapping();

         //first check if we have explicit mappings for this requestPath
         if (mappings.getMapping(requestPath) != null)
         {
            return mappings.getMapping(requestPath);
         }

         //next check if we use node name matching
         if (mappings.isUseNodeNameMatching())
         {
            String redirectRequestPath = getNodeIfExists(redirectSite, requestPath,
                  mappings.getUnKnownNodeMatching() == Mappings.unknownNodeMatching.COMMON_ANCESTOR_NAME_MATCH);
            {
               if (redirectRequestPath != null)
               {
                  return redirectRequestPath;
               }
            }
         }

         // if no explicit mapping, no name matching and not using common ancestor
         if (mappings.getUnKnownNodeMatching() == Mappings.unknownNodeMatching.NO_REDIRECT)
         {
            return null;
         }
         else if (mappings.getUnKnownNodeMatching() == Mappings.unknownNodeMatching.REDIRECT)
         {
            return requestPath;
         }
         else if (mappings.getUnKnownNodeMatching() == Mappings.unknownNodeMatching.ROOT)
         {
            return "";
         }
         else
         //unknown, return null to be sure
         {
            return null;
         }
      }
      else
      {
         return null;
      }
   }

   protected String getNodeIfExists(String redirectSite, String requestPath, Boolean useCommonAncestor)
   {
      log.debug("GetNodeExits called [" + redirectSite + "] : [" + requestPath + "]");

      //TODO: fix this check somewhere else
      if (requestPath == null || requestPath.isEmpty())
      {
         return "";
      }

      RequestLifeCycle.begin(ExoContainerContext.getCurrentContainer());
      String[] path = requestPath.split("/");
      NavigationContext navContext = navService.loadNavigation(SiteKey.portal(redirectSite));
      NodeContext nodeContext = navService.loadNode(NodeModel.SELF_MODEL, navContext,
            GenericScope.branchShape(path, Scope.ALL), null);

      boolean found = true;
      String lastCommonAncestor = "";

      for (String nodeName : path)
      {
         nodeContext = nodeContext.get(nodeName);
         if (nodeContext == null)
         {
            found = false;
            break;
         }
         else
         {
            lastCommonAncestor += "/" + nodeContext.getName();
         }
      }
      RequestLifeCycle.end();

      if (found == true)
      {
         return requestPath;
      }
      else if (useCommonAncestor)
      {
         return lastCommonAncestor;
      }
      else
      {
         return null;
      }
   }

}
