/*
 * Copyright (C) 2012 eXo Platform SAS.
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

eXo.initWebUISamplePortlet = function(id) {
  require([ "SHARED/jquery" ], function(gj) {
    gj('#' + id).find('.CollapseIcon').on('click', function() {
      var subGroup = gj(this.parentNode).children("div.ChildrenContainer")[0];
      var className = this.className;
      if (!subGroup) {
        return;
      }
      if (subGroup.style.display == "none") {
        if (className.indexOf("ExpandIcon") == 0) {
          this.className = "CollapseIcon ClearFix";
        }
        subGroup.style.display = "block";
      } else {
        if (className.indexOf("CollapseIcon") == 0) {
          this.className = "ExpandIcon ClearFix";
        }
        subGroup.style.display = "none";
      }
    });
  });
}