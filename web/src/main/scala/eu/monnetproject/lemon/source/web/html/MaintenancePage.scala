package eu.monnetproject.lemon.source.web.html

import eu.monnetproject.lemon.source.web.WebLemonEditor

object MaintenancePage {

  def page(deployPrefix : String) = <html xmlns="http://www.w3.org/1999/xhtml">
    <head>
      <title>lemon source is down for maintenance</title>
        <link rel="stylesheet" type="text/css" href={deployPrefix + "/css/Aristo.css"}/>
        <link rel="stylesheet" type="text/css" href={deployPrefix + "/css/source.css"}/>
    </head>
    <body>
      <br/>
      <br/>
      <br/>
      <br/>
      <br/>
       <table>
         <tr>
           <td width="200px">
           </td>
           <td width="250px">
            <img src={deployPrefix+"/images/logo.png"}/>
           </td>
           <td>
             Sorry, lemon source is currently down for maintenance. <font size="1">(Yeah, this is really old school but the database genuinely needs to be shut down to load data)</font>
           </td>
         </tr>
       </table>
    </body>
  </html>
}
