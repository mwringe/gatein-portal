/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.web.application;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceScope;
import org.gatein.portal.controller.resource.script.FetchMap;
import org.gatein.portal.controller.resource.script.FetchMode;
import org.gatein.portal.controller.resource.script.ScriptResource;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by The eXo Platform SAS
 * Mar 27, 2007  
 */
public class JavascriptManager
{
   Log log = ExoLogger.getLogger("portal:JavascriptManager");
   
   /** . */
   private Set<String> importedScripts = new HashSet<String>();

   /** . */
   private FetchMap<ResourceId> resourceIds = new FetchMap<ResourceId>();
   
   /** . */
   private FetchMap<String> extendedScriptURLs = new FetchMap<String>();

   /** . */
   private StringBuilder scripts = new StringBuilder();

   /** . */
   private StringBuilder customizedOnloadJavascript = new StringBuilder();

   /**
    * Add a valid javascript code
    * 
    * @param s a valid javascript code
    */
   public void addJavascript(CharSequence s)
   {
      if (s != null)
      {
         scripts.append(s.toString().trim());
         scripts.append(";\n");
      }
   }

   /**
    * Register a SHARE Javascript resource that will be loaded in Rendering phase
    * Script FetchMode is ON_LOAD by default
    */
   public void loadScriptResource(String name)
   {      
      loadScriptResource(ResourceScope.SHARED, name, null);
   }

   /**
    * Register a Javascript resource that will be loaded in Rendering phase
    * If mode is null, script will be loaded with mode defined in gatein-resources.xml
    */
   public void loadScriptResource(ResourceScope scope, String name, FetchMode mode)
   {
      if (scope == null)
      {
         throw new IllegalArgumentException("scope can't be null");
      }
      if (name == null)
      {
         throw new IllegalArgumentException("name can't be null");
      }
      resourceIds.add(new ResourceId(scope, name), mode);
   }

   public FetchMap<ResourceId> getScriptResources()
   {
      return resourceIds;
   }
   
   public FetchMap<String> getExtendedScriptURLs()
   {
      return new FetchMap<String>(extendedScriptURLs);
   }

   public void addExtendedScriptURLs(String url, FetchMode mode)
   {
      if (mode == null) 
      {
         mode = FetchMode.IMMEDIATE;
      }
      this.extendedScriptURLs.add(url, mode);
   }

   @Deprecated
   public void importJavascript(CharSequence s)
   {
      String moduleName = s.toString();
      JavascriptConfigService jsSrevice_ =
         (JavascriptConfigService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(
            JavascriptConfigService.class);
      ScriptResource res = jsSrevice_.getResourceIncludingModule(moduleName);
      if (res != null)
      {
         try
         {
            ResourceId id = res.getId();
            loadScriptResource(id.getScope(), id.getName(), null);
            if (log.isWarnEnabled())
            {
               log.warn("This method is deprecated. You could loadScriptResource " + res.getId() + " instead of importJavascript " + moduleName);
            }
         }
         catch (Exception ex)
         {
            //Spare me, Sonar! This importJavascript is to be deleted soon
            ex.printStackTrace();
         }
      }
      else
      {
         importJavascript(moduleName, null);
      }
   }

   @Deprecated
   public void importJavascript(String s, String location)
   {
      if (s != null)
      {
         JavascriptConfigService jsSrevice_ =
            (JavascriptConfigService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(
               JavascriptConfigService.class);
         if (!jsSrevice_.isModuleLoaded(s) || PropertyManager.isDevelopping())
         {
            if (location == null)
            {
               location = "/eXoResources/javascript/";
            }

            String path = location + s.replaceAll("\\.", "/") + ".js";
            if (!jsSrevice_.isJavascriptLoaded(path) || PropertyManager.isDevelopping())
            {
               importedScripts.add(path);
            }
         }
      }
   }

   public void addOnLoadJavascript(CharSequence s)
   {
      if (s != null)
      {
         String id = Integer.toString(Math.abs(s.hashCode()));
         scripts.append("eXo.core.Browser.addOnLoadCallback('mid");
         scripts.append(id);
         scripts.append("',");
         scripts.append(s instanceof String ? (String)s : s.toString());
         scripts.append(");\n");
      }
   }

   public void addOnResizeJavascript(CharSequence s)
   {
      if (s != null)
      {
         String id = Integer.toString(Math.abs(s.hashCode()));
         scripts.append("eXo.core.Browser.addOnResizeCallback('mid");
         scripts.append(id);
         scripts.append("',");
         scripts.append(s instanceof String ? (String)s : s.toString());
         scripts.append(");\n");
      }
   }

   public void addOnScrollJavascript(CharSequence s)
   {
      if (s != null)
      {
         String id = Integer.toString(Math.abs(s.hashCode()));
         scripts.append("eXo.core.Browser.addOnScrollCallback('mid");
         scripts.append(id);
         scripts.append("',");
         scripts.append(s instanceof String ? (String)s : s.toString());
         scripts.append(");\n");
      }
   }

   public void addCustomizedOnLoadScript(CharSequence s)
   {
      if (s != null)
      {
         customizedOnloadJavascript.append(s.toString().trim());
         customizedOnloadJavascript.append(";\n");
      }
   }

   @Deprecated
   public void writeCustomizedOnLoadScript(Writer writer) throws IOException
   {
      if (customizedOnloadJavascript != null)
      {
         writer.write(customizedOnloadJavascript.toString());
      }
   }

   /**
    * Returns javascripts which were added by {@link #addJavascript(CharSequence)},
    * {@link #addOnLoadJavascript(CharSequence)}, {@link #addOnResizeJavascript(CharSequence)},
    * {@link #addOnScrollJavascript(CharSequence)}, {@link #addCustomizedOnLoadScript(CharSequence)}
    * 
    * @return
    */
   public String getJavaScripts()
   {
      StringBuilder callback = new StringBuilder();
      callback.append(scripts);
      callback.append("eXo.core.Browser.onLoad();\n");
      callback.append(customizedOnloadJavascript);
      return callback.toString();
   }

   public Set<String> getImportedJavaScripts()
   {
      return importedScripts;
   }

   /**
    * @deprecated You would handle the returned javascripts from {@link #getJavaScripts()} and 
    * {@link #getImportedJavaScripts()} by yourself instead of delegating JavascriptManager to
    * write the output
    * 
    * @param writer
    * @throws IOException
    */
   @Deprecated
   public void writeJavascript(Writer writer) throws IOException
   {
      StringBuilder callback = new StringBuilder();
      callback.append(scripts);
      callback.append("eXo.core.Browser.onLoad();\n");
      callback.append(customizedOnloadJavascript);

      if (importedScripts.size() > 0)
      {
         String jsPaths = buildJSArray(importedScripts);

         StringBuilder builder = new StringBuilder("eXo.loadJS(");
         builder.append(jsPaths).append(",");
         builder.append("function() {").append(callback.toString()).append("});");
         writer.write(builder.toString());
      }
      else
      {
         writer.write(callback.toString());
      }
   }

   private String buildJSArray(Collection<String> params)
   {
      StringBuilder pathBuilder = new StringBuilder("[");
      for (String str : params)
      {
         pathBuilder.append("'").append(str).append("',");
      }
      if (!params.isEmpty())
      {
         pathBuilder.deleteCharAt(pathBuilder.length() - 1);
      }
      pathBuilder.append("]");
      return pathBuilder.toString();
   }
}
