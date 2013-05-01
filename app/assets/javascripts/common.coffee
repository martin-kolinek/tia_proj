requirejs.config
    paths:
    	"dataTables" : "jquery.dataTables"
    	"router" : "jsRoutes"
    shim:
    	"dataTables" : ["jquery"]
    	"bootstrap" : ["jquery"]
    	"underscore" :
    		exports : "_"
    	"router" : 
    	    deps : ["jquery"]
    	    exports : "jsRouter"
    	   
