/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.gatein.portal.controller.resource.script;

import org.exoplatform.commons.utils.I18N;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.controller.QualifiedName;
import org.gatein.portal.controller.resource.Resource;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceRequestHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * <p></p>
 * 
 * <p></p>This class implements the {@link Comparable} interface, however the natural ordering provided here
 * is not consistent with equals, therefore this class should not be used as a key in a {@link java.util.TreeMap}
 * for instance.</p>
 * 
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ScriptResource extends Resource<ScriptResource> implements Comparable<ScriptResource>
{

   /** . */
   ScriptGraph graph;
   
   /** . */
   private final List<Module> modules;

   /** . */
   private final Map<QualifiedName, String> parameters;

   /** . */
   private final Map<Locale, Map<QualifiedName, String>> parametersMap;

   /** . */
   private final Map<QualifiedName, String> minParameters;

   /** . */
   private final  Map<Locale, Map<QualifiedName, String>> minParametersMap;

   /** . */
   final HashSet<ResourceId> dependencies;

   /** . */
   final HashSet<ResourceId> closure;

   /** . */
   FetchMode fetchMode;

   ScriptResource(ScriptGraph graph, ResourceId id, FetchMode fetchMode)
   {
      super(id);

      //
      Map<QualifiedName, String> parameters = new HashMap<QualifiedName, String>();
      parameters.put(WebAppController.HANDLER_PARAM, "script");
      parameters.put(ResourceRequestHandler.RESOURCE_QN, id.getName());
      parameters.put(ResourceRequestHandler.SCOPE_QN, id.getScope().name());
      parameters.put(ResourceRequestHandler.COMPRESS_QN, "");
      parameters.put(ResourceRequestHandler.VERSION_QN, ResourceRequestHandler.VERSION);
      parameters.put(ResourceRequestHandler.LANG_QN, "");

      //
      Map<QualifiedName, String> minifiedParameters = new HashMap<QualifiedName, String>(parameters);
      minifiedParameters.put(ResourceRequestHandler.COMPRESS_QN, "min");

      //
      this.parameters = parameters;
      this.minParameters = minifiedParameters;
      this.graph = graph;
      this.modules = new ArrayList<Module>();
      this.closure = new HashSet<ResourceId>();
      this.dependencies = new HashSet<ResourceId>();
      this.fetchMode = fetchMode;
      this.parametersMap = new HashMap<Locale, Map<QualifiedName, String>>();
      this.minParametersMap = new HashMap<Locale, Map<QualifiedName, String>>();
   }

   public boolean isEmpty()
   {
      return modules.isEmpty();
   }

   public FetchMode getFetchMode()
   {
      return fetchMode;
   }

   public Map<QualifiedName, String> getParameters(boolean minified, Locale locale)
   {
      Map<Locale, Map<QualifiedName, String>> map = minified ? minParametersMap : parametersMap;
      for (Locale current = locale;current != null;current = I18N.getParent(current))
      {
         Map<QualifiedName, String> ret = map.get(locale);
         if (ret != null)
         {
            return ret;
         }
      }
      return minified ? minParameters : parameters;
   }

   public void addDependency(ResourceId dependencyId)
   {
      ScriptResource dependency = graph.getResource(dependencyId);

      // Detect cycle
      if (dependency != null && dependency.closure.contains(id))
      {
         throw new IllegalStateException("Going to create a cycle");
      }

      // That is important to make closure independent from building order of graph nodes.
      if(dependency != null)
      {
         closure.addAll(dependency.getClosure());
      }
      
      //Update the source's closure
      closure.add(dependencyId);
      
      // Update any entry that points to the source
      for (Map<String, ScriptResource> resources : graph.resources.values())
      {
         for (ScriptResource resource : resources.values())
         {
            if (resource.closure.contains(id))
            {
               resource.closure.addAll(closure);
            }
         }
      }                
      
      //
      dependencies.add(dependencyId);
   }

   public Set<ResourceId> getClosure()
   {
      return closure;
   }

   public Module.Local addLocalModule(String contextPath, String name, String path, String resourceBundle, int priority)
   {
      Module.Local module = new Module.Local(this, contextPath, name, path, resourceBundle, priority);
      modules.add(module);
      return module;
   }

   public Module.Remote addRemoteModule(String contextPath, String name, String path, int priority)
   {
      Module.Remote module = new Module.Remote(this, contextPath, name, path, priority);
      modules.add(module);
      return module;
   }
   
   public List<Module> removeModuleByName(String name)
   {
      ArrayList<Module> removed = new ArrayList<Module>();
      for (Iterator<Module> i = modules.iterator();i.hasNext();)
      {
         Module module = i.next();
         if (module.getName().equals(name))
         {
            removed.add(module);
            i.remove();
         }
      }
      return removed;
   }
   
   public Module getModule(String name)
   {
      for (Module module : modules)
      {
         if (module.getName().equals(name))
         {
            return module;
         }
      }
      return null;
   }

   public List<Module> removeModuleByContextPath(String contextPath)
   {
      ArrayList<Module> removed = new ArrayList<Module>();
      for (Iterator<Module> i = modules.iterator();i.hasNext();)
      {
         Module module = i.next();
         if (module.getContextPath().equals(contextPath))
         {
            removed.add(module);
            i.remove();
         }
      }
      return removed;
   }

   public List<Module> getModules()
   {
      return modules;
   }
   
   public List<String> getModulesNames()
   {
      ArrayList<String> names = new ArrayList<String>();
      for (Module script : modules)
      {
         names.add(script.getName());
      }
      return names;
   }

   public int compareTo(ScriptResource o)
   {
      if (closure.contains(o.id))
      {
         return 1;
      }
      else if (o.closure.contains(id))
      {
         return -1;
      }
      else
      {
         return 0;
      }
   }
   
   public void addSupportedLocale(Locale locale)
   {
      if (!parametersMap.containsKey(locale))
      {
         Map<QualifiedName, String> localizedParameters = new HashMap<QualifiedName, String>(parameters);
         localizedParameters.put(ResourceRequestHandler.LANG_QN, I18N.toTagIdentifier(locale));
         parametersMap.put(locale, localizedParameters);
         Map<QualifiedName, String> localizedMinParameters = new HashMap<QualifiedName, String>(minParameters);
         localizedMinParameters.put(ResourceRequestHandler.LANG_QN, I18N.toTagIdentifier(locale));
         minParametersMap.put(locale, localizedMinParameters);
      }
   }

   @Override
   public Set<ResourceId> getDependencies()
   {
      return dependencies;
   }

   @Override
   public String toString()
   {
      return "ScriptResource[id=" + id + "]";
   }
}
