
function setCursorField(field) {
	if(typeof document.DS5250.cursorfield !== "undefined")
   document.DS5250.cursorfield.value = field.name;
}
   
function noHelp(){
return false;
}

document.onhelp=noHelp;

$(function(){
    
    var SHIFT = false;
    var CTRL = false;
     
    document.DS5250.TnKey.value = "";
    document.DS5250.PF.value = "";
    
    
    $(document).keydown( function (e){
    	var k = e.keyCode;
       // if (k == 16){
        //    SHIFT = true;
      //  }
        
        if (k == 17){  // Enter mapped to CTRL
            CTRL = true;
        }
        
        if (CTRL) {document.DS5250.submit(); // CTRL is IBMi = enter
        			CTRL = false;
        			return false;
        			}
        	else
        	{
		        if(e.shiftKey){
		        	
		         	if(k==27) {
		    			document.DS5250.TnKey.Value = "sysreq";
		         	}
		         	if (k > 111){
		         		document.DS5250.PF.value = "PF" + ((k-111) + 12) ;
		         	}
		         	
		         	//SHIFT = false;// reset after setting the key
		        } //SHIFT
		    	else
		    	{
		    		if (k > 111){
		    	
		    			document.DS5250.PF.value = "PF" + (k-111) ;
		    		}
		    		else {
		    			if (k == 34){ 
		    				document.DS5250.TnKey.value = "pgdown";
		    			}
		    			if (k == 33){ 
		    				document.DS5250.TnKey.value = "pgup";
		    			}
		    			if (k == 13){ 
		    				document.DS5250.TnKey.value = "enter";
		    			}
		    		} 
		
		    	} // no SHIFT
		        if ( (document.DS5250.TnKey.value != "") || (document.DS5250.PF.value != ""))
		        	{
			        	document.DS5250.submit();
			        	return false;
		        	}
		        else
		        	return true;
		        
            }
		    });

    
    $(document).keyup( function (e){
        if (e.keyCode == 16){
            SHIFT = false;
            //return false;
        } 
    });
    

    function LogMsg(msg){
        $('<div/>')
        .append(msg)
        .appendTo('#output');
    }
    
});
