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

import java.io.InputStream;
import java.util.regex.Pattern;

import org.staxnav.Naming;
import org.staxnav.StaxNavException;
import org.staxnav.StaxNavigator;
import org.staxnav.StaxNavigatorFactory;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class DescriptorBuilder
{
   public DeviceConfiguration build(InputStream is) throws StaxNavException
   {
      return build(StaxNavigatorFactory.create(new Naming.Enumerated.Simple<Element>(Element.class, Element.UNKNOWN),
            is));
   }

   public DeviceConfiguration build(StaxNavigator<Element> root) throws StaxNavException
   {
      DeviceConfiguration cdc = new DeviceConfiguration();

      if (root.child(Element.DEVICE_REDIRECTS))
      {
         if (root.child() != null)
         {
            for (StaxNavigator<Element> description : root.fork(Element.DEVICE_REDIRECT))
            {
               DeviceRedirect redirectDescriptor = buildDeviceRedirect(description.fork());
               cdc.addDeviceRedirect(redirectDescriptor);
            }
         }
      }
      return cdc;
   }

   protected DeviceRedirect buildDeviceRedirect(StaxNavigator<Element> root) throws StaxNavException
   {
      if (root.child(Element.ORIGIN_SITE))
      {
         DeviceRedirect deviceRedirect = new DeviceRedirect(root.getContent());

         if (root.next(Element.REDIRECTS))
         {
            if (root.child() != null)
            {
               for (StaxNavigator<Element> redirects : root.fork(Element.REDIRECT))
               {
                  Redirect redirect = buildRedirect(redirects.fork());
                  deviceRedirect.addRedirect(redirect);
               }
            }
         }
         return deviceRedirect;
      }
      else
      {
         throw new AssertionError();
      }
   }

   protected Redirect buildRedirect(StaxNavigator<Element> root)
   {
      if (root.child(Element.REDIRECT_SITE))
      {
         Redirect redirect = new Redirect(root.getContent());

         while (root.sibling() != null)
         {
            if (root.getName().equals(Element.CONDITIONS))
            {
               StaxNavigator<Element> fork = root.fork();
               if (fork.child() != null)
               {
                  for (StaxNavigator<Element> conditions : fork.fork(Element.CONDITION))
                  {
                     Condition condition = buildCondition(conditions);
                     redirect.addCondition(condition);
                  }
               }
            }
            else if (root.getName().equals(Element.MAPPINGS))
            {
               Mappings mappings = buildMappings(root.fork());
               redirect.addMapping(mappings);
            }
            else
            {
               throw new AssertionError();
            }
         }
         return redirect;
      }
      else
      {
         throw new AssertionError();
      }
   }

   protected Condition buildCondition(StaxNavigator<Element> root)
   {
      Condition condition = new Condition();
      for (Element elt = root.child(); elt != null; elt = root.sibling())
      {
         StaxNavigator<Element> fork = root.fork();
         switch (elt)
         {
            case USER_AGENT : {
               //               if (fork.child() != null)
               //               {
               for (Element uaElt = fork.child(); uaElt != null; uaElt = fork.sibling())
               {
                  //StaxNavigator<Element> uafork = fork;//.fork();
                  switch (uaElt)
                  {
                     case CONTAINS : {
                        condition.addUserAgentContains(fork.getContent());
                        break;
                     }
                     case DOES_NOT_CONTAIN : {
                        condition.addUserAgentDoesNotContain(fork.getContent());
                        break;
                     }
                     case MATCHES : {
                        condition.addUserAgentMatches(Pattern.compile(fork.getContent()));
                        break;
                     }
                  }
                  //                  }
               }
               break;
            }
            case DEVICE_PROPERTY : {
               condition.addDeviceProperty(buildDeviceProperty(fork));
               break;
            }
         }
      }
      return condition;
   }

   protected Mappings buildMappings(StaxNavigator<Element> root)
   {
      Mappings mapping = new Mappings();
      for (Element elt = root.child(); elt != null; elt = root.sibling())
      {
         switch (elt)
         {
            case USE_NODE_NAME_MATCHING : {
               mapping.setIsUseNodeNameMatching(Boolean.parseBoolean(root.getContent()));
               break;
            }
            case UNKNOWN_NODE_MATCHING : {
               mapping.setUnknownNodeMatching(Mappings.unknownNodeMatching.valueOf(root.getContent()));
               break;
            }
            case COMMON_ANCESTOR_NAME_MATCHING : {
               mapping.setUnknownNodeMatching(Mappings.unknownNodeMatching.valueOf(root.getContent()));
               break;
            }
            case MAP : {
               String originPath = null;
               String redirectPath = null;
               while (root.next() != null)
               {
                  if (root.getName().equals(Element.ORIGIN_PATH))
                  {
                     originPath = root.getContent();
                  }
                  else if (root.getName().equals(Element.REDIRECT_PATH))
                  {
                     redirectPath = root.getContent();
                  }
                  else
                  {
                     throw new AssertionError();
                  }
               }
               if (originPath != null && redirectPath != null)
               {
                  mapping.addMapping(originPath, redirectPath);
               }
               else
               {
               }
               break;
            }
            default : {
               throw new AssertionError();
            }
         }
      }
      return mapping;
   }

   protected DeviceProperty buildDeviceProperty(StaxNavigator<Element> root)
   {
      if (root.child(Element.PROPERTY_NAME))
      {
         DeviceProperty deviceProperty = new DeviceProperty(root.getContent());

         while (root.sibling() != null)
         {
            if (root.getName().equals(Element.GREATER_THAN))
            {
               deviceProperty.setGreaterThan(Float.parseFloat(root.getContent()));
            }
            else if (root.getName().equals(Element.LESS_THAN))
            {
               deviceProperty.setLessThan(Float.parseFloat(root.getContent()));
            }
            else if (root.getName().equals(Element.EQUALS))
            {
               deviceProperty.setEquals(root.getContent());
            }
            else if (root.getName().equals(Element.MATCHES))
            {
               deviceProperty.setMatches(Pattern.compile(root.getContent()));
            }
            else
            {
               throw new AssertionError();
            }
         }
         return deviceProperty;
      }
      else
      {
         throw new AssertionError();
      }
   }
}
