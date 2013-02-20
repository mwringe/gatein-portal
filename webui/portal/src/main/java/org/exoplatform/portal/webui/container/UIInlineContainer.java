/**
 * Copyright (C) 2009-2013 eXo Platform SAS.
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
package org.exoplatform.portal.webui.container;

import java.util.Collections;
import java.util.List;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.container.UIContainerActionListener.EditContainerActionListener;
import org.exoplatform.portal.webui.portal.UIPortalComponent;
import org.exoplatform.portal.webui.portal.UIPortalComponentActionListener.DeleteComponentActionListener;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
@ComponentConfig(template = "system:/groovy/portal/webui/container/UIInlineContainer.gtmpl", events = {
        @EventConfig(listeners = UIInlineContainer.InsertInlineContainerActionListener.class),
        @EventConfig(listeners = DeleteComponentActionListener.class, confirm = "UIInlineContainer.deleteInlineContainer"),
        @EventConfig(listeners = EditContainerActionListener.class) })
public class UIInlineContainer extends UIContainer{
    public static final String INLINE_CONTAINER = "InlineContainer";

    public static final String INSERT_AFTER = "insertContainerAfter";

    public static final String INSERT_BEFORE = "insertContainerBefore";

    public UIInlineContainer() {
        super();
    }
    
    public static class InsertInlineContainerActionListener extends EventListener<UIInlineContainer> {
        @Override
        public void execute(Event<UIInlineContainer> event) throws Exception {
            String insertPosition = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
            UIInlineContainer uiSelectedContainer = event.getSource();
            UIPortalComponent uiParent = (UIPortalComponent) uiSelectedContainer.getParent();
            if (insertPosition.equals(INSERT_AFTER)) {
                UIInlineContainer.insertInlineContainer(uiSelectedContainer, true);
            } else if (insertPosition.equals(INSERT_BEFORE)) {
                UIInlineContainer.insertInlineContainer(uiSelectedContainer, false);
            }

            PortalRequestContext pcontext = (PortalRequestContext) event.getRequestContext();
            pcontext.addUIComponentToUpdateByAjax(uiParent);
            pcontext.ignoreAJAXUpdateOnPortlets(true);
            pcontext.getJavascriptManager().require("SHARED/portalComposer", "portalComposer")
                    .addScripts("portalComposer.toggleSaveButton();");
        }

    }

    private static void insertInlineContainer(UIInlineContainer selectedContainer, boolean isInsertAfter) throws Exception {
        UIContainer uiParent = selectedContainer.getParent();
        UIInlineContainer uiNewInlineContainer = uiParent.addChild(UIInlineContainer.class, null, null);

        uiNewInlineContainer.setTemplate(selectedContainer.getTemplate());
        uiNewInlineContainer.setFactoryId(selectedContainer.getFactoryId());
        uiNewInlineContainer.setId(String.valueOf(uiNewInlineContainer.hashCode()));

        List<UIComponent> containers = uiParent.getChildren();
        int position = containers.indexOf(selectedContainer);
        if (isInsertAfter) {
            position += 1;
        }
        Collections.rotate(containers.subList(position, containers.size()), 1);
    }
}

