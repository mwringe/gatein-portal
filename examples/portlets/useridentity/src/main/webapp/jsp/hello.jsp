<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects/>

<div class="portlet-section-header">Currently Authenticated User: <%= renderRequest.getRemoteUser() %></div>
