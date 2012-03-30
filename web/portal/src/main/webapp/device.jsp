<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en"> 
<head>

  <script type="text/javascript">
if (document.images) {
    img1 = new Image();
    img1.src = "/portal/gatein_logo_loading.gif";
}
</script>

  <meta name="viewport" content="initial-scale=1.0, maximum-scale=0, minimum-scale=1, height=device-height, width=device-width"/>
  <% String initialURI = (String)request.getAttribute("gtn.device.initialURI"); %>
  <% if (initialURI != null)
     { %>
       <meta http-equiv="REFRESH" content="120;url=<%=initialURI%>"/>
  <% }
  %>
</head>
<body>
  <script type="text/javascript">

   var propertyMap = {};

   // ADD DEVICE PROPERTIES HERE 
   addParameter("screen.height", screen.height);
   addParameter("screen.width", screen.width);
   addParameter("window.devicePixelRatio", window.devicePixelRatio);
   addParameter("touch.enabled", touchEnabled());
   addParameter("window.innerWidth", window.innerWidth);
//   submitParameters();


   function touchEnabled()
   {
     if ('ontouchstart' in window)
     {
       return true
     }
     else
     {
       return false;
     }
   }


   function addParameter(name, value)
   {
     propertyMap["gtn.device." + name] = value;
   }

   function submitParameters ()
   {
     if (propertyMap != null)
     {
       var form = document.createElement("form");
       form.setAttribute("method", "post");
       form.setAttribute("action", "<%= initialURI %>");
       
       for (key in propertyMap)
       {
          var input = document.createElement("input");
          input.setAttribute("type", "hidden");
          input.setAttribute("name", key);
          input.setAttribute("value", propertyMap[key]);
          form.appendChild(input);
       }
       document.body.appendChild(form);    
       form.submit();

     }
   }

  </script>

 <img src="/portal/gatein_logo_loading.gif" onload="submitParameters()" style="display:block; margin-left:auto; margin-right:auto"/>
</body>
</html>
