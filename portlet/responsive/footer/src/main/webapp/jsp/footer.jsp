<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<portlet:defineObjects/>
<jsp:useBean id="footer" class="org.gatein.portlet.responsive.footer.FooterBean"/>

<%
Locale locale = renderRequest.getLocale();
ResourceBundle resourceBundle = portletConfig.getResourceBundle(locale);

List<Locale> languages = footer.getLanguages();
%>

<div class="gtnResponsiveFooterPortlet">
  <div class="gtn_CopyrightInfo"><%= resourceBundle.getString("copyrightText")%>
     <a href="http://www.redhat.com/"><%= resourceBundle.getString("RedHatInc") %></a>
     <%= resourceBundle.getString("and") %>
     <a href="http://www.exoplatform.com/"><%= resourceBundle.getString("eXoPlatformSAS")%></a>
  </div>
  
  <div class="gtn_options">
    <ul>
      <li><a href="http://www.gatein.org">gatein.org</a></li>
      <li><a href="#"><%= resourceBundle.getString("contactUs") %></a></li>
      <li>
        <select id="grf_languageSelect">
          <% for (Locale language : languages)
          {
             String languageName = language.getDisplayLanguage(locale);
             String languageNameinLanguage = language.getDisplayLanguage(language);
             
             if (language.getCountry() != null && !language.getCountry().isEmpty())
             {
                languageName += "(" + language.getDisplayCountry(locale) + ")";
                languageNameinLanguage += "(" + language.getDisplayCountry(language) + ")";
             }
             
             if (!language.equals(locale))
             {
             %>
                 <option><%= languageName + " - " + languageNameinLanguage %></option>
          <% }
             else
             { %>
                 <option selected="selected"><%= languageName %></option>
          <% }
          }
          %>
        </select>
      </li>
    </ul>
  </div>
</div>

<script type="text/javascript">

   var GRHLanguageSelect = document.getElementById("grf_languageSelect");
   GRHLanguageSelect.style.width = GRHLanguageSelect.options[GRHLanguageSelect.selectedIndex].text.length + "em";

</script>