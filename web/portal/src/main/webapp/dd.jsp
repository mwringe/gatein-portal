<html>
<head>
  <meta name="viewport" content="initial-scale=1.0, maximum-scale=1, minimum-scale=1, height=device-height, width=device-width, user-scalable=no, target-densitydpi=device-dpi">
 
  <style type="text/css">
    html{
      width=100%;
      height=100%;
      background:url(gatein_logo_loading.gif) center center no-repeat;
    }
  </style>

  <script type="text/javascript">
  
   window.scrollTo(0, 1);
   var propertyMap = {};

   addParameter("screen.height", screen.height);
   addParameter("screen.width", screen.width);
   addParameter("window.devicePixelRatio", window.devicePixelRatio);
   addParameter("screen.availHeight", screen.availHeight);
   addParameter("screen.availWidth", screen.availWidth);
   addParameter("window.outerWidth", window.outerWidth);
   addParameter("window.outerHeight", window.outerHeight);
   addParameter("window.innerWidth", window.innerWidth);
   addParameter("window.innerHeight", window.innerHeight);
   addParameter("orientation", window.orientation);
   addParameter("touchEnabled", touchEnabled());

   submitParameters();

   function addParameter(name, value)
   {
     propertyMap[name] = value;
   }

   function submitParameters ()
   {
     if (propertyMap != null)
     {
       for (key in propertyMap)
       {
         document.writeln( key + "=" + propertyMap[key] + "</br>");
       }
     }
   }

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
 
  </script>

</head>
<body>
<img src="/portal/gatein_logo_loading.gif" alt="LOADING" height="50" width="50"/>
</body>
</html>
