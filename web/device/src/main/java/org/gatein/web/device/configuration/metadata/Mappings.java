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

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class Mappings
{
   protected boolean useNodeNameMatching = false; //defaults to false; TODO: configure what to use for the default in the xml conf file?

   protected unknownNodeMatching unknownNode = unknownNodeMatching.NO_REDIRECT; //defaults to NO_REDIRECT; TODO: configure the default in the xml conf file?

   protected Map<String, String> mappings;

   public enum unknownNodeMatching {
      REDIRECT, NO_REDIRECT, ROOT, COMMON_ANCESTOR_NAME_MATCH
   }

   public boolean isUseNodeNameMatching()
   {
      return useNodeNameMatching;
   }

   public void setIsUseNodeNameMatching(boolean useNodeNameMatching)
   {
      this.useNodeNameMatching = useNodeNameMatching;
   }

   public unknownNodeMatching getUnKnownNodeMatching()
   {
      return unknownNode;
   }

   public void setUnknownNodeMatching(unknownNodeMatching unknownNode)
   {
      this.unknownNode = unknownNode;
   }

   public void addMapping(String originNode, String redirectNode)
   {
      if (mappings == null)
      {
         mappings = new HashMap<String, String>();
      }
      mappings.put(originNode, redirectNode);
   }

   public String getMapping(String originNode)
   {
      if (mappings == null || mappings.get(originNode) == null)
      {
         return null;
      }
      else
      {
         return mappings.get(originNode);
      }
   }

}
