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
public class Redirect
{
   protected String redirectSiteName;

   protected Mappings mappings;

   protected List<Condition> conditions;

   public Redirect(String redirectSiteName)
   {
      this.redirectSiteName = redirectSiteName;
   }

   public String getRedirectSiteName()
   {
      return redirectSiteName;
   }

   public void addCondition(Condition condition)
   {
      if (conditions == null)
      {
         conditions = new ArrayList<Condition>();
      }
      conditions.add(condition);
   }

   public List<Condition> getConditions()
   {
      return this.conditions;
   }

   public void addMapping(Mappings mappings)
   {
      this.mappings = mappings;
   }

   public Mappings getMapping()
   {
      return mappings;
   }
}
